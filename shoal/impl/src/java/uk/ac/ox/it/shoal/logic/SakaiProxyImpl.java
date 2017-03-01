package uk.ac.ox.it.shoal.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.*;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import uk.ac.ox.it.shoal.model.TeachingItem;
import uk.ac.ox.it.shoal.model.TeachingItemModel;

import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;

import static org.sakaiproject.event.api.NotificationService.NOTI_NONE;
import static uk.ac.ox.it.shoal.utils.StringPacker.pack;
import static uk.ac.ox.it.shoal.utils.StringPacker.unpack;


/**
 * Implementation of our SakaiProxy API
 */
public class SakaiProxyImpl implements SakaiProxy {

    private static final Log log = LogFactory.getLog(SakaiProxyImpl.class);

    private ToolManager toolManager;

    private SessionManager sessionManager;

    private UserDirectoryService userDirectoryService;

    private SecurityService securityService;

    private ServerConfigurationService serverConfigurationService;

    private SiteService siteService;

    private ContentHostingService contentHostingService;

    private EventTrackingService eventTrackingService;

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setServerConfigurationService(
            ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentSiteId() {
        return toolManager.getCurrentPlacement().getContext();
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentUserDisplayName() {
        return userDirectoryService.getCurrentUser().getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSuperUser() {
        return securityService.isSuperUser();
    }

    /**
     * {@inheritDoc}
     */
    public void postEvent(String event, String reference, boolean modify) {
        eventTrackingService.post(eventTrackingService.newEvent(event, reference, modify));
    }

    /**
     * {@inheritDoc}
     */
    public String getSkinRepoProperty() {
        return serverConfigurationService.getString("skin.repo");
    }

    /**
     * {@inheritDoc}
     */
    public String getToolSkinCSS(String skinRepo) {

        String skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();

        if (skin == null) {
            skin = serverConfigurationService.getString("skin.default");
        }

        return skinRepo + "/" + skin + "/tool.css";
    }

    /**
     * {@inheritDoc}
     */
    public boolean getConfigParam(String param, boolean dflt) {
        return serverConfigurationService.getBoolean(param, dflt);
    }

    /**
     * {@inheritDoc}
     */
    public String getConfigParam(String param, String dflt) {
        return serverConfigurationService.getString(param, dflt);
    }

    @Override
    public TeachingItem getTeachingItem() {
        TeachingItem teachingItem = new TeachingItemModel();
        Placement placement = toolManager.getCurrentPlacement();
        if (placement != null) {
            String context = placement.getContext();
            try {
                Site site = siteService.getSite(context);
                teachingItem = toTeachingItem(site);
            } catch (IdUnusedException e) {
                log.warn("Failed to find site with ID: " + context, e);
            }
        } else {
            log.warn("No placement found, this tool will not function correctly.");
        }
        return teachingItem;
    }

    static TeachingItem toTeachingItem(Site site) {
        TeachingItem teachingItem = new TeachingItemModel();
        ResourceProperties properties = site.getProperties();
        teachingItem.setId(site.getId());
        teachingItem.setTitle(site.getTitle());
        teachingItem.setDescription(site.getDescription());
        teachingItem.setSubject(unpack(properties.getProperty(key("subject"))));
        teachingItem.setLevel(unpack(properties.getProperty(key("level"))));
        teachingItem.setPurpose(unpack(properties.getProperty(key("purpose"))));
        teachingItem.setInteractivity(properties.getProperty(key("interactivity")));
        teachingItem.setType(unpack(properties.getProperty(key("type"))));
        teachingItem.setAuthor(properties.getProperty(key("author")));
        teachingItem.setContact(properties.getProperty(key("contact")));

        try {
            String stamp = properties.getProperty(key("added"));
            teachingItem.setAdded(Instant.ofEpochMilli(Long.parseLong(stamp)));
        } catch (NumberFormatException e) {
            // Do nothing
        }
        teachingItem.setPermission(properties.getProperty(key("permission")));
        teachingItem.setThumbnail(properties.getProperty(key("thumbnail")));
        teachingItem.setLicense(properties.getProperty(key("license")));
        teachingItem.setUrl(properties.getProperty(key("url")));

        return teachingItem;
    }

    private static String key(String property) {
        return "item." + property;
    }

    @Override
    public void saveTeachingItem(TeachingItem model) {
        Placement placement = toolManager.getCurrentPlacement();
        if (placement != null) {
            // Set added time if not set.
            if (model.getAdded() == null) {
                model.setAdded(Instant.now());
            }
            String context = placement.getContext();
            try {
                Site site = siteService.getSite(context);
                // The URL should always be the site.
                model.setUrl(site.getUrl());

                ResourceProperties properties = site.getProperties();
                site.setTitle(model.getTitle());
                site.setDescription(model.getDescription());
                properties.addProperty(key("description"), model.getDescription());
                properties.addProperty(key("subject"), pack(model.getSubject()));
                properties.addProperty(key("level"), pack(model.getLevel()));
                properties.addProperty(key("purpose"), pack(model.getPurpose()));
                properties.addProperty(key("interactivity"), model.getInteractivity());
                properties.addProperty(key("type"), pack(model.getType()));
                properties.addProperty(key("author"), model.getAuthor());
                properties.addProperty(key("contact"), model.getContact());
                properties.addProperty(key("added"), Long.toString(model.getAdded().toEpochMilli()));
                properties.addProperty(key("permission"), model.getPermission());
                properties.addProperty(key("thumbnail"), model.getThumbnail());
                properties.addProperty(key("license"), model.getLicense());
                properties.addProperty(key("url"), model.getUrl());

                siteService.save(site);

            } catch (IdUnusedException e) {
                throw new RuntimeException("Failed to find site for current context: " + context, e);
            } catch (PermissionException e) {
                // We should have a bundle keyed runtime exception here so we can handle in UI.
                throw new RuntimeException("No permission to save site: " + context, e);
            }
        } else {
            log.warn("No placement found, this tool will not function correctly.");
        }
    }

    @Override
    public String saveThumbnail(InputStream inputStream) {
        Placement placement = toolManager.getCurrentPlacement();
        if (placement != null) {
            String context = placement.getContext();
            String siteCollection = contentHostingService.getSiteCollection(context);
            String thumbnailPath = siteCollection + "thumbnail.jpg";
            try {
                try {
                    contentHostingService.removeResource(thumbnailPath);
                } catch (IdUnusedException e) {
                    // We expect this to happen.
                } catch (TypeException | InUseException e) {
                    throw new RuntimeException("Can't remove file, so can't upload replacement.", e);
                }
                ContentResourceEdit content = contentHostingService.addResource(thumbnailPath);
                content.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "thumbnail.jpg");
                content.setContent(inputStream);
                // The thumbnails should always be accessible.
                content.setPublicAccess();
                contentHostingService.commitResource(content, NOTI_NONE);
                return content.getUrl(true);
            } catch (IdUsedException | InconsistentException | ServerOverloadException e) {
                // Shouldn't ever happen and should log this.
                log.warn("Failed to save image: " + thumbnailPath, e);
                throw new UserMessageException("Failed to save image, try again.", e, "error.upload.try", null);
            } catch (IdInvalidException e) {
                // Shouldn't ever happen
                log.warn("Invalid ID: " + thumbnailPath);
                throw new UserMessageException("Invalid File", e, "error.upload.unknown", null);
            } catch (PermissionException e) {
                throw new UserMessageException("No Permission", e, "error.upload.permission", null);
            } catch (OverQuotaException e) {
                throw new UserMessageException("Over quota.", e, "error.upload.quota", null);
            }
        } else {
            log.warn("No placement found, this tool will not function correctly.");
        }
        return null;
    }

    /**
     * init - perform any actions required here for when this bean starts up
     */
    public void init() {
        Objects.requireNonNull(toolManager);
        Objects.requireNonNull(sessionManager);
        Objects.requireNonNull(userDirectoryService);
        Objects.requireNonNull(securityService);
        Objects.requireNonNull(eventTrackingService);
        Objects.requireNonNull(serverConfigurationService);
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(contentHostingService);
        log.info("init");
    }

}
