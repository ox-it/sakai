package uk.ac.ox.oucs.vle;

import java.text.MessageFormat;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;

import edu.amc.sakai.user.JLDAPDirectoryProvider;
import edu.amc.sakai.user.LdapConnectionManager;

public class AdditionalUserDetailsImpl implements AdditionalUserDetails {
	
	private String groupBase = "cn=courses,dc=oak,dc=ox,dc=ac,dc=uk";
	
	private String memberFormat = "oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk";
	
	private String displayNameAttribute = "displayName";

	private String memberAttribute = "member";
	
	LdapConnectionManager ldapConnectionManager;
	public void setLdapConnectionManager(LdapConnectionManager ldapConnectionManager) {
		this.ldapConnectionManager = ldapConnectionManager;
	}
	
	JLDAPDirectoryProvider jldapDirectoryProvider;
	public void setJldapDirectoryProvider(
			JLDAPDirectoryProvider jldapDirectoryProvider) {
		this.jldapDirectoryProvider = jldapDirectoryProvider;
	}
	
	public void init() {
		// TODO Auto-generated method stub	
	}
	
	public String getDegreeProgram(String userId) {
		
		MessageFormat formatter = new MessageFormat(memberFormat);
		String member = formatter.format(new Object[]{userId});
		LDAPConnection connection = null;
		String degreeProgram = null;
		
		try {
			connection = getConnection();
			String filter = memberAttribute+ "="+member;
			LDAPSearchResults results = 
					connection.search(groupBase, LDAPConnection.SCOPE_ONE, filter, 
									  new String[]{displayNameAttribute}, false);
			
			while (results.hasMore()) {
				LDAPEntry entry = results.next();
				LDAPAttribute attribute = entry.getAttribute(displayNameAttribute);
				if (attribute != null) {
					String[] names = attribute.getStringValueArray();
					if (names.length > 0) {
						degreeProgram = names[0];
					}
				}
			}
			return degreeProgram;
			
		} catch (LDAPException ldape) {
			//log.error("Problem with LDAP.", ldape);
			
		} finally {
			if (connection != null) {
				returnConnection(connection);
			}
		}
		
		return null;
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
		searchConstraints.setMaxResults(1000);
		connection.setConstraints(searchConstraints);
		return connection;
	}

	void returnConnection(LDAPConnection connection) {
		ensureConnectionManager();
		ldapConnectionManager.returnConnection(connection);
	}

	
}
