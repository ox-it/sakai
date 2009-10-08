package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.vle.ExternalGroupException.Type;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public class AttributePathHandler implements PathHandler{

	private static Log log = LogFactory.getLog(AttributePathHandler.class);

	private String base;

	private String root;

	private String attribute;
	
	private String displayAttribute;
	
	private String searchAttribute;

	private DisplayAdjuster displayAdjuster;
	
	// TODO Refactor out an interface.
	private ExternalGroupManagerImpl groupManager;

	public AttributePathHandler(String base, String root, String searchAttribute, String attribute, String displayAttribute, ExternalGroupManagerImpl groupManager) {
		this.base = base;
		this.root = root;
		this.searchAttribute = searchAttribute;
		this.attribute = attribute;
		this.displayAttribute = displayAttribute;
		this.groupManager = groupManager;
	}

	public boolean canHandle(String[] path) {
		return path.length == 2 && root.equals(path[0]);
	}

	public List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException {
		if (!canHandle(path)) 
			throw new IllegalArgumentException("Can't handle this path: "+ path);
		LDAPConnection conn = null;
		try {
			conn = groupManager.getConnection();
			LDAPSearchResults searchResults = conn.search(base, LDAPConnection.SCOPE_ONE, searchAttribute+ "="+ path[1], new String[]{attribute, displayAttribute}, false);
			List<ExternalGroupNode> nodes = new ArrayList<ExternalGroupNode>();
			String pathPrefix = path[0]+ PathHandler.SEPARATOR + path[1]+ PathHandler.SEPARATOR;
			while (searchResults.hasMore()) {
				LDAPEntry result = searchResults.next();
				String name = result.getAttribute(attribute).getStringValue();
				String displayName = adjustedDisplayName(result.getAttribute(displayAttribute).getStringValue());
				nodes.add(new ExternalGroupNodeImpl(pathPrefix +name, displayName));
			}
			return nodes;
		} catch (LDAPException lde) {
			log.warn(lde);
			throw new ExternalGroupException(Type.UNKNOWN);
		} finally {
			groupManager.returnConnection(conn);
		}
	}

	private String adjustedDisplayName(String name) {
		if (displayAdjuster != null) {
			return displayAdjuster.adjustDisplayName(name);
		}
		return name;
	}

}
