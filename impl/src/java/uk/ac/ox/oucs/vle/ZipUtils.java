package uk.ac.ox.oucs.vle;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

/**
 * Small class to handle uncompressing a stream into a folder.
 * @author buckett
 *
 */
public class ZipUtils {

	/**
	 * Expands the inputstream into the folder supplied.
	 * @param inputStream The inputstream to the zipfile.
	 * @param root The path to the into which the zipfile should be expanded.
	 * @return A list of errors.
	 */
	public static List<ZipExpansionError> expandZip(InputStream inputStream, String root) {
		List<ZipExpansionError> errors = new ArrayList<ZipExpansionError>();
		ZipInputStream zipfile = new ZipInputStream(inputStream);
		ZipEntry entry;
		try {
			while ((entry = zipfile.getNextEntry()) != null) {
				if(entry.isDirectory()) {
					File dir = new File(root, entry.getName());
					if (!dir.exists() && !dir.mkdirs()) {
						errors.add(new ZipExpansionError(dir, "Failed to create directory."));
					}
				} else {
					File file = new File (root, entry.getName());
					File parentFile = file.getParentFile();
					if (parentFile.isDirectory() || parentFile.mkdirs()) {
						OutputStream out = null;
						try {
							out = new BufferedOutputStream(new FileOutputStream(file));
							IOUtils.copy(zipfile, out);
						} catch (FileNotFoundException fnfe) {
							errors.add(new ZipExpansionError(file, "Unable to create file: "+ fnfe.getMessage()));
						} catch (IOException ioe) {
							errors.add(new ZipExpansionError(file, "IO problem copying file: "+ ioe.getMessage()));
						} finally {
							if (out != null) {
								try {
									out.close();
								} catch (IOException ioe) {} // Ignore
							}
						}
					} else {
						errors.add(new ZipExpansionError(file, "Failed to create containing folder"));
					}
				}
			}
		} catch (IOException e) {
			errors.add(new ZipExpansionError(null, "Failed to get next entry"));
		}
		return errors;
	}
}
