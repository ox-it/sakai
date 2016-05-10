package uk.ac.ox.oucs.vle;

import java.util.List;

public interface PathHandler {
	
	public String SEPARATOR = ":";

	public boolean canHandle(String[] path);
	
	public List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException;

}
