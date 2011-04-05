package uk.ac.ox.oucs.vle;

public class SubUnitImpl implements SubUnit {
	
	private String code;
	private String name;
	
	public SubUnitImpl(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

}
