package uk.ac.ox.oucs.vle;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;

@FunctionalInterface
public interface SearchBuilder {

    SearchRequest create(String[] path) throws LDAPException;
}
