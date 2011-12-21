package uk.ac.ox.oucs.oxam.dao;

import uk.ac.ox.oucs.oxam.logic.Location;

public class FakeLocation implements Location {

	public String getPrefix() {
		return "";
	}

	public String getPath(String path) {
		return path;
	}

}
