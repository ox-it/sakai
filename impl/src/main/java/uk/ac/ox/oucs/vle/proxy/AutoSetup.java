package uk.ac.ox.oucs.vle.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.*;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.SakaiComponentEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.io.InputStream;

/**
 * This attempts to automatically setup content needed for the tool to function.
 * This is done on Spring events so that we can be sure all the services have done init()
 * and are ready for use.
 */
public class AutoSetup implements ApplicationListener {

	private static final Log log = LogFactory.getLog(AutoSetup.class);

	private ContentHostingService contentHostingService;
	private SessionManager sessionManager;
	private UsageSessionService usageSessionService;
	private ServerConfigurationService serverConfigurationService;

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {

		if (serverConfigurationService.getBoolean("ses.autosetup", true) &&
				event instanceof SakaiComponentEvent) {
			try {
				doAsAdmin(new ImportContent());
			} catch (Exception e) {
				log.error("Failed to setup content", e);
			}
		}
	}

	/**
	 * Actually so the importing.
	 */
	protected class ImportContent implements Runnable {

		@Override
		public void run() {
			String siteId = serverConfigurationService.getString("course-signup.site-id", "course-signup");
			String collection = ContentHostingService.COLLECTION_SITE + siteId;
			// Create folder.
			createFolder(ContentHostingService.COLLECTION_SITE, siteId, true);
			// Copy in files
			copyFile(collection, "departments.json");
		}

		/**
		 * Copy a file from the classpath to content hosting.
		 * Don't do anything if the file already exists.
		 *
		 * @param collection The location to import into.
		 * @param filename   The filename
		 */
		private void copyFile(String collection, String filename) {
			String classPath = "/autosetup/" + filename;
			InputStream source = getClass().getResourceAsStream(classPath);
			if (source == null) {
				throw new NullPointerException("Couldn't find " + source);
			}
			String contentPath = collection + Entity.SEPARATOR+ filename;
			try {
				ContentResourceEdit file = contentHostingService.addResource(contentPath);
				file.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME, filename);
				file.setContent(source);
				contentHostingService.commitResource(file);
			} catch (PermissionException e) {
				throw new RuntimeException(e);
			} catch (IdUsedException e) {
				log.debug("File already exists: " + contentPath);
			} catch (IdInvalidException e) {
				throw new RuntimeException(e);
			} catch (InconsistentException e) {
				throw new RuntimeException(e);
			} catch (ServerOverloadException e) {
				throw new RuntimeException(e);
			} catch (OverQuotaException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Create a folder in content hosting.
		 * If the folder already exists don't do anything.
		 *
		 * @param base The containing folder.
		 * @param name The name of the new folder
		 * @param isPublic If <code>true</code> then make it public
		 */
		private void createFolder(String base, String name, boolean isPublic) {
			String path = base + name;
			try {
				ContentCollectionEdit collection = contentHostingService.addCollection(base, name);
				collection.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				if (isPublic) {
					collection.setPublicAccess();
				}
				contentHostingService.commitCollection(collection);
			} catch (IdUsedException e) {
				log.debug("Folder already exists, this is expected: " + path);
			} catch (IdInvalidException e) {
				throw new RuntimeException(e);
			} catch (PermissionException e) {
				throw new RuntimeException(e);
			} catch (InconsistentException e) {
				throw new RuntimeException(e);
			} catch (IdUnusedException e) {
				throw new RuntimeException(e);
			} catch (TypeException e) {
				throw new RuntimeException(e);
			} catch (IdLengthException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Runs the supplied instance as admin.
	 * But makes sure then no matter what happens we stop running as admin at the end.
	 *
	 * @param runnable The thing to run as admin.
	 */
	protected void doAsAdmin(Runnable runnable) {
		try {
			Session session = sessionManager.startSession();
			session.setUserId("admin");
			sessionManager.setCurrentSession(session);
			usageSessionService.startSession("admin", null, null);
			runnable.run();
		} finally {
			Session session = sessionManager.getCurrentSession();
			if (session != null) {
				session.invalidate();
			}
		}
	}
}
