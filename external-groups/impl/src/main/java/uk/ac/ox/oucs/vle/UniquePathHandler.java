package uk.ac.ox.oucs.vle;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ox.oucs.vle.ExternalGroupException.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UniquePathHandler implements PathHandler {

    private static Log log = LogFactory.getLog(UniquePathHandler.class);

    private String root;

    private EntryMapper mapper;

    private SearchBuilder builder;

    // TODO Refactor out an interface.
    private ExternalGroupManagerImpl groupManager;

    public UniquePathHandler(String root, ExternalGroupManagerImpl groupManager, SearchBuilder builder, EntryMapper mapper) {
        this.root = root;
        this.groupManager = groupManager;
        this.mapper = mapper;
        this.builder = builder;
    }

    public boolean canHandle(String[] path) {
        return path.length == 1 && root.equals(path[0]);
    }

    public List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException {
        if (!canHandle(path))
            throw new IllegalArgumentException("Can't handle this path: " + path);
        try {
            SearchRequest searchRequest = builder.create(path);
            searchRequest.setSizeLimit(groupManager.getSizeLimit());
            SearchResult searchResults = groupManager.getLdapConnectionPool().search(searchRequest);
            Map<String, ExternalGroupNode> nodes = new HashMap<>();

            for (SearchResultEntry result : searchResults.getSearchEntries()) {
                ExternalGroupNode map = mapper.map(path, result);
                nodes.putIfAbsent(map.getName(), map);
            }
            return new ArrayList<>(nodes.values());
        } catch (LDAPException lde) {
            log.error("Failed to get nodes for path: " + Arrays.toString(path), lde);
            throw new ExternalGroupException(Type.UNKNOWN);
        }
    }

}
