package org.sakaiproject.elfinder.sakai.ezproxy;

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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
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
 * This is the creator for EZProxy FsVolumes
 * @author bbailla2
 */
public class EZProxySiteVolumeFactory implements SiteVolumeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(EZProxySiteVolumeFactory.class);
    private UserDirectoryService userDirectoryService;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;

    private static final String ENTITY_PREFIX = "ezproxy";

    private static final String TOOL_PERM_NAME = "ezproxy.configure";
    private static final String SCORM_URL_PREFIX = "/direct/ezproxy/";

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
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
        return "ezproxy";
    }

    @Override
    public SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId) {
        return new EZProxySiteVolume(sakaiFsService,siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.ezproxy";
    }
    
    public class EZProxySiteVolume extends ReadOnlyFsVolume implements SiteVolume{
        private SakaiFsService service;
        private String siteId;

        public EZProxySiteVolume(SakaiFsService service, String siteId) {
            this.service = service;
            this.siteId = siteId;
        }

        @Override
        public String getSiteId() {
            return this.siteId;
        }

        @Override
        public SiteVolumeFactory getSiteVolumeFactory() {
            return EZProxySiteVolumeFactory.this;
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
                if (tokens.length == 3 && getPrefix().equals(tokens[0]))
                {
                    //String siteId = tokens[1]; (unused)
                    String pageId = tokens[2];
                    SitePage page = getPageById(pageId);
                    if (page != null)
                    {
                        return new EZProxyFsItem(this, page);
                    }
                }
            }
            return this.getRoot();
        }

        /**
         * @throws IllegalStateException if:
         *     -UserDirectoryService.getCurrentUser() returns null
         *     -No site can be found whose id = this.siteId
         * @return the page with the specified ID; or null if they don't have permission
         */
        private SitePage getPageById(String pageId) throws IllegalStateException
        {
            User user = userDirectoryService.getCurrentUser();
            if (user == null)
            {
                throw new IllegalStateException("userDirectoryService.getCurrentUser() returned null");
            }
            String userId = user.getId();
            Site site = null;
            try
            {
                site = siteService.getSite(siteId);
            }
            catch (IdUnusedException ex)
            {
                throw new IllegalStateException("No site found for site ID: " + siteId + " : " + ex.getMessage());
            }
            if (site != null)
            {
                if (!securityService.unlock(userId, TOOL_PERM_NAME, siteService.siteReference(siteId)))
                {
                    LOG.error("User (" + userId + ") does not have permission (" + TOOL_PERM_NAME + ") for site: " + siteId);
                    return null;
                }
                return site.getPage(pageId);
            }
            return null;
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
            return this.isFolder(fsItem)?"directory":"sakai/ezproxy";
        }

        @Override
        public String getName() {
            return "EZProxy";
        }

        @Override
        public String getName(FsItem fsItem) {
            if(this.getRoot().equals(fsItem)) {
                return getName();
            }
            else if(fsItem instanceof EZProxyFsItem){
                // Get the title of the page for this item
                EZProxyFsItem item = (EZProxyFsItem) fsItem;
                SitePage page = item.getPage();
                if (page == null)
                {
                    return getName();
                }
                return page.getTitle();
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
            else if(fsItem instanceof EZProxyFsItem){
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
            else if (fsItem instanceof EZProxyFsItem)
            {
                EZProxyFsItem ezProxyFsItem = (EZProxyFsItem)fsItem;
                return "/" + getPrefix() + "/" + siteId + "/" + 
                    ezProxyFsItem.getPageId();
            }
            throw new IllegalArgumentException("Wrong Type: " + fsItem.toString());
        }

        @Override
        public FsItem getRoot() {
            return new EZProxyFsItem(this, null);
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
            return fsItem instanceof EZProxyFsItem;
        }

        @Override
        public boolean isFolder(FsItem fsItem) {
            if(fsItem instanceof EZProxyFsItem && ((EZProxyFsItem)fsItem).getPage() == null)
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
                try
                {
                    // Get the site, verify it exists
                    Site site = siteService.getSite(siteId);
                    if (site != null)
                    {
                        // Ensure the current user has 'ezproxy.configure' permission for the site
                        if (!securityService.unlock(userId, TOOL_PERM_NAME, siteService.siteReference(siteId)))
                        {
                            LOG.error("User (" + userId + ") does not hav permission (" + TOOL_PERM_NAME + ") for site: " + siteId);
                            return new FsItem[0];
                        }

                        // Loop through a list of EZProxy instances in this site
                        Collection<ToolConfiguration> ezProxyLinks = site.getTools(getToolId());
                        for (ToolConfiguration config : ezProxyLinks)
                        {
                            // Get the page that contains this EZProxy instance
                            SitePage page = config.getContainingPage();
                            if (page != null)
                            {
                                items.add(new EZProxyFsItem(this, page));
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
            else if(fsItem instanceof EZProxyFsItem){
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
            if(fsItem instanceof EZProxyFsItem){
                EZProxyFsItem ezProxyFsItem = (EZProxyFsItem)fsItem;
                return serverUrlPrefix + SCORM_URL_PREFIX + siteId + ":" + ezProxyFsItem.getPageId();
            }
            return null;
        }
    }
}
