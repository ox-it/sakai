package uk.ac.ox.oucs.oxam.model;

import java.io.Serializable;

public class Term  implements Serializable{

	private static final long serialVersionUID = 1L;
	private final String code;
	private final String name;
	private final int orderInYear;
	private boolean inSecondYear;
	
	/**
	 * Create a new term. This is exposed so that tests don't have to use the service.
	 * @param code The code for the term (eg T).
	 * @param name The full name for the term (eg Trinity).
	 * @param orderInYear The position of the term in the year, this is used for sorting the terms.
	 * @param inSecondYear <code>true</code> if the term is in the second year of the academic year.
	 */
	public Term(String code, String name, int orderInYear, boolean inSecondYear) {
		this.code = code;
		this.name = name;
		this.orderInYear = orderInYear;
		this.inSecondYear = inSecondYear;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	
	public boolean inSecondYear() {
		return inSecondYear;
	}
	
	public int getOrderInYear() {
		return orderInYear;
	}

	@Override
	public String toString() {
		return "Term [code=" + code + ", name=" + name +  ", inSecondYear="+ inSecondYear+"]";
	}

}
