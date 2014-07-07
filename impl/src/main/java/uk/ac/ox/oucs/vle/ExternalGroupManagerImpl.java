package uk.ac.ox.oucs.vle;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.ac.ox.oucs.vle.ExternalGroupException.Type;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;

import edu.amc.sakai.user.JLDAPDirectoryProvider;
import edu.amc.sakai.user.LdapConnectionManager;

public class ExternalGroupManagerImpl implements ExternalGroupManager {

	private static Log log = LogFactory.getLog(ExternalGroupManagerImpl.class);
	
	LdapConnectionManager ldapConnectionManager;
	
	JLDAPDirectoryProvider jldapDirectoryProvider;
	
	UserDirectoryService userDirectoryService;
	
	MappedGroupDao mappedGroupDao;

	private String memberFormat = "oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk";

	private String groupBase = "dc=oak,dc=ox,dc=ac,dc=uk";

	private String[] searchAttributes = {"displayName"};

	private int SEARCH_LIMIT = 50;

	private String searchPattern = "(&{0}(member=*)(objectClass=oakGroupAbs))";
	
	private List<PathHandler> pathHandlers;

	private String memberAttribute = "member";

	private Map<String, String> displayNames = new HashMap<String, String>();

	// The maximum number of results parsed from LDAP.
	private int maxResults = 2000;

	public void init() {
		log.debug("init()");
		if (ldapConnectionManager == null && jldapDirectoryProvider == null) {
			throw new IllegalStateException("Don't have a way of getting a LdapConnectionManager");
		}
		if (userDirectoryService == null) {
			throw new IllegalStateException("UserDirectoryService must be set.");
		}
		if (mappedGroupDao == null) {
			throw new IllegalStateException("MappedGroupDao must be set.");
		}
		
		DisplayAdjuster da = new MappedDisplayAdaptor(displayNames);
		
		pathHandlers = new ArrayList<PathHandler>();
		pathHandlers.add(new StaticPathHandler(Arrays.asList(new ExternalGroupNode[]{
				new ExternalGroupNodeImpl("courses", "Course Groups"),
				new ExternalGroupNodeImpl("units", "Unit Groups")}))
		);
		pathHandlers.add(new UniquePathHandler("cn=courses,dc=oak,dc=ox,dc=ac,dc=uk", "courses", "oakOSSUnitCode", "oakOSSUnitName", this));
		pathHandlers.add(new AttributePathHandler("cn=courses,dc=oak,dc=ox,dc=ac,dc=uk", "courses", "oakOSSUnitCode", "oakGN", "displayName", this));
		pathHandlers.add(new SubPathHandler("oakGN=%s,cn=courses,dc=oak,dc=ox,dc=ac,dc=uk", "courses", "oakOSSUnitCode", "displayName", this));
		
		UniquePathHandler unitsUnique = new UniquePathHandler("ou=units,dc=oak,dc=ox,dc=ac,dc=uk", "units", "oakDivision", "oakDivision", this);
		unitsUnique.setDisplayAdjuster(da);
		
		pathHandlers.add(unitsUnique); // Needs a mapper.
		pathHandlers.add(new AttributePathHandler("ou=units,dc=oak,dc=ox,dc=ac,dc=uk", "units", "oakDivision", "oakUnitCode", "displayName", this));
		pathHandlers.add(new SubPathHandler("oakUnitCode=%s,ou=units,dc=oak,dc=ox,dc=ac,dc=uk", "units", "oakUnitCode", "displayName", this));
		
	}

	public String addMappedGroup(String externalGroupId, String role) throws ExternalGroupException {
		ExternalGroup group = findExternalGroup(externalGroupId);
		if (group == null) {
			return null;
		}
		MappedGroup mappedGroup = mappedGroupDao.findByGroupRole(externalGroupId, role);
		if (mappedGroup == null) {
			return mappedGroupDao.newMappedGroup(externalGroupId, role);
		} else {
			return mappedGroup.getId();
		}
	}

	public String findExternalGroupId(String mappedGroupId) {
		MappedGroup mappedGroup = mappedGroupDao.findById(mappedGroupId);
		return (mappedGroup == null)?null:mappedGroup.getExternalGroup();
	}
	
	public String findRole(String mappedGroupId) {
		MappedGroup mappedGroup = mappedGroupDao.findById(mappedGroupId);
		return (mappedGroup == null)?null:mappedGroup.getRole();
	}

	public ExternalGroup findExternalGroup(String externalGroupId) throws ExternalGroupException {
		if (externalGroupId == null || externalGroupId.length() < 0) {
			return null;
		}
		ExternalGroup group = null;
		LDAPConnection connection = null;
		try {
			connection = getConnection();
			LDAPEntry entry = connection.read(externalGroupId, getSearchAttributes());
			if (entry != null) {
				group = convert(entry);
			}
		} catch (LDAPException ldape) {
			// Not finding a DN throws an exception.
			switch (ldape.getResultCode()) {
			case LDAPException.NO_SUCH_OBJECT:
				log.warn("Didn't find group ID: " + externalGroupId);
				break;
			case LDAPException.INVALID_DN_SYNTAX:
				log.warn("Badly formed DN: " + externalGroupId);
				throw new IllegalArgumentException("Badly formed DN: "+ externalGroupId);
			default:
				// If there is a problem with LDAP we must throw this so we don't update groups
				// to be empty.
				throw new ExternalGroupException("Failed to lookup group: "+ externalGroupId, ldape);
			}
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
		return group;
	}

	public Map<String, String> getGroupRoles(String userId) throws ExternalGroupException {
		MessageFormat formatter = new MessageFormat(memberFormat);
		String member = formatter.format(new Object[]{userId});
		LDAPConnection connection = null;
		Map<String, String> groupRoles;
		try {
			connection = getConnection();
			String filter = memberAttribute+ "="+member;
			LDAPSearchResults results = connection.search(groupBase, LDAPConnection.SCOPE_SUB, filter, getSearchAttributes(), false);
			groupRoles = new HashMap<String, String>();
			while (results.hasMore()) {
				ExternalGroup group = convert(results.next());
				if (group != null) {
					List <MappedGroup> mappedGroups = mappedGroupDao.findByGroup(group.getId());
					for (MappedGroup mappedGroup: mappedGroups) {
						groupRoles.put(mappedGroup.getId(), mappedGroup.getRole());
					}
				}
			}
			return groupRoles;
		} catch (LDAPException ldape) {
			throw new ExternalGroupException("Failed to lookup group roles for : "+ userId, ldape);
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
	}
	public List<ExternalGroup> search(String[] terms) throws ExternalGroupException {
		if (terms == null || terms.length == 0) {
			return Collections.emptyList();
		}
		StringBuilder query = new StringBuilder();
		query.append("(&");
		for (String term: terms) {
			if (isValidTerm(term)) {
				query.append("(displayName=*");
				query.append(escapeSearchFilterTerm(term));
				query.append("*)");
			}
		}
		query.append(")");
		return doSearch(query.toString());
	}

	private boolean isValidTerm(String term) {
		// We want to be able to search for 1
		return term != null && (term.length() > 1 ||
			(term.length() == 1 && Character.isDigit(term.charAt(0))));
	}

	public List<ExternalGroup> search(String term) throws ExternalGroupException{
		if (term == null || term.length() == 0) {
			return Collections.emptyList();
		}
		String query = "(displayName=*"+ escapeSearchFilterTerm(term)+ "*)";
		return doSearch(query);
	}

	protected List<ExternalGroup> doSearch(String query)
			throws ExternalGroupException {
		List<ExternalGroup> groups = null;
		LDAPConnection connection = null;
		try {
			connection = getConnection();
			LDAPSearchConstraints constraints = new LDAPSearchConstraints(connection.getConstraints());
			constraints.setMaxResults(SEARCH_LIMIT);
			connection.setConstraints(constraints);
			MessageFormat filterFormat = new MessageFormat(searchPattern);
			String filter = filterFormat.format(new Object[]{query});
			LDAPSearchResults results = connection.search(groupBase, LDAPConnection.SCOPE_SUB, filter, getSearchAttributes(), false);
			groups = new ArrayList<ExternalGroup>(results.getCount());
			while (results.hasMore()) {
				ExternalGroup group = convert(results.next());
				if (group != null) {
					groups.add(group);
				}
			}
		} catch (LDAPException ldape) {
			if (ldape.getResultCode() == LDAPException.SIZE_LIMIT_EXCEEDED) {
				throw new ExternalGroupException(Type.SIZE_LIMIT);
			} else {
				log.error("Problem with LDAP.", ldape);
			}
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
		return groups;
	}

	ExternalGroup convert(LDAPEntry entry) {
		String dn = entry.getDN();
		String name = null;
		for(String attributeName: getSearchAttributes() ) {
			LDAPAttribute attribute = entry.getAttribute(attributeName);
			if (attribute != null) {
				String[] names = attribute.getStringValueArray();
				if (names.length == 1) {
					name = names[0];
				} else {
					if (names.length == 0) {
						if (log.isDebugEnabled()) {
							log.debug("No names for: "+ dn);
						}
					} else {
						name = names[0];
						if (log.isDebugEnabled()) {
							log.debug("Found "+ names.length+ " for: "+ dn);
						}
					}
				}
			}
		}
		if (name == null) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to convert ldap entry: "+ entry);
			}
			return null;
		}
		return newExternalGroup(dn, name);
	}
	
	ExternalGroup newExternalGroup(String id, String name) {
		return new ExternalGroupImpl(id, name, this, userDirectoryService);
	}

	String[] getSearchAttributes() {
		return searchAttributes;
	}

	void ensureConnectionManager() {
		if (ldapConnectionManager == null) { 
			ldapConnectionManager = jldapDirectoryProvider.getLdapConnectionManager();
		}
	}
	
	/**
	 * Get an LDAP Connection.
	 * Attempt to get a connection manager, see if we have one locally or 
	 * get one from the directory provider. We can't do this in the init()
	 * as we then have an init() dependency between this and JLDAPDirectoryProvider.
	 * This is not a standard getter.
	 * @return A @{LdapConnection}.
	 * @throws LDAPException 
	 */
	LDAPConnection getConnection() throws LDAPException {
		ensureConnectionManager();
		LDAPConnection connection =  ldapConnectionManager.getConnection();
		LDAPSearchConstraints searchConstraints = connection.getSearchConstraints();
		searchConstraints.setMaxResults(maxResults);
		connection.setConstraints(searchConstraints);
		return connection;
	}

	void returnConnection(LDAPConnection connection) {
		ensureConnectionManager();
		ldapConnectionManager.returnConnection(connection);
	}

	public void setLdapConnectionManager(LdapConnectionManager ldapConnectionManager) {
		this.ldapConnectionManager = ldapConnectionManager;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setJldapDirectoryProvider(
			JLDAPDirectoryProvider jldapDirectoryProvider) {
		this.jldapDirectoryProvider = jldapDirectoryProvider;
	}

	public void setMappedGroupDao(MappedGroupDao mappedGroupDao) {
		this.mappedGroupDao = mappedGroupDao;
	}

	public void setMemberFormat(String memberFormat) {
		this.memberFormat = memberFormat;
	}

	public void setGroupBase(String groupBase) {
		this.groupBase = groupBase;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public void setDisplayNames(Map<String, String> displayNames) {
		this.displayNames = displayNames;
	}

	Collection<String> findMembers(String externalId) {
		Collection<String> users = Collections.emptyList();
		LDAPConnection connection = null;
		try {
			connection = getConnection();
			LDAPSearchResults results = connection.search(externalId, LDAPConnection.SCOPE_BASE, null, new String[]{memberAttribute}, false);
			while(results.hasMore()) {
				LDAPEntry entry = results.next();
				LDAPAttribute memberAttr = entry.getAttribute(memberAttribute);
				if (memberAttr == null) {
					continue;
				}
				String[] members = memberAttr.getStringValueArray();
				
				MessageFormat formatter = new MessageFormat(memberFormat);
				users = new ArrayList<String>(members.length);
				
				for(String member: members) {
					//oakPrimaryPersonID=21096,ou=people,dc=oak,dc=ox,dc=ac,dc=uk
					Object[] parseValues = formatter.parse(member);
					if(parseValues.length == 1) {
						String eid = (String) parseValues[0];
						users.add(eid);
					} else {
						log.warn("Failed to parse member of group: "+ member);
					}
				}
			}
		} catch (LDAPException ldape) {
			log.warn("Failed to find members of: "+ externalId, ldape);
		} catch (ParseException e) {
			log.error("Error in formatter, can't load members. "+ e.getMessage());
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
		return users;
	}
	
	// Taken from the provider LDAP stuff.
	public String escapeSearchFilterTerm(String term) {
		if (term == null)
			return null;
		// From RFC 2254
		String escapedStr = new String(term);
		escapedStr = escapedStr.replaceAll("\\\\", "\\\\5c");
		escapedStr = escapedStr.replaceAll("\\*", "\\\\2a");
		escapedStr = escapedStr.replaceAll("\\(", "\\\\28");
		escapedStr = escapedStr.replaceAll("\\)", "\\\\29");
		escapedStr = escapedStr.replaceAll("\\" + Character.toString('\u0000'),
				"\\\\00");
		return escapedStr;
	}

	public List<ExternalGroupNode> findNodes(String path)
			throws ExternalGroupException {
		if (path == null) {
			path = "";
		}
		String parts[] = path.split(PathHandler.SEPARATOR);
		for (PathHandler handler: pathHandlers) {
			if (handler.canHandle(parts)) {
				return handler.getNodes(parts);
			}
		}
		return null;
	}

}
