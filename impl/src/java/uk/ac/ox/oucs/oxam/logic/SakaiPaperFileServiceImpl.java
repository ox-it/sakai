package uk.ac.ox.oucs.oxam.logic;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
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
	
	public void setFileSystemLocation(Location location) {
		this.location = location;
	}
	
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
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
				resource = contentHostingService.editResource(path);
				// Ignore PermissionException, IdUnusedException, TypeException
				// As they are too serious to continue.
			} catch (InUseException iue) {
				// Will attempt to create containing folders.
				resource = contentHostingService.addResource(path);
			}
			resource.setContent(in);
			contentHostingService.commitResource(resource);
			LOG.debug("Sucessfully copied file to: "+ path);
		} catch (VirusFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverQuotaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerOverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IdUnusedException e) {
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
		if (!(paperFile instanceof PaperFileImpl)) {
			throw new IllegalArgumentException("PaperFile must have been retrieved from this service using get(String, String, String).");
		}
		return (PaperFileImpl)paperFile;
	}
}
