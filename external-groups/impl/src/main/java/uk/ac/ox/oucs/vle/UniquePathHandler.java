package uk.ac.ox.oucs.vle;

import java.util.*;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPSearchResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.vle.ExternalGroupException.Type;


public class UniquePathHandler implements PathHandler{

	private static Log log = LogFactory.getLog(UniquePathHandler.class);

	private String base;

	private String root;

	private String attribute;
	
	private String displayAttribute;

	private DisplayAdjuster displayAdjuster;

	// TODO Refactor out an interface.
	private ExternalGroupManagerImpl groupManager;

	public UniquePathHandler(String base, String root, String attribute, String displayAttribute, ExternalGroupManagerImpl groupManager) {
		this.base = base;
		this.root = root;
		this.attribute = attribute;
		this.displayAttribute = displayAttribute;
		this.groupManager = groupManager;
	}

	
	public void setDisplayAdjuster(DisplayAdjuster displayAdjuster) {
		this.displayAdjuster = displayAdjuster;
	}	

	public boolean canHandle(String[] path) {
		return path.length == 1 && root.equals(path[0]);
	}

	public List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException {
		if (!canHandle(path)) 
			throw new IllegalArgumentException("Can't handle this path: "+ path);
		try (LDAPConnection conn = groupManager.getConnection()){
			String filter = attribute + "=*";
			if (root.equals(ExternalGroupManagerImpl.COURSES)) {
				filter = groupManager.getCourseOwnerFilter();
			}
			SearchRequest searchRequest = new SearchRequest(base, SearchScope.SUB, filter, attribute, displayAttribute);
			searchRequest.setSizeLimit(groupManager.getSizeLimit());
			SearchResult searchResults = conn.search(searchRequest);
			Set<String> names = new HashSet<String>();
			Map<String,String> displayNames = new HashMap<String,String>();
			for(SearchResultEntry result: searchResults.getSearchEntries()) {
				String name = result.getAttribute(attribute).getValue();
				if (names.add(name)) {
					String displayName = adjustedDisplayName(result.getAttribute(displayAttribute).getValue());
					displayNames.put(name, displayName);
				}
			}
			
			List<ExternalGroupNode> nodes = new ArrayList<ExternalGroupNode>(names.size());
			String pathPrefix = path[0] + PathHandler.SEPARATOR;
			for (String name : names) {
				nodes.add(new ExternalGroupNodeImpl(pathPrefix +name, displayNames.get(name)));
			}
			return nodes;
		} catch (LDAPException lde) {
			log.error("Failed to get nodes for path: "+ Arrays.toString(path), lde);
			throw new ExternalGroupException(Type.UNKNOWN);
		}
	}

	private String adjustedDisplayName(String name) {
		if (displayAdjuster != null) {
			return displayAdjuster.adjustDisplayName(name);
		}
		return name;
	}

}
