package uk.ac.ox.oucs.vle;

/**
 * Interface for nodes in a browseable tree of groups.
 * @author buckett
 *
 */
public interface ExternalGroupNode {

	public String getPath();
	
	public String getName();
	
	public boolean hasGroup();
	
	public ExternalGroup getGroup();

}
