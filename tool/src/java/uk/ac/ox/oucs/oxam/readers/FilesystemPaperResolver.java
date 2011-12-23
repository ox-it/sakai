package uk.ac.ox.oucs.oxam.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ox.oucs.oxam.Utils;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Term;

/**
 * This class is not thread safe.
 * @author buckett
 *
 */
public class FilesystemPaperResolver implements PaperResolver {
	
	private final String directory;
	private final TermService termService;
	private String extension;
	private Map<String, Result> cached = new HashMap<String, Result>(); // We want to cache for the MD5 calculations.

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
			Result result;
			if (cached.containsKey(filename)) {
				result = cached.get(filename);
			} else {
				result = new Result(filename);
				cached.put(filename, result);
			}
			return result;
		}
		return null;
	}
	
	public class Result implements PaperResolutionResult {
		
		private File file;
		private String md5;
		
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

		public String[] getPaths() {
			return new String[]{file.getAbsolutePath()};
		}

		public String getMD5() {
			if (md5 == null) {
				InputStream in = null;
				try {
					md5 = Utils.getMD5(in);
				} finally { 
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}
			}
			return md5;
		}

	}

}
