package org.sakaiproject.elfinder.sakai.ezproxy;

import cn.bluejoe.elfinder.service.FsItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * @author bjones86
 */
@Slf4j
public class EZProxySiteVolumeFactory implements SiteVolumeFactory
{
    private static final String DIRECTORY           = "directory";
    private static final String EZP_ENTITY_PREFIX   = "ezproxy";
    private static final String EZP_CONFIG_PERM     = "ezproxy.configure";
    private static final String EZP_TYPE            = "sakai/ezproxy";
    private static final String EZP_URL_PREFIX      = "/direct/ezproxy/";
    private static final String EZP_TOOL_ID         = "sakai.ezproxy";
    private static final String EZP_DFLT_TOOL_NAME  = "EZProxy Library Link";

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private SecurityService securityService;
    @Setter private SiteService siteService;

    @Override
    public String getPrefix()
    {
        return EZP_ENTITY_PREFIX;
    }

    @Override
    public String getToolId()
    {
        return EZP_TOOL_ID;
    }

    @Override
    public SiteVolume getVolume( SakaiFsService sakaiFsService, String siteID )
    {
        return new EZProxySiteVolume( sakaiFsService, siteID );
    }

    @AllArgsConstructor
    public class EZProxySiteVolume extends ReadOnlyFsVolume implements SiteVolume
    {
        private SakaiFsService service;
        @Getter private String siteId;

        @Override
        public SiteVolumeFactory getSiteVolumeFactory()
        {
            return EZProxySiteVolumeFactory.this;
        }

        @Override
        public String getMimeType( FsItem item )
        {
            return isFolder( item ) ? DIRECTORY : EZP_TYPE;
        }

        @Override
        public boolean isFolder( FsItem item )
        {
            return item instanceof EZProxyFsItem && ((EZProxyFsItem) item).getPage() == null;
        }

        @Override
        public String getName()
        {
            return EZP_DFLT_TOOL_NAME;
        }

        @Override
        public String getName( FsItem item )
        {
            if( getRoot().equals( item ) )
            {
                return getName();
            }
            if( item instanceof EZProxyFsItem )
            {
                // Get the title of the page for this item
                EZProxyFsItem i = (EZProxyFsItem) item;
                SitePage page = i.getPage();
                return page == null ? getName() : page.getTitle();
            }
            else
            {
                throw new IllegalArgumentException( "Could not get title for: " + item.toString() );
            }
        }

        @Override
        public FsItem getRoot()
        {
             return new EZProxyFsItem( this, null );
        }

        @Override
        public FsItem getParent( FsItem item )
        {
            if( getRoot().equals( item ) )
            {
                return service.getSiteVolume( siteId ).getRoot();
            }
            else if( item instanceof EZProxyFsItem )
            {
                return getRoot();
            }

            return null;
        }

        @Override
        public boolean hasChildFolder( FsItem item )
        {
            return item instanceof EZProxyFsItem;
        }

        @Override
        public String getPath( FsItem item ) throws IOException
        {
            if( getRoot().equals( item ) )
            {
                return "";
            }
            else if( item instanceof EZProxyFsItem )
            {
                EZProxyFsItem ezProxyFsItem = (EZProxyFsItem) item;
                return "/" + getPrefix() + "/" + siteId + "/" + ezProxyFsItem.getPageID();
            }

            throw new IllegalArgumentException( "Wrong Type: " + item.toString() );
        }

        @Override
        public FsItem fromPath( String path )
        {
            if( StringUtils.isNotBlank( path ) )
            {
                String tokens[] = path.split("/");
                if( tokens.length == 3 && getPrefix().equals( tokens[0] ) )
                {
                    String pageId = tokens[2];
                    SitePage page = getPageById( pageId );
                    if( page != null )
                    {
                        return new EZProxyFsItem( this, page );
                    }
                }
            }

            return getRoot();
        }

        @Override
        public FsItem[] listChildren( FsItem item )
        {
            User user = userDirectoryService.getCurrentUser();
            if( user == null )
            {
                return new FsItem[0];
            }

            List<FsItem> items = new ArrayList<>();
            if( getRoot().equals( item ) )
            {
                try
                {
                    // Get the site, verify it exists
                    Site site = siteService.getSite(siteId);
                    if( site != null )
                    {
                        // Ensure the current user has 'ezproxy.configure' permission for the site
                        if( !securityService.unlock( user.getId(), EZP_CONFIG_PERM, siteService.siteReference( siteId  ) ) )
                        {
                            log.debug( "User {} does not have permission {} for site {}", user.getId(), EZP_CONFIG_PERM, siteId );
                            return new FsItem[0];
                        }

                        // Iterate over a list of EZProxy instances in this site
                        Collection<ToolConfiguration> ezProxyLinks = site.getTools( getToolId() );
                        for( ToolConfiguration config : ezProxyLinks )
                        {
                            // Get the page that contains this EZProxy instance
                            SitePage page = config.getContainingPage();
                            if( page != null )
                            {
                                items.add( new EZProxyFsItem( this, page ) );
                            }
                        }
                    }
                }
                catch( IdUnusedException e )
                {
                    log.warn( "Can't find site with ID = {}", siteId );
                }
            }
            else if( item instanceof EZProxyFsItem )
            {
                items.add( item );
            }

            return items.toArray( new FsItem[items.size()] );
        }

        @Override
        public String getURL( FsItem item )
        {
            if( item instanceof EZProxyFsItem )
            {
                EZProxyFsItem ezProxyItem = (EZProxyFsItem) item;
                String serverURL = serverConfigurationService.getServerUrl();
                return serverURL + EZP_URL_PREFIX + siteId + ":" + ezProxyItem.getPageID();
            }

            return null;
        }

        /**
         * Utility method to get a SitePage by it's ID.
         * @return the page with the specified ID; or null if they don't have permission, user can't be found, or site can't be found
         */
        private SitePage getPageById( String pageId )
        {
            User user = userDirectoryService.getCurrentUser();
            if( user == null )
            {
                log.warn( "userDirectoryService.getCurrentUser() returned null" );
                return null;
            }

            try
            {
                Site site = siteService.getSite( siteId );
                if( site != null )
                {
                    if( !securityService.unlock( user.getId(), EZP_CONFIG_PERM, siteService.siteReference( siteId ) ) )
                    {
                        log.debug( "User {} does not have permission {} for site {}", user.getId(), EZP_CONFIG_PERM, siteId );
                        return null;
                    }

                    return site.getPage( pageId );
                }
            }
            catch( IdUnusedException ex )
            {
                log.warn( "Can't find site with ID = {}", siteId );
            }

            return null;
        }

        // Unimplemented methods below
        @Override public boolean        exists              ( FsItem item )                     { return false; }
        @Override public boolean        isRoot              ( FsItem item )                     { return false; }
        @Override public boolean        isWriteable         ( FsItem item )                     { return false; }
        @Override public String         getDimensions       ( FsItem item )                     { return null;  }
        @Override public String         getThumbnailFileName( FsItem item )                     { return null;  }
        @Override public InputStream    openInputStream     ( FsItem item ) throws IOException  { return null;  }
        @Override public long           getLastModified     ( FsItem item )                     { return 0L;    }
        @Override public long           getSize             ( FsItem item ) throws IOException  { return 0L;    }
    }
}
