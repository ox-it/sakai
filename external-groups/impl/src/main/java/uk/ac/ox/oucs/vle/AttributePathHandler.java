package uk.ac.ox.oucs.vle;

import java.util.*;

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

	private String attribute2;

	private String displayAttribute;
	
	private String searchAttribute;

	private DisplayAdjuster displayAdjuster;
	
	// TODO Refactor out an interface.
	private ExternalGroupManagerImpl groupManager;

	public AttributePathHandler(String base, String root, String searchAttribute, String attribute, String attribute2, String displayAttribute, ExternalGroupManagerImpl groupManager) {
		this.base = base;
		this.root = root;
		this.searchAttribute = searchAttribute;
		this.attribute = attribute;
		this.attribute2 = attribute2;
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
			LDAPSearchResults searchResults = conn.search(base, LDAPConnection.SCOPE_SUB, searchAttribute+ "="+ path[1], new String[]{attribute, attribute2, displayAttribute}, false);
			List<ExternalGroupNode> nodes = new ArrayList<ExternalGroupNode>();
			String pathPrefix = getPathPrefix(path);
			while (searchResults.hasMore()) {
				LDAPEntry result = searchResults.next();
				String name = result.getAttribute(attribute).getStringValue();
				String name2 = "";
				if (root.equals(ExternalGroupManagerImpl.COURSES)){
					name2 = ":" + result.getAttribute(attribute2).getStringValue();
				}
				String displayName = adjustedDisplayName(result.getAttribute(displayAttribute).getStringValue());
				nodes.add(new ExternalGroupNodeImpl(pathPrefix + name + name2, displayName));
			}
			return nodes;
		} catch (LDAPException lde) {
			log.warn(lde);
			throw new ExternalGroupException(Type.UNKNOWN);
		} finally {
			groupManager.returnConnection(conn);
		}
	}

	private String getPathPrefix(String[] path) {
		String pathPrefix = path[0]+ PathHandler.SEPARATOR;
		if (root.equals(ExternalGroupManagerImpl.UNITS) && ExternalGroupManagerImpl.OXUNI_SUB_FOLDERS.contains(path[1])){
			pathPrefix = pathPrefix + ExternalGroupManagerImpl.OXUNI + PathHandler.SEPARATOR;
		}
		pathPrefix = pathPrefix + path[1]+ PathHandler.SEPARATOR;
		return pathPrefix;
	}

	private String adjustedDisplayName(String name) {
		if (displayAdjuster != null) {
			return displayAdjuster.adjustDisplayName(name);
		}
		return name;
	}

}
