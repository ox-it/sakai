package uk.ac.ox.oucs.vle;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

import edu.amc.sakai.user.JLDAPDirectoryProvider;
import edu.amc.sakai.user.LdapConnectionManager;

public class ExternalGroupManagerImpl implements ExternalGroupManager {

	private static Log log = LogFactory.getLog(ExternalGroupManagerImpl.class);
	
	LdapConnectionManager ldapConnectionManager;
	
	JLDAPDirectoryProvider jldapDirectoryProvider;
	
	UserDirectoryService userDirectoryService;
	
	public void init() {
		log.debug("init()");
		if (ldapConnectionManager == null && jldapDirectoryProvider == null) {
			throw new IllegalStateException("Don't have a way of getting a LdapConnectionManager");
		}
		if (userDirectoryService == null) {
			throw new IllegalStateException("UserDirectoryService must be set.");
		}
			
	}

	public String addMappedGroup(String externalGroupId, String role) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findExternalGroupId(String mappedGroupId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ExternalGroup findExternalGroup(String externalGroupId) {
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
			if (LDAPException.NO_SUCH_OBJECT == ldape.getResultCode()) {
				log.debug("Didn't find group ID: "+ externalGroupId);
			} else {
				log.error("Problem with LDAP.", ldape);
			}
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
		return group;
	}

	public List<ExternalGroup> search(String query) {
		if (query == null || query.length() == 0) {
			return Collections.emptyList();
		}
		List<ExternalGroup> groups = null;
		LDAPConnection connection = null;
		try {
			connection = getConnection();
			String filter = "ou=*"+ query+ "*";
			LDAPSearchResults results = connection.search("ou=units,dc=oak,dc=ox,dc=ac,dc=uk", LDAPConnection.SCOPE_SUB, filter, getSearchAttributes(), false);
			groups = new ArrayList<ExternalGroup>(results.getCount());
			while (results.hasMore()) {
				ExternalGroup group = convert(results.next());
				if (group != null) {
					groups.add(group);
				}
			}
		} catch (LDAPException ldape) {
			log.error("Problem with LDAP.", ldape);
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
		return groups;
	}

	ExternalGroup convert(LDAPEntry entry) {
		String dn = entry.getDN();
		LDAPAttribute attribute = entry.getAttribute("ou");
		String name = null;
		if (attribute != null) {
			String[] names = attribute.getStringValueArray();
			if (names.length == 1) {
				name = names[0];
			} else {
				if (names.length == 0) {
					log.warn("No names for: "+ dn);
				} else {
					name = names[0];
					log.warn("Found "+ names.length+ " for: "+ dn);
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
		return new ExternalGroupImpl(id, name, this);
	}

	String[] getSearchAttributes() {
		return new String[]{"ou"};
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
		return ldapConnectionManager.getConnection();
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

	Iterator<User> findMembers(String externalId) {
		Collection<User> users = Collections.emptyList();
		LDAPConnection connection = null;
		try {
			connection = getConnection();
			LDAPSearchResults results = connection.search(externalId, LDAPConnection.SCOPE_BASE, null, new String[]{"member"}, false);
			while(results.hasMore()) {
				LDAPEntry entry = results.next();
				LDAPAttribute memberAttr = entry.getAttribute("member");
				String[] members = memberAttr.getStringValueArray();
				
				MessageFormat formatter = new MessageFormat("oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk");
				users = new ArrayList<User>(members.length);
				
				for(String member: members) {
					//oakPrimaryPersonID=21096,ou=people,dc=oak,dc=ox,dc=ac,dc=uk
					Object[] parseValues = formatter.parse(member);
					if(parseValues.length == 1) {
						String eid = (String) parseValues[0];
						try {
							User user = userDirectoryService.getUserByEid(eid);
							users.add(user);
						} catch (UserNotDefinedException e) {
							if (log.isInfoEnabled()) {
								log.info("Failed to find user ("+ eid+ ") for group ("+ externalId+ ")");
							}
						}
					} else {
						log.warn("Failed to parse member of group: "+ member);
					}
				}
				
			}
		} catch (LDAPException ldape) {
			
		} catch (ParseException e) {
			log.error("Error in formatter, can't load members. "+ e.getMessage());
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
		return users.iterator();
	}

}
