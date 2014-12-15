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

public class SubPathHandler implements PathHandler{

	private static Log log = LogFactory.getLog(SubPathHandler.class);

	private String base;

	private String root;

	private String attribute;
	
	private String displayAttribute;

	private DisplayAdjuster displayAdjuster;
	
	// TODO Refactor out an interface.
	private ExternalGroupManagerImpl groupManager;

	public SubPathHandler(String base, String root, String attribute, String displayAttribute, ExternalGroupManagerImpl groupManager) {
		this.base = base;
		this.root = root;
		this.attribute = attribute;
		this.displayAttribute = displayAttribute;
		this.groupManager = groupManager;
	}	

	public boolean canHandle(String[] path) {
		return (path.length == 3  || path.length == 4) && root.equals(path[0]);
	}

	public List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException {
		if (!canHandle(path)) 
			throw new IllegalArgumentException("Can't handle this path: "+ path);
		LDAPConnection conn = null;
		try {
			conn = groupManager.getConnection();
			String basePrefix = getBasePrefix(path);
			String pathSub1 = getPathSub1(path);
			String pathSub2 = getPathSub2(path);
			LDAPSearchResults searchResults = conn.search(basePrefix + String.format(base, pathSub1, pathSub2) , LDAPConnection.SCOPE_SUB, "(|(member=*)(cn=*suspended*))", new String[]{displayAttribute}, false);
			List<ExternalGroupNode> nodes = new ArrayList<ExternalGroupNode>();
			String pathPrefix = path[0]+ PathHandler.SEPARATOR + path[1]+ PathHandler.SEPARATOR + path[2] + PathHandler.SEPARATOR;
			while (searchResults.hasMore()) {
				LDAPEntry result = searchResults.next();
				String name = result.getDN();
				String displayName = adjustedDisplayName(result.getAttribute(displayAttribute).getStringValue());
				if ((root.equals(ExternalGroupManagerImpl.COURSES) && !name.startsWith(ExternalGroupManagerImpl.CN_GRADUAND) && !name.startsWith(ExternalGroupManagerImpl.CN_GRADUATE) && !name.startsWith(ExternalGroupManagerImpl.CN_TRANSFERRED)
				   && (name.contains("current")) && !name.matches("cn=\\d{4}-current,ou=y.*"))
						|| (root.equals(ExternalGroupManagerImpl.UNITS)
						&& (name.startsWith(ExternalGroupManagerImpl.ALL) || name.startsWith(ExternalGroupManagerImpl.ITSS) || name.startsWith(ExternalGroupManagerImpl.STAFF) || name.startsWith(ExternalGroupManagerImpl.STUDENTS) || name.startsWith(ExternalGroupManagerImpl.POSTGRADS)))) {
					nodes.add(new ExternalGroupNodeImpl(pathPrefix + name, displayName, new ExternalGroupImpl(result.getDN(), displayName, groupManager, groupManager.userDirectoryService)));
				}
			}
			return nodes;
		} catch (LDAPException lde) {
			log.warn(lde);
			throw new ExternalGroupException(Type.UNKNOWN);
		} finally {
			groupManager.returnConnection(conn);
		}
	}

	private String getPathSub1(String[] path) {
		if (root.equals(ExternalGroupManagerImpl.UNITS)){
			return path[1];
		}
		return path[2];
	}

	private String getPathSub2(String[] path) {
		if (root.equals(ExternalGroupManagerImpl.UNITS)){
			return path[1];
		}
		return path[3];
	}

	private String getBasePrefix(String[] path) {
		String basePrefix = "";
		if (root.equals(ExternalGroupManagerImpl.UNITS)) {
			int length = path.length - 1;
			if (path.length>3 && path[2]!=null && path[3]!=null && path[2].equals(ExternalGroupManagerImpl.CONTED) && path[3].equals(ExternalGroupManagerImpl.CONTED)) {
				length = path.length - 2;
			}
			for (int i = length; i > 0; i--) {
				basePrefix = basePrefix + "ou=" + path[i] + ",";
			}
		}
		return basePrefix;
	}

	private String adjustedDisplayName(String name) {
		if (displayAdjuster != null) {
			return displayAdjuster.adjustDisplayName(name);
		}
		return name;
	}

}
