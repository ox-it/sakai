package uk.ac.ox.oucs.oxam.pages;

public class FacetSort {

	public static enum Order {ASC, DESC};
	public static enum On {VALUE, COUNT};
	
	private Order order;
	private On on;
	
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
