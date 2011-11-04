package uk.ac.ox.oucs.oxam.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Term;

public class FilesystemPaperResolver implements PaperResolver {
	
	private final String directory;
	private final TermService termService;
	private String extension;

	public FilesystemPaperResolver(String directory, TermService termService, String extension) {
		// Trim trailing separators.
		while(directory.endsWith(File.separator) && directory.length() >0) {
			directory = directory.substring(0, directory.length()-1);
		}
		this.directory = directory;
		this.termService = termService;
		this.extension = extension;
	}
	
	public PaperResolutionResult getPaper(int year, String termCode, String paperCode) {
		// The terms 
		Term term = termService.getByCode(termCode);
		if (term != null && paperCode != null) {
			String filename = directory + File.separator+ year+ File.separator+ term.getName().toLowerCase()+ File.separator+ paperCode.toLowerCase()+ "."+ extension;
			return new Result(filename);
		}
		return null;
	}
	
	public class Result implements PaperResolutionResult {
		
		private File file;
		
		public Result(String filename) {
			this.file = new File(filename);
		}

		public boolean isFound() {;
			return file.exists();
		}

		public InputStream getStream() {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// The calling code should check the file exist first.
			}
			return null;
		}

		public String getPath() {
			return file.getAbsolutePath();
		}

		
	}

}
