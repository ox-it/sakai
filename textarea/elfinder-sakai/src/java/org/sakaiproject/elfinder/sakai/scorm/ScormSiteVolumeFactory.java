package org.sakaiproject.elfinder.sakai.scorm;

import cn.bluejoe.elfinder.service.FsItem;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This is the creator for SCORM FsVolumes
 * @author bbailla2
 */
public class ScormSiteVolumeFactory implements SiteVolumeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ScormSiteVolumeFactory.class);
    private UserDirectoryService userDirectoryService;
    private ScormContentService contentService;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;
    private static final String TOOL_CONFIG_PERM = "scorm.configure";
    private static final String SCORM_URL_PREFIX = "/direct/scorm/";

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setContentService(ScormContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setSecurityService(SecurityService securityService)
    {
        this.securityService = securityService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    @Override
    public String getPrefix() {
        return "scorm";
    }

    @Override
    public SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId) {
        return new ScormSiteVolume(sakaiFsService,siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.scorm.tool";
    }
    
    public class ScormSiteVolume extends ReadOnlyFsVolume implements SiteVolume{
        private SakaiFsService service;
        private String siteId;

        public ScormSiteVolume(SakaiFsService service, String siteId) {
            this.service = service;
            this.siteId = siteId;
        }

        @Override
        public String getSiteId() {
            return this.siteId;
        }

        @Override
        public SiteVolumeFactory getSiteVolumeFactory() {
            return ScormSiteVolumeFactory.this;
        }

        @Override
        public boolean isWriteable(FsItem item) {
            return false;
        }

        @Override
        public boolean exists(FsItem fsItem) {
            return false;
        }

        @Override
        public FsItem fromPath(String path){
            if (!StringUtils.isBlank(path))
            {
                String tokens[] = path.split("/");
                if (tokens.length == 6 && getPrefix().equals(tokens[0]))
                {
                    //String siteId = tokens[1]; (unused)
                    String toolId = tokens[2];
                    String contentPackageId = tokens[3];
                    String resourceId = tokens[4];
                    String title = tokens[5];

                    return new ScormFsItem(this, toolId, contentPackageId, resourceId, title);
                }
            }
            return this.getRoot();
        }

        @Override
        public String getDimensions(FsItem fsItem) {
            return null;
        }

        @Override
        public long getLastModified(FsItem fsItem) {
            return 0L;
        }

        @Override
        public String getMimeType(FsItem fsItem) {
            return this.isFolder(fsItem)?"directory":"sakai/scorm";
        }

        @Override
        public String getName() {
            return "SCORM";
        }

        @Override
        public String getName(FsItem fsItem) {
            if(this.getRoot().equals(fsItem)) {
                return getName();
            }
            else if(fsItem instanceof ScormFsItem){
                return ((ScormFsItem)fsItem).getTitle();
            }
            else{
                throw new IllegalArgumentException("Could not get title for: " + fsItem.toString());
            }
        }

        @Override
        public FsItem getParent(FsItem fsItem) {
            if(this.getRoot().equals(fsItem)){
                return service.getSiteVolume(siteId).getRoot();
            }
            else if(fsItem instanceof ScormFsItem){
                return this.getRoot();
            }
            return null;
        }

        @Override
        public String getPath(FsItem fsItem) throws IOException {
            if (this.getRoot().equals(fsItem))
            {
                return "";
            }
            else if (fsItem instanceof ScormFsItem)
            {
                ScormFsItem scormFsItem = (ScormFsItem)fsItem;
                return "/" + getPrefix() + "/" + siteId + "/" + 
                    scormFsItem.getToolId() + "/" +
                    scormFsItem.getContentPackageId() + "/" +
                    scormFsItem.getResourceId() + "/" +
                    scormFsItem.getTitle();

            }
            throw new IllegalArgumentException("Wrong Type: " + fsItem.toString());
        }

        @Override
        public FsItem getRoot() {
            return new ScormFsItem(this, "", "", "", "");
        }

        @Override
        public long getSize(FsItem fsItem) throws IOException {
            return 0;
        }

        @Override
        public String getThumbnailFileName(FsItem fsItem) {
            return null;
        }

        @Override
        public boolean hasChildFolder(FsItem fsItem) {
            return fsItem instanceof ScormFsItem;
        }

        @Override
        public boolean isFolder(FsItem fsItem) {
            if(fsItem instanceof ScormFsItem && ((ScormFsItem)fsItem).getTitle().equals(""))
                return true;
            return false;
        }

        @Override
        public boolean isRoot(FsItem fsItem) {
            return false;
        }

        @Override
        public FsItem[] listChildren(FsItem fsItem) {
            List<FsItem> items = new ArrayList<>();
            String userId = null;
            User user = userDirectoryService.getCurrentUser();
            if (user == null)
            {
                return new FsItem[0];
            }
            userId = user.getId();
            if(this.getRoot().equals(fsItem)){
                String toolId = "";
                try
                {
                    Site site = siteService.getSite(siteId);
                    if (site != null)
                    {
                        if (!securityService.unlock(userId, TOOL_CONFIG_PERM, siteService.siteReference(siteId)))
                        {
                            LOG.error("User (" + userId + ") does not hav permission (" + TOOL_CONFIG_PERM + ") for site: " + siteId);
                            return new FsItem[0];
                        }

                        Collection<ToolConfiguration> toolConfigs = site.getTools(getToolId());

                        for (ToolConfiguration toolConfig : toolConfigs)
                        {
                            toolId = toolConfig.getId();
                        }

                        // Only continue if the tool ID is valid
                        if (StringUtils.isNotBlank(toolId))
                        {
                            // Get the content packages
                            List<ContentPackage> contentPackages = contentService.getContentPackages(siteId);
                            for (ContentPackage contentPackage : contentPackages)
                            {
                                String contentPackageId = String.valueOf(contentPackage.getContentPackageId());
                                String resourceId = contentPackage.getResourceId();
                                String title = contentPackage.getTitle();
                                items.add(new ScormFsItem(this, toolId, contentPackageId, resourceId, title));
                            }
                        }
                    }
                }
                catch(IdUnusedException ex)
                {
                    LOG.warn("Can't find site with ID = " + siteId, ex);
                    throw new IllegalArgumentException("Can't find site with ID = " + siteId, ex);
                }
                
                
            }
            else if(fsItem instanceof ScormFsItem){
                items.add(fsItem);
            }
            
            return items.toArray(new FsItem[0]);
        }

        @Override
        public InputStream openInputStream(FsItem fsItem) throws IOException {
            return null;
        }

        @Override
        public String getURL(FsItem fsItem) {
            String serverUrlPrefix = serverConfigurationService.getServerUrl();
            if(fsItem instanceof ScormFsItem){
                ScormFsItem scormFsItem = (ScormFsItem)fsItem;
                return serverUrlPrefix + SCORM_URL_PREFIX + siteId + ":" + scormFsItem.getToolId() + ":" + 
                    scormFsItem.getContentPackageId() + ":" + scormFsItem.getResourceId() + ":" + scormFsItem.getTitle();
            }
            return null;
        }
    }
}
