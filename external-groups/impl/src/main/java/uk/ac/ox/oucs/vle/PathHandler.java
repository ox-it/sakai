package uk.ac.ox.oucs.vle;

import java.util.List;

public interface PathHandler {
	
	String SEPARATOR = ":";

	boolean canHandle(String[] path);
	
	List<ExternalGroupNode> getNodes(String[] path) throws ExternalGroupException;

}
