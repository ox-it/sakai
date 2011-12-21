package uk.ac.ox.oucs.oxam.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This puts file in the filesystem.
 * @author buckett
 *
 */
public class LocalPaperFileServiceImpl implements PaperFileService {

	private final static Log LOG = LogFactory.getLog(LocalPaperFileServiceImpl.class);
	
	private Location location;
	
	public void setFileSystemLocation(Location location) {
		this.location = location;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.oxam.logic.PaperFileService#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public PaperFile get(String year, String term, String paperCode, String extension) {
		return new PaperFileImpl(year, term, paperCode, extension, location);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.oxam.logic.PaperFileService#exists(uk.ac.ox.oucs.oxam.logic.PaperFile)
	 */
	public boolean exists(PaperFile paperFile) {
		PaperFileImpl impl = castToImpl(paperFile);
		String path = impl.getPath();
		return new File(path).exists();
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.oxam.logic.PaperFileService#deposit(uk.ac.ox.oucs.oxam.logic.PaperFile, uk.ac.ox.oucs.oxam.logic.Callback)
	 */
	public void deposit(PaperFile paperFile, InputStream in ) {
		PaperFileImpl impl = castToImpl(paperFile);
		String path = impl.getPath();
		File file = new File(path);
		OutputStream out = null;
		try {
			// Create containing directory
			createPath(file);
			file.createNewFile();
			out = new FileOutputStream(file);
			IOUtils.copy(in, out);
			LOG.debug("Sucessfully copied file to: "+ file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			LOG.error("Creation should have failed for: "+ file.getAbsolutePath(), e);
		} catch (IOException e) {
			LOG.error("Problem creation directory/file for: "+ file.getAbsolutePath(), e);
		} finally {
			// Close the stream here as this is where it's created.
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOG.info("Problem closing file: "+ e.getMessage());
				}
			}
		}
		
	}

	private void createPath(File file) throws IOException {
		if (file.exists()) {
			return;
		}
		File directory = file.getParentFile();
		if (!directory.mkdirs() && !directory.exists()) {
			throw new IOException("Failed to create directory: "+ directory);
		}
	}

	private PaperFileImpl castToImpl(PaperFile paperFile) {
		if (!(paperFile instanceof PaperFileImpl)) {
			throw new IllegalArgumentException("PaperFile must have been retrieved from this service using get(String, String, String).");
		}
		return (PaperFileImpl)paperFile;
	}
}
