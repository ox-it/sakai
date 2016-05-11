package uk.ac.ox.oucs.oxam.readers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
	
	public void destroy() {
		Queue<File> directories = new LinkedList<File>();
		if (temp.exists()) {
			directories.add(temp);
			
			while(!directories.isEmpty()) {
				File dir = directories.peek();
				for (File file: dir.listFiles()) {
					if(file.isDirectory()) {
						directories.add(file); // Push nested folder
						break;
					}
					file.delete();
				}
				dir.delete();
				directories.poll(); // Remove the now empty folder.
			}
		}
	}

}
