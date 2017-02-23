package org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Convenience class to house common routines used by several reference resolvers.
 *
 * @author bjones86
 */
public class RefResolverUtils
{
    /**
     * Convenience method to get a Site object by its ID
     * @param siteID the site ID of the object to retrieve
     * @param siteServ the SiteService used to do the retrieval
     * @param log the logger to log exceptions to
     * @return the site object, or null if it can't be found
     */
    public static Site getSiteByID( String siteID, SiteService siteServ, Log log )
    {
        try
        {
            return siteServ.getSite( siteID );
        }
        catch( IdUnusedException ex )
        {
            log.warn( "Unable to get site by ID: " + siteID, ex );
            return null;
        }
    }

    /**
     * Convenience method to get a User object by its ID.
     * @param userID the user ID of the object to retrieve
     * @param userDirServ the UserDirectoryService used to do the retrieval
     * @param log the logger to log exceptions to
     * @return the user object, or null if it can't be found
     */
    public static User getUserByID( String userID, UserDirectoryService userDirServ, Log log )
    {
        try
        {
            return userDirServ.getUser( userID );
        }
        catch( UserNotDefinedException ex )
        {
            log.warn( "Unable to get user by ID: " + userID, ex );
            return null;
        }
    }

    /**
     * Convenience method to extract the file name out of the resource ID (ref) given.
     * @param resourceID the resource ref (ID) to parse the file name from
     * @return the file name parsed from the resource ref string, or null
     */
    public static String getResourceFileName( String resourceID )
    {
        if( resourceID == null )
        {
            return null;
        }

        String[] delimiters = { "/", "\\" };
        for( String delimiter : delimiters )
        {
            int lastIndex = resourceID.lastIndexOf( delimiter );
            if( lastIndex >= 0 )
            {
                resourceID = resourceID.substring( lastIndex + 1 );
            }
        }

        return resourceID;
    }

    /**
     * Gets the resource's display name; if it doesn't exist, falls back to the filename
     * @param resource The resource object to retrieve it's pretty name
     * @return The pretty name of the resource object requested
     */
    public static String getResourceName( ContentResource resource )
    {
        ResourceProperties resourceProps = resource.getProperties();
        String displayName = (String) resourceProps.get( ResourceProperties.PROP_DISPLAY_NAME );
        if( StringUtils.isBlank( displayName ) )
        {
            return getResourceFileName( resource.getId() );
        }

        return displayName;
    }

    /**
     * Convenience method to encode the given string to UTF-8 compliant URL string.
     * @param toEncode the string to encode
     * @param log the logger to log exceptions to
     * @return the encoded string
     */
    public static String urlEncode( String toEncode, Log log )
    {
        try
        {
            return URLEncoder.encode( toEncode, "UTF-8" );
        }
        catch( UnsupportedEncodingException ex )
        {
            log.warn( "Unable to encode string to UTF-8 URL: " + toEncode, ex );
            return null;
        }
    }

    /**
     * Convenience method to decode the given UTF-8 compliant URL string.
     * @param toDecode the string to decode
     * @param log the logger to log exceptions to
     * @return the decoded string
     */
    public static String urlDecode( String toDecode, Log log )
    {
        try
        {
            return URLDecoder.decode( toDecode, "UTF-8" );
        }
        catch( UnsupportedEncodingException ex )
        {
            log.warn( "Unable to decode string from UTF-8 URL: " + toDecode, ex );
            return null;
        }
    }

    /**
     * Returns a singleton list for the specified key, value pair. If value is empty, returns empty list
     * @param key the key for the key-value pair
     * @param value the value for the key-value pair
     * @return a singleton list for the specified key-value pair
     */
    public static List<ResolvedRef> getSingletonList( String key, String value )
    {
        if( StringUtils.isEmpty( value ) )
        {
            return Collections.emptyList();
        }

        return Collections.singletonList( ResolvedRef.newText( key, value ) );
    }

    /**
     * Add a new text ResolvedRef to the list, provided that both the key and value are not empty/blank/null
     * @param eventDetails the list to add the ResolvedRef to
     * @param key the key to be added
     * @param value the value to be added
     */
    public static void addEventDetailsText( List<ResolvedRef> eventDetails, String key, String value )
    {
        if( StringUtils.isNotBlank( key ) && StringUtils.isNotBlank( value ) )
        {
            eventDetails.add( ResolvedRef.newText( key, value ) );
        }
    }

    /**
     * Add a new text ResolvedRef to the list, provided that both the key and value are not empty/blank/null
     * @param eventDetails the list to add the ResolvedRef to
     * @param key the key to be added
     * @param displayValue the display value to be used for the link
     * @param url the URL to be added
     */
    public static void addEventDetailsLink( List<ResolvedRef> eventDetails, String key, String displayValue, String url )
    {
        if( StringUtils.isNotBlank( key ) && StringUtils.isNotBlank( displayValue ) && StringUtils.isNotBlank( url ) )
        {
            eventDetails.add( ResolvedRef.newLink( key, displayValue, url ) );
        }
    }

    /**
     * Sort the given list of ResolvedRef objects by the 'key'.
     * @param eventDetails the list to be sorted
     * @return the sorted list
     */
    public static List<ResolvedRef> sortEventDetails( List<ResolvedRef> eventDetails )
    {
        // Sort the list by key
        Collections.sort( eventDetails, new Comparator<ResolvedRef>()
        {
            @Override
            public int compare( ResolvedRef lhs, ResolvedRef rhs )
            {
                return lhs.getKey().compareTo( rhs.getKey() );
            }
        });

        return eventDetails;
    }
}
