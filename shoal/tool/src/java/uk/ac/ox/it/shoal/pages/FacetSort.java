package uk.ac.ox.it.shoal.pages;

/**
 * Holder for how we should sort data in facets.
 * Class is imutable.
 */
public class FacetSort {

	/** Are we sorting ascending or descending */
	public enum Order {ASC, DESC};
	/** What are we sorting on */
	public enum On {VALUE, COUNT};
	
	private final Order order;
	private final On on;
	
	public FacetSort(On on, Order order) {
		this.on = on;
		this.order = order;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public On getOn() {
		return on;
	}
	
}
