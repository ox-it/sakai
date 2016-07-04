package uk.ac.ox.oucs.vle;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.vle.ExternalGroupException.Type;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

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
		LDAPConnection conn = null;
		try {
			conn = groupManager.getConnection();
			String filter = attribute + "=*";
			if (root.equals(ExternalGroupManagerImpl.COURSES)) {
				filter = groupManager.getCourseOwnerFilter();
			}
			LDAPSearchResults searchResults = conn.search(base, LDAPConnection.SCOPE_SUB, filter, new String[]{attribute, displayAttribute}, false);
			Set<String> names = new HashSet<String>();
			Map<String,String> displayNames = new HashMap<String,String>();
			while (searchResults.hasMore()) {
				LDAPEntry result = searchResults.next();
				String name = result.getAttribute(attribute).getStringValue();
				if (names.add(name)) {
					String displayName = adjustedDisplayName(result.getAttribute(displayAttribute).getStringValue());
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
