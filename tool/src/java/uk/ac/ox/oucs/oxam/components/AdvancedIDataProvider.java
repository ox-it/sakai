package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * This is a hack as the SolrProvider needs to know which items it should be getting
 * even when the size() method is called, otherwise it ends up doing two searches when one would do.
 * As lots of setters are final there isn't a nice way todo this.
 * @author buckett
 *
 * @param <T>
 */
public interface AdvancedIDataProvider<T> extends IDataProvider<T> {

	public void setFirst(int first);
	public void setCount(int count);
	
}
