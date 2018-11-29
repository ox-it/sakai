package uk.ac.ox.oucs.vle;

import java.util.Objects;

public class ExternalGroupNodeImpl implements ExternalGroupNode {

	private String path;
	private String name;
	// Can be null.
	private ExternalGroup group;

	ExternalGroupNodeImpl(String path, String name) {
		this.path = path;
		this.name = name;
	}

	ExternalGroupNodeImpl(String path, String name, ExternalGroup group) {
		this.path = path;
		this.name = name;
		this.group = group;
	}

	public ExternalGroup getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public boolean hasGroup() {
		return group != null;
	}
}
