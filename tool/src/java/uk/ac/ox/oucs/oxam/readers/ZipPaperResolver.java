package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import uk.ac.ox.oucs.oxam.Utils;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Term;

/**
 * This looks for paper files in a zipfile.
 * @author buckett
 *
 */
public class ZipPaperResolver implements PaperResolver {
	
	//Always use / with zipfiles
	protected static final String ZIP_SEPERATOR = "/";
	protected final TermService termService;
	protected String extension;
	protected ZipFile zipFile;
	protected String zipPrefix;
	protected Map<String, Result> cached = new HashMap<String, Result>();

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
			Result result;
			if (cached.containsKey(filename)) {
				result = cached.get(filename);
			} else {
				result = new Result(filename);
				if (result.entry != null) { // Only cache good ones.
					cached.put(filename, result);
				}
			}
			return result;
		}
		return null;
	}
	
	public class Result implements PaperResolutionResult {
		
		private ZipEntry entry;
		private String path;
		private String md5;
		
		public Result(String filename) {
			this.path = filename; // As if the entry isn't found we won't know the path.
			this.entry = zipFile.getEntry(filename);
		}

		public boolean isFound() {;
			return entry != null;
		}

		public InputStream getStream() throws IOException {
			if (entry != null) {
				return zipFile.getInputStream(entry);
			}
			return null;
		}

		public String[] getPaths() {
			return new String[]{path};
		}

		public String getMD5() throws IOException {
			if (md5 == null) {
				InputStream in = null;
				try {
					in = getStream();
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
