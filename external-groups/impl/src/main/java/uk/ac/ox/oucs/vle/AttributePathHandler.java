package uk.ac.ox.oucs.vle;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ox.oucs.vle.ExternalGroupException.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * This looks for all nodes that match a particular attribute from the base.
 */
public class AttributePathHandler implements PathHandler{

	private static Log log = LogFactory.getLog(AttributePathHandler.class);

	private String root;

	private EntryMapper mapper;

	private SearchBuilder builder;

	// TODO Refactor out an interface.
	private ExternalGroupManagerImpl groupManager;

	public AttributePathHandler(String root, ExternalGroupManagerImpl groupManager, SearchBuilder builder, EntryMapper mapper) {
		this.root = root;
		this.groupManager = groupManager;
		this.mapper = mapper;
		this.builder = builder;
	}

	public boolean canHandle(String[] path) {
		return path.length == 2 && root.equals(path[0]);
	}

	public List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException {
		if (!canHandle(path)) 
			throw new IllegalArgumentException("Can't handle this path: "+ path);
		try {
			SearchRequest searchRequest = builder.create(path);
			SearchResult searchResults = groupManager.getLdapConnectionPool().search(searchRequest);
			List<ExternalGroupNode> nodes = new ArrayList<>();
			for(SearchResultEntry result : searchResults.getSearchEntries()) {
				nodes.add(mapper.map(path, result));
			}
			return nodes;
		} catch (LDAPException lde) {
			log.warn(lde);
			throw new ExternalGroupException(Type.UNKNOWN);
		}
	}

}
