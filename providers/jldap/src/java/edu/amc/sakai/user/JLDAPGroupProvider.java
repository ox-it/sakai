package edu.amc.sakai.user;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupProvider;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

/**
 * This is a GroupProvider that uses LDAP for it's lookups.
 * @author buckett
 *
 */
public class JLDAPGroupProvider implements GroupProvider {

	// We just ask for the DN when looking for a group
	private static final String[] DN_ATTR = new String[]{"dn"};

	private static final Log log = LogFactory.getLog(JLDAPDirectoryProvider.class);

	private LdapConnectionManager connectionManager;
	private ProvidedGroupManager groupManager;
	private JLDAPDirectoryProvider jldapDirectoryProvider;

	private String searchBase = "ou=units,dc=oak,dc=ox,dc=ac,dc=uk";
	private String memberAttribute = "member";
	private String personIdPattern = "oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk";

	
	public void init() {
		// Want to share the connection manager.
		setConnectionManager(jldapDirectoryProvider.getLdapConnectionManager());
	}
	
	public ProvidedGroup createGroup(String dn, String role) {
		return groupManager.newGroup(dn, role);
	}
	
	public boolean groupExists(String groupId) {
		ProvidedGroup group = groupManager.getGroup(groupId);
		return group != null;
	}
	
	public Map getGroupRolesForUser(String eid) {
		// Do subtree search on LDAP, then map to groups using the group manager.
		String personId = MessageFormat.format(personIdPattern, eid);
		String filter = memberAttribute+ "="+personId;
		LDAPConnection connection = null;
		try {
			connection = connectionManager.getConnection();
			LDAPSearchResults results = connection.search(searchBase, LDAPConnection.SCOPE_SUB, filter, DN_ATTR, false);
			HashMap<String, String> groupRoles = new HashMap<String, String>();
			while (results.hasMore()) {
				LDAPEntry resultEntry = results.next();
				String dn = resultEntry.getDN();
				Set<ProvidedGroup> groups = groupManager.getGroupByDNs(dn);
				if (groups != null) {
					for (ProvidedGroup group: groups) {
						groupRoles.put(group.getId(), group.getRole());
					}
				}
			}
			return groupRoles;
		} catch (LDAPException e) {
			log.warn("Problem getting groups for "+ eid, e);
		} finally {
			if (connection != null) {
				connectionManager.returnConnection(connection);
			}
		}
		return null;
	}

	public String getRole(String groupId, String eid) {
		ProvidedGroup group = groupManager.getGroup(groupId);
		if (group != null) {
			return group.getRole();
		}
		return null;
	}

	public Map getUserRolesForGroup(String groupId) {
		//TODO Need to unpack the groupId?
		ProvidedGroup group = groupManager.getGroup(groupId);
		if (group != null) {
			String dn = group.getDn();
			LDAPConnection connection = null;
			try {
				connection = connectionManager.getConnection();
				LDAPSearchResults results = connection.search(dn, LDAPConnection.SCOPE_ONE, null, new String[]{memberAttribute}, false);
				Map userRoles = new HashMap();;
				while (results.hasMore()) {
					LDAPEntry result = results.next();
					LDAPAttribute member = result.getAttribute(memberAttribute);
					Enumeration values = member.getStringValues();
					MessageFormat message = new MessageFormat(personIdPattern);
					while (values.hasMoreElements()) {
						String value = (String) values.nextElement();
						try {
							Object[] personIds = message.parse(value);
							if (personIds.length == 1) {
								userRoles.put(personIds[0], group.getRole());
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
				return userRoles;
			} catch (LDAPException e) {
				log.warn("Problem finding user roles for group: "+ groupId, e);
			} finally {
				if (connection != null) {
					connectionManager.returnConnection(connection);
				}
			}
		}
		return Collections.EMPTY_MAP;
	}

	public String packId(String[] ids) {
		return ids[0];
	}

	public String preferredRole(String one, String other) {
		return one;
	}

	public String[] unpackId(String id) {
		return new String[]{id};
	}

	public void setConnectionManager(LdapConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public LdapConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public JLDAPDirectoryProvider getJldapDirectoryProvider() {
		return jldapDirectoryProvider;
	}

	public void setJldapDirectoryProvider(
			JLDAPDirectoryProvider jldapDirectoryProvider) {
		this.jldapDirectoryProvider = jldapDirectoryProvider;
	}

	public ProvidedGroupManager getGroupManager() {
		return groupManager;
	}

	public void setGroupManager(ProvidedGroupManager groupManager) {
		this.groupManager = groupManager;
	}

}
