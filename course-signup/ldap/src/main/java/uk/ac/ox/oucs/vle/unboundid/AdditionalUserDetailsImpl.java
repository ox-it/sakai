/*
 * #%L
 * Course Signup Ldap
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle.unboundid;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import org.sakaiproject.unboundid.UnboundidDirectoryProvider;
import uk.ac.ox.oucs.vle.AdditionalUserDetails;

import java.text.MessageFormat;

import static com.unboundid.ldap.sdk.SearchScope.ONE;

/**
 * This load additional details about the user from LDAP
 */
public class AdditionalUserDetailsImpl implements AdditionalUserDetails {

	private String groupBase = "cn=courses,dc=oak,dc=ox,dc=ac,dc=uk";

	private String memberFormat = "oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk";

	private String displayNameAttribute = "displayName";

	private String memberAttribute = "member";

	private LDAPConnectionPool ldapPool;

	private UnboundidDirectoryProvider unboundidDirectoryProvider;

	public void setLdapPool(LDAPConnectionPool ldapPool) {
		this.ldapPool = ldapPool;
	}

	public void setUnboundidDirectoryProvider(UnboundidDirectoryProvider unboundidDirectoryProvider) {
		this.unboundidDirectoryProvider = unboundidDirectoryProvider;
	}

	public void init() {
		if (ldapPool == null && unboundidDirectoryProvider != null) {
			ldapPool = unboundidDirectoryProvider.getConnectionPool();
		}
	}

	public String getDegreeProgram(String userId) {

		MessageFormat formatter = new MessageFormat(memberFormat);
		String member = formatter.format(new Object[]{userId});
		String degreeProgram = null;

		try (LDAPConnection connection = ldapPool.getConnection()){
			String filter = memberAttribute+ "="+member;
			SearchRequest request = new SearchRequest(groupBase, ONE, filter, displayNameAttribute);
			request.setSizeLimit(1000);
			SearchResult results = connection.search(request);

			for (SearchResultEntry entry :results.getSearchEntries()){
				Attribute attribute = entry.getAttribute(displayNameAttribute);
				if (attribute != null) {
					String[] names = attribute.getValues();
					if (names.length > 0) {
						degreeProgram = names[0];
					}
				}
			}
			return degreeProgram;

		} catch (LDAPException ldape) {
			//log.error("Problem with LDAP.", ldape);

		}
		return null;
	}
}
