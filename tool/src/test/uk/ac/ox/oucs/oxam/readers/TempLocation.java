package uk.ac.ox.oucs.oxam.readers;

import java.io.File;
import java.io.IOException;

import uk.ac.ox.oucs.oxam.logic.Location;

public class TempLocation implements Location {

	private File temp;

	public TempLocation() throws IOException {
		temp = File.createTempFile(getClass().getSimpleName(), Long.toString(System.nanoTime()));
		temp.delete();
		temp.mkdir();
	}
	
	public String getPrefix() {
		return "";
	}

	public String getPath(String path) {
		return temp.getAbsolutePath()+ path;
	}

}
