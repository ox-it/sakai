package org.sakaiproject.component.app.scheduler.jobs.attachmentpermissions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.util.List;

/**
 * This class looks for all the attachment folders and makes the owner of them all to be the site owner.
 */
public class UpdateAttachmentPermissions implements Job {

    // After processing this many site folders we reset the session/threadlocals to prevent running out of memory.
    public static final int RESET_EVERY = 100;

    private final Log log = LogFactory.getLog(UpdateAttachmentPermissions.class);

    private ContentHostingService contentHostingService;

    private SessionManager sessionManager;

    private SiteService siteService;

    private UserDirectoryService userDirectoryService;

    private ThreadLocalManager threadLocalManager;

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        this.threadLocalManager = threadLocalManager;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int siteAttachmentFolders = 0, toolAttachmentFolders = 0, unknownOwners = 0, fixed = 0;


        Session currentSession = null;
        try {
            currentSession = resetSession();
            ContentCollection attachmentRoot;
            attachmentRoot = contentHostingService.getCollection("/attachment/");
            List<String> attachmentSites = attachmentRoot.getMembers();
            for (String attachmentSiteId : attachmentSites) {
                try {
                    if (!contentHostingService.isCollection(attachmentSiteId)) {
                        continue;
                    }
                    siteAttachmentFolders++;
                    // Reset every 100 folders.
                    if (siteAttachmentFolders % RESET_EVERY == 0) {
                        currentSession = resetSession();
                        log.debug("Reset session after: "+ siteAttachmentFolders);
                    }
                    ContentCollection attachmentSite = contentHostingService.getCollection(attachmentSiteId);
                    String siteId = isolateName(attachmentSiteId);

                    String newOwner = getSiteCreator(siteId);
                    if (newOwner != null) {
                        for (String toolCollectionId : attachmentSite.getMembers()) {
                            if (contentHostingService.isCollection(toolCollectionId)) {
                                toolAttachmentFolders++;
                                if (setCollectionCreator(toolCollectionId, newOwner)) {
                                    fixed++;
                                }
                            } else {
                                log.debug("Ignored non-collection entry: " + toolCollectionId);
                            }
                        }
                    }
                } catch (SakaiException se) {
                    log.warn("Failed to load attachment collection for " + attachmentSiteId, se);
                }
            }
            log.info("Finished processing attachment collections:" +
                    " siteFolders: " + siteAttachmentFolders +
                    " toolFolders: " + toolAttachmentFolders +
                    " unknownOwners: "+ unknownOwners +
                    " fixed: "+ fixed);

        } catch (SakaiException se) {
            throw new JobExecutionException("Couldn't load attachment folder stopping.", se);
        } finally {
            if (currentSession != null) {
                currentSession.clear();
                threadLocalManager.clear();
            }
        }
    }

    /**
     * This clears any previous session. This is because ContentHostingService uses thread local caches which don't
     * work well with long running quartz jobs as they eat up all the memory.
     * @return A session setup as admin.
     */
    private Session resetSession() {
        threadLocalManager.clear();
        Session session = sessionManager.getCurrentSession();
        session.setUserId("admin");
        session.setUserEid("admin");
        return session;
    }

    /**
     * Set's the owner of the collection to the new owner.
     *
     * @param collectionId The collection to reset the owner on.
     * @param userId The user ID of the new owner.
     * @return <code>true</code> if the owner was reset.
     */
    private boolean setCollectionCreator(String collectionId, String userId) {
        ContentCollectionEdit toolCollection = null;
        try {
            toolCollection = contentHostingService.editCollection(collectionId);
            ResourcePropertiesEdit propertiesEdit = toolCollection.getPropertiesEdit();
            String creator = propertiesEdit.getProperty(ResourceProperties.PROP_CREATOR);
            if (!userId.equals(creator)) {
                propertiesEdit.addProperty(ResourceProperties.PROP_CREATOR, userId);
                contentHostingService.commitCollection(toolCollection);
                return true;
            } else {
                log.debug("Owner is already correct on: " + collectionId);
            }
        } catch (SakaiException se) {
            log.warn("Failed to set owner on " + collectionId + " " + se.getMessage());
        } finally {
            if (toolCollection != null && toolCollection.isActiveEdit()) {
                contentHostingService.cancelCollection(toolCollection);
            }
        }
        return false;
    }

    private String getSiteCreator(String siteId) {
        String newOwner = null;
        try {
            Site site = siteService.getSite(siteId);
            User createdBy = site.getCreatedBy();
            // We don't want to change the owner to be the anonymous user.
            if (!userDirectoryService.getAnonymousUser().equals(createdBy)) {
                newOwner = createdBy.getId();
            } else {
                log.debug("Refusing to use the anonymous user for: "+ siteId);
            }
        } catch (IdUnusedException iue) {
            log.debug("Failed to find site for: " + siteId);
        }
        return newOwner;
    }


    private static String isolateName(String id) {
        if (id == null) return null;
        if (id.length() == 0) return null;

        // take after the last resource path separator, not counting one at the very end if there
        boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
        return id.substring(id.lastIndexOf('/', id.length() - 2) + 1, (lastIsSeparator ? id.length() - 1 : id.length()));

    } // isolateName
}
