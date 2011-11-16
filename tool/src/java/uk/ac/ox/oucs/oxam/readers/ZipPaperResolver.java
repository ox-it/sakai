package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Term;

public class ZipPaperResolver implements PaperResolver {
	
	//Always use / with zipfiles
	protected static final String ZIP_SEPERATOR = "/";
	protected final TermService termService;
	protected String extension;
	protected ZipFile zipFile;
	protected String zipPrefix;

	public ZipPaperResolver(String filePath, String zipPrefix, TermService termService, String extension) throws IOException {
		zipFile = new ZipFile(filePath);
		this.termService = termService;
		this.extension = extension;
		this.zipPrefix = zipPrefix;
	}
	
	public PaperResolutionResult getPaper(int year, String termCode, String paperCode) {
		// The terms 
		Term term = termService.getByCode(termCode);
		if (term != null && paperCode != null) {
			
			String filename = zipPrefix+ ZIP_SEPERATOR+ year+ ZIP_SEPERATOR+ term.getName().toLowerCase()+ ZIP_SEPERATOR+ paperCode.toLowerCase()+ "."+ extension;
			return new Result(filename);
		}
		return null;
	}
	
	public class Result implements PaperResolutionResult {
		
		private ZipEntry entry;
		private String path;
		
		public Result(String filename) {
			this.path = filename; // As if the entry isn't found we won't know the path.
			this.entry = zipFile.getEntry(filename);
		}

		public boolean isFound() {;
			return entry != null;
		}

		public InputStream getStream() {
			try {
				if (entry != null) {
					return zipFile.getInputStream(entry);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public String getPath() {
			return path;
		}

		
	}

}
