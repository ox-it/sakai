package uk.ac.ox.oucs.vle;

import com.unboundid.ldap.sdk.SearchResultEntry;

@FunctionalInterface
public interface EntryMapper {
    ExternalGroupNode map(String[] path, SearchResultEntry entry);
}
