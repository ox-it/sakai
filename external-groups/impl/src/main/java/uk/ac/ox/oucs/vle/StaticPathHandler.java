package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.List;

/**
 * This path handler doesn't lookup anything, it just returns predefined nodes.
 * This is useful for the root of the tree.
 */
public class StaticPathHandler implements PathHandler {

	private List<ExternalGroupNode> nodes;

	public StaticPathHandler(List<ExternalGroupNode> nodes) {
		this.nodes = nodes;
	}

	public boolean canHandle(String[] path) {
		return path.length == 0 || (path.length == 1 && path[0].length() == 0);
	}

	public List<ExternalGroupNode> getNodes(String[] path) {
		return new ArrayList<>(nodes);
	}

}
