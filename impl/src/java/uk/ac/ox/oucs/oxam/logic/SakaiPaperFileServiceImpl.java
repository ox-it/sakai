package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

/**
 * This implementation puts uploaded files into the Sakai content hosting area. All the uploads are put into 
 * single site.
 * <p>
 * Having written a version to store the papers on the filesystem the Sakai in comparision seems a little
 * crazy with the proliferation of exceptions.
 * </p>
 * @author buckett
 *
 */
public class SakaiPaperFileServiceImpl implements PaperFileService {

	private final static Log LOG = LogFactory.getLog(SakaiPaperFileServiceImpl.class);
	
	private Location location;

	private ContentHostingService contentHostingService;
	
	private FileTypeMap mimeTypes;
	
	public void setFileSystemLocation(Location location) {
		this.location = location;
	}
	
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	public void setMimeTypes(FileTypeMap mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	public void init() {
		if (mimeTypes == null) {
			mimeTypes = new MimetypesFileTypeMap();
		}
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
		try {
			contentHostingService.checkResource(path);
			return true;
		} catch (PermissionException e) {
			LOG.warn("Problem checking resource: "+path + " "+e.getMessage());
		} catch (IdUnusedException e) {
			// Expected.
		} catch (TypeException e) {
			LOG.error("Wrong sort of resource at path: "+ path+ " "+ e.getMessage());
		}
		return false;
	}
	
	public InputStream getInputStream(PaperFile paperFile) {
		PaperFileImpl impl = castToImpl(paperFile);
		String path = impl.getPath();
		try {
			ContentResource resource = contentHostingService.getResource(path);
			return resource.streamContent();
		} catch (Exception e) {
			LOG.info("Failed to get stream for: "+ path);
		}
		return null;
		
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.oxam.logic.PaperFileService#deposit(uk.ac.ox.oucs.oxam.logic.PaperFile, uk.ac.ox.oucs.oxam.logic.Callback)
	 */
	public void deposit(PaperFile paperFile, InputStream in) {
		PaperFileImpl impl = castToImpl(paperFile);
		String path = impl.getPath();
		ContentResourceEdit resource = null;
		try {
			
			try {
				contentHostingService.checkResource(path);
				resource = contentHostingService.editResource(path);
				// Ignore PermissionException, IdUnusedException, TypeException
				// As they are too serious to continue.
			} catch (IdUnusedException iue) {
				// Will attempt to create containing folders.
				
				resource = contentHostingService.addResource(path);
				// Like the basename function.
				String filename = StringUtils.substringAfterLast(path, "/");
				ResourceProperties props = resource.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, filename);
				resource.setContentType(mimeTypes.getContentType(filename));
			}
			resource.setContent(in);
			contentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
			LOG.debug("Sucessfully copied file to: "+ path);
		} catch (OverQuotaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerOverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IdUsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IdInvalidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InconsistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Close the stream here as this is where it's created.
			if(resource.isActiveEdit()) {
				contentHostingService.cancelResource(resource);
			}
		}
		
	}

	private PaperFileImpl castToImpl(PaperFile paperFile) {
		// TODO Move paperfile to API as there doen't need to be differing implementations.
		if (!(paperFile instanceof PaperFileImpl)) {
			throw new IllegalArgumentException("PaperFile must have been retrieved from this service using get(String, String, String).");
		}
		return (PaperFileImpl)paperFile;
	}
}
