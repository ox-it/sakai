package pom.model;

import java.io.Serializable;

/**
 * An example thing
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class Thing implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private String name;

	public Thing() {
		
	}
	
	public Thing(long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
