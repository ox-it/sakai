package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;

public class MappedDisplayAdaptor implements DisplayAdjuster {

	private Map<String, String> names;
	
	public MappedDisplayAdaptor(Map <String, String> names) {
		this.names = new HashMap<>(names);
	}
	
	public String adjustDisplayName(String name) {
		String replacement = names.get(name);
		return (replacement == null)? name : replacement;
	}

}
