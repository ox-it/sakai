package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.List;

public class StaticPathHandler implements PathHandler {

	private List<ExternalGroupNode> nodes;

	public StaticPathHandler(List<ExternalGroupNode> nodes) {
		this.nodes = nodes;
	}

	public boolean canHandle(String[] path) {
		return path.length == 0 || (path.length == 1 && path[0].length() == 0);
	}

	public List<ExternalGroupNode> getNodes(String[] path)
			throws ExternalGroupException {
		return new ArrayList<ExternalGroupNode>(nodes);
	}

}
