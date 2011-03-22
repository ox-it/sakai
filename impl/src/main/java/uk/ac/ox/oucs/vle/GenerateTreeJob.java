package uk.ac.ox.oucs.vle;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * This job creates the static tree of departments.
 * It puts the file inside content hosting. It also sets up a site so that people can
 * find the site and login and upload resources.
 * @author buckett
 *
 */
public class GenerateTreeJob implements Job {
	
	private static final Log log = LogFactory.getLog(GenerateTreeJob.class);
	
	private ContentHostingService contentHostingService;
	private UserDirectoryService userDirectoryService;
	private SiteService siteService;
	private GenerateTree generateTree;
	private ServerConfigurationService serverConfigurationService;
	private SessionManager sessionManager;
	

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setGenerateTree(GenerateTree generateTree) {
		this.generateTree = generateTree;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		switchUser();
		setupSite();
		String json = generateTree.generateDepartmentTree();
		String jsonResourceId = contentHostingService.getSiteCollection(getSiteId())+ "departments.json";
		ContentResourceEdit cre = null;
		
		try {
			try {
				// editResource() doesn't throw IdUnusedExcpetion but PermissionException
				// when the resource is missing so we first just tco to find it.
				contentHostingService.getResource(jsonResourceId);
				cre = contentHostingService.editResource(jsonResourceId);
			} catch (IdUnusedException e) {
				try {
					cre = contentHostingService.addResource(jsonResourceId);
					ResourceProperties props = cre.getPropertiesEdit();
					props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, "departments.json");
					cre.setContentType("application/json");
				} catch (IdUsedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IdInvalidException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InconsistentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ServerOverloadException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			try {
				cre.setContent(json.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Don't notify anyone about this resource.
			contentHostingService.commitResource(cre, NotificationService.NOTI_NONE);
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverQuotaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerOverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != cre && cre.isActiveEdit()) {
				contentHostingService.cancelResource(cre);
			}
		}
		
	}
	
	/**
	 * This sets up the user for the current request.
	 */
	private void switchUser() {
		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
	}

	/**
	 * This checks that a site exists for the static content and if it doesn't creates one.
	 * Throws a runtime exception if the site doesn't exist and can't be created.
	 */
	protected void setupSite() {
		String siteId = getSiteId();
		try {
			siteService.getSite(siteId);
		} catch (IdUnusedException iue) {
			try {
				Site newSite = siteService.addSite(siteId, "project", null);
				newSite.setTitle("Course Signup Content");
				newSite.setPublished(true);
				SitePage resourcesPage = newSite.addPage();
				resourcesPage.setTitle("Resources");
				resourcesPage.addTool("sakai.resources");
				SitePage siteInfoPage = newSite.addPage();
				siteInfoPage.setTitle("Site Info");
				siteInfoPage.addTool("sakai.siteinfo");
				
				// Add the current user.
				newSite.addMember(userDirectoryService.getCurrentUser().getId(), "maintain", true, false);
				siteService.save(newSite);
			
			} catch (IdInvalidException e) {
				log.warn(siteId+ " isn't a valid ID.", e);
				throw new IllegalArgumentException(e);
			} catch (IdUsedException e) {
				log.warn("There already exists a site with the ID of: "+ siteId);
				throw new IllegalStateException(e);
			} catch (PermissionException e) {
				log.warn("Lack of permissions", e);
				throw new RuntimeException(e);
			} catch (IdUnusedException e) {
				log.warn("Should never happen");
				throw new RuntimeException(e);
			}
			
			String siteContentId = contentHostingService.getSiteCollection(siteId);

			try {
				contentHostingService.getCollection(siteContentId);
			} catch (IdUnusedException e) {
				try {
					ContentCollectionEdit cc = contentHostingService.addCollection(siteContentId);
					contentHostingService.commitCollection(cc);
					contentHostingService.setPubView(siteContentId, true);
				} catch (IdUsedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IdInvalidException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (PermissionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InconsistentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (TypeException e) {
				
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected String getSiteId() {
		return serverConfigurationService.getString("course-signup.site-id", "course-signup");
	}

}
