package uk.ac.ox.oucs.vle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
	public static List<ZipError> expandZip(InputStream inputStream, String root) {
		List<ZipError> errors = new ArrayList<ZipError>();
		ZipInputStream zipfile = new ZipInputStream(inputStream);
		ZipEntry entry;
		try {
			while ((entry = zipfile.getNextEntry()) != null) {
				if(entry.isDirectory()) {
					File dir = new File(root, entry.getName());
					if (!dir.exists() && !dir.mkdirs()) {
						errors.add(new ZipError(dir, "Failed to create directory."));
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
							errors.add(new ZipError(file, "Unable to create file: "+ fnfe.getMessage()));
						} catch (IOException ioe) {
							errors.add(new ZipError(file, "IO problem copying file: "+ ioe.getMessage()));
						} finally {
							if (out != null) {
								try {
									out.close();
								} catch (IOException ioe) {} // Ignore
							}
						}
					} else {
						errors.add(new ZipError(file, "Failed to create containing folder"));
					}
				}
			}
		} catch (IOException e) {
			errors.add(new ZipError(null, "Failed to get next entry"));
		}
		return errors;
	}
	
	/**
	 * Creates an input based on a folder and all it's contents
	 * @param outputstream The outputstream is write the filezip to.
	 * @param root The path the zip.
	 * @return A list of errors.
	 */
	public static List<ZipError> createZip(OutputStream outputStream, String root) {
		List<ZipError> errors = new ArrayList<ZipError>();
		Queue<File> dirsToProcess = new ArrayDeque<File>(); 
		ZipOutputStream zipfile = new ZipOutputStream(outputStream);

		File rootFile = new File(root);
		dirsToProcess.add(rootFile);

		File dir;
		while ((dir = dirsToProcess.poll()) != null) {
			if (dir.exists()) {
				for (File file: dir.listFiles()) {
					if (file.isDirectory()) {
						dirsToProcess.add(file);
					} else {
						BufferedInputStream input = null;
						try {
							String zipName = relative(rootFile, file);
							ZipEntry entry = new ZipEntry(zipName);
							zipfile.putNextEntry(entry);
							input = new BufferedInputStream(new FileInputStream(file));
							IOUtils.copy(input, zipfile);
						} catch (FileNotFoundException fne) {
							errors.add(new ZipError(file, "Couldn't find file"));
						} catch (IOException ioe) {
							errors.add(new ZipError(file, "Problem reading file."));
						}
						finally {
							if (input != null) {
								try {
									input.close();
								} catch (IOException ioe) {} //Ignore
							}
						}
					}
				}
			} else {
				errors.add(new ZipError(dir, "Doesn't exist"));
			}
		}

		return errors;
	}
	
	static String relative (File parent, File child) {
		return child.getAbsolutePath().substring(parent.getAbsolutePath().length());
	}
}
