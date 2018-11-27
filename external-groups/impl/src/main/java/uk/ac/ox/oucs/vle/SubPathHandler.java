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
 * This find all groups below a base that have members.
 */
public class SubPathHandler implements PathHandler{

	private static Log log = LogFactory.getLog(SubPathHandler.class);

	private String root;

	private SearchBuilder builder;
	private EntryMapper mapper;

	// TODO Refactor out an interface.
	private ExternalGroupManagerImpl groupManager;

	public SubPathHandler(String root, ExternalGroupManagerImpl groupManager, SearchBuilder builder, EntryMapper mapper) {
		this.root = root;
		this.groupManager = groupManager;
		this.builder = builder;
		this.mapper = mapper;
	}	

	public boolean canHandle(String[] path) {
		return (path.length == 3  || path.length == 4) && root.equals(path[0]);
	}

	public List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException {
		if (!canHandle(path)) 
			throw new IllegalArgumentException("Can't handle this path: "+ path);
		try {
			SearchRequest searchRequest = builder.create(path);
			searchRequest.setSizeLimit(groupManager.getSizeLimit());
			SearchResult searchResults = groupManager.getLdapConnectionPool().search(searchRequest);
			List<ExternalGroupNode> nodes = new ArrayList<>();
			for (SearchResultEntry result : searchResults.getSearchEntries()) {
				ExternalGroupNode node = mapper.map(path, result);
				if (node != null) {
					nodes.add(node);
				}
			}
			return nodes;
		} catch (LDAPException lde) {
			log.warn(lde);
			throw new ExternalGroupException(Type.UNKNOWN, lde);
		}
	}

}
