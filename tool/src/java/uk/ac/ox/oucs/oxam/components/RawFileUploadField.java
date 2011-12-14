package uk.ac.ox.oucs.oxam.components;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.Request;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.util.upload.DiskFileItem;
import org.apache.wicket.util.upload.FileItem;

/**
 * This class lets you get at the Raw FileItem, this is needed because
 * it's not possible to get the temporary file on disk without it, only an InputStream.
 * 
 * @see http://stackoverflow.com/questions/8493819/wicket-fileuploadfield-and-large-zip-uploads
 * @author buckett
 *
 */
public class RawFileUploadField extends FileUploadField {

	private static final long serialVersionUID = 1L;

	public RawFileUploadField(String id) {
		super(id);
	}
	
	/**
	 * Attempts to get the file that is on disk if it exists and if it doesn't
	 * then just write the file to a temp location.
	 * @return The file or <code>null</code> if no file.
	 * @throws IOException
	 */
	public File getFile() throws IOException {
		// Get request
		final Request request = getRequest();

		// If we successfully installed a multipart request
		if (request instanceof IMultipartWebRequest)
		{
			// Get the item for the path
			FileItem item = ((IMultipartWebRequest)request).getFile(getInputName());
			if (item instanceof DiskFileItem) {
				File location = ((DiskFileItem)item).getStoreLocation();
				if (location != null) {
					return location;
				}
			}
		}
		// Fallback
		FileUpload upload = getFileUpload();
		return (upload != null)?upload.writeToTempFile():null;
	}

}
