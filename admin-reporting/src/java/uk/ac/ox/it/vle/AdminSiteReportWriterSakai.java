package uk.ac.ox.it.vle;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.*;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.io.InputStream;

/**
 * Simple implementation that writes the content out into content hosting.
 *
 * @author Matthew Buckett
 */
public class AdminSiteReportWriterSakai implements AdminSiteReportWriter {

	// The admin user ID
	public static final String USER_ID = "admin";
	// The admin user EID
	public static final String USER_EID = "admin";

	private ContentHostingService contentHostingService;
	private SessionManager sessionManager;
	private String outputFolder;

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setOutputFolder(String outputFolder) {
		// Trim any trailing slash.
		if (outputFolder.endsWith(Entity.SEPARATOR)) {
			outputFolder = outputFolder.substring(0, outputFolder.length() - 1);
		}
		this.outputFolder = outputFolder;
	}


	public void doAsAdmin(Runnable runnable) {
		Session session = sessionManager.getCurrentSession();
		String oldId = session.getUserId();
		String oldEid = session.getUserEid();
		try {
			session.setUserId(USER_ID);
			session.setUserEid(USER_EID);
			runnable.run();
		} finally {
			session.setUserId(oldId);
			session.setUserEid(oldEid);
		}
	}

	@Override
	public void writeReport(final String filename, final String mimeType, final InputStream stream, final Access access) {
		doAsAdmin(new Runnable() {
			@Override
			public void run() {
				String resourceId = outputFolder + Entity.SEPARATOR + filename;
				try {
					try {
						contentHostingService.removeResource(resourceId);
					} catch (IdUnusedException e) {
						// Expected.
					}
					ContentResourceEdit resource = contentHostingService.addResource(resourceId);
					resource.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, filename);
					resource.setContent(stream);
					resource.setContentType(mimeType);
					if (Access.PUBLIC.equals(access)) {
						resource.setPublicAccess();
					}
					contentHostingService.commitResource(resource);
					// These catches should be updated to use SakaiException when we are on Sakai 10+.
				} catch (InUseException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				} catch (TypeException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				} catch (PermissionException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				} catch (IdUsedException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				} catch (IdInvalidException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				} catch (InconsistentException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				} catch (ServerOverloadException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				} catch (OverQuotaException e) {
					throw new IllegalStateException("Unable to update resource: " + resourceId, e);
				}

			}
		});
	}
}
