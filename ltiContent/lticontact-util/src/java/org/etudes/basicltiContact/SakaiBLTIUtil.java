/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/ltiContact/trunk/lticontact-util/src/java/org/etudes/basicltiContact/SakaiBLTIUtil.java $
 * $Id: SakaiBLTIUtil.java 8795 2014-09-18 17:37:40Z rashmim $
 **********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2014 Etudes, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

package org.etudes.basicltiContact;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

/**
 * Some Sakai Utility code for IMS Basic LTI
 * This is mostly code to support the Sakai conventions for
 * making and launching BLTI resources within Sakai.
 */
public class SakaiBLTIUtil {

    private static Log M_log = LogFactory.getLog(SakaiBLTIUtil.class);
    
    public static void dPrint(String str)
    {
        // M_log.warn(str)
    }

    /**
     * Add User information to Launch properties
     * @param props
     * @param context
     * @param user
     * @return
     */
    public static Properties addUserInformation(Properties props, String context, User user)
    {
    	if ( user == null ) return props;
    	
		setProperty(props,"user_id",user.getId());
		setProperty(props,"launch_presentation_locale","en_US");
		
		String givenName = user.getFirstName();
		String familyName = user.getLastName();
		String fullName = user.getFirstName() +" " +user.getLastName();
		String email = user.getEmail();
		if (givenName != null && givenName.trim().length() > 0 && familyName != null && familyName.trim().length() > 0)
		{
			setProperty(props,"lis_person_name_given",givenName.trim());
			setProperty(props,"lis_person_name_family",familyName.trim());
			setProperty(props,"lis_person_name_full",fullName.trim());			
		}		
		
		if (email != null && email.trim().length() > 0)
			setProperty(props,"lis_person_contact_email_primary",user.getEmail().trim());
		
		setProperty(props,"lis_person_sourcedid",user.getEid());

    	return props;
    }

	/**
	 *
	 * @param props
	 * @return
	 */
	public static Properties addOrganizationDetails(Properties props)
	{
		// Get the organizational information
		setProperty(props, "tool_consumer_instance_guid", ServerConfigurationService.getString("basiclti.consumer_instance_guid", null));
		setProperty(props, "tool_consumer_instance_name", ServerConfigurationService.getString("basiclti.consumer_instance_name", null));
		setProperty(props, "tool_consumer_instance_url", ServerConfigurationService.getString("basiclti.consumer_instance_url", null));
		setProperty(props, "launch_presentation_return_url", ServerConfigurationService.getString("basiclti.consumer_return_url", null));
		setProperty(props, "tool_consumer_info_product_family_code", ServerConfigurationService.getString("ui.service", "etudes"));
		setProperty(props, "tool_consumer_info_version", "NG");
		return props;
	}

	/**
	 *
	 * @param contextId
	 * @param whichProvider
	 * @return
	 */
	public static boolean showProviderInEditor(String contextId, String whichProvider)
	{
		if (contextId == null || whichProvider == null) return false;
		if (findProviderPlacement(contextId, whichProvider) == null) return false;
		return true;
	}

	/**
	 *
	 * @param contextId
	 * @param whichProvider
	 * @return
	 */
	public static Placement findProviderPlacement(String contextId, String whichProvider)
	{
		try
		{
			Site site = SiteService.getSite(contextId);
			Collection<ToolConfiguration> toolList = site.getTools("sakai.iframe");

			if (toolList != null && !toolList.isEmpty())
			{
				Iterator<ToolConfiguration> toolIter = toolList.iterator();
				// Iterate through tools of source site
				while (toolIter.hasNext())
				{
					ToolConfiguration toolConfig = (ToolConfiguration) toolIter.next();

					// we do not want to import "special" uses of sakai.iframe, such as worksite info
					String special = toolConfig.getPlacementConfig().getProperty("special");

					if (special == null && toolConfig.getTitle().equalsIgnoreCase(whichProvider))
					{
						return toolConfig;
					}
				}// while end
			}

			return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}

    /**
     * Load url, key etc from placement
     * @param info
     * @param launch
     * @param placement
     * @return
     */
	public static boolean loadFromPlacement(Properties info, Properties launch, Placement placement)
	{
		Properties config = placement.getConfig();
		String launch_url = config.getProperty("source", null);

		if (config.containsKey("secret"))
		{
			setProperty(info, "secret", config.getProperty("secret", null));
		}

		if (config.containsKey("key"))
		{
			setProperty(info, "key", config.getProperty("key", null));
		}

		setProperty(info, "launch_url", launch_url);

		if (config.getProperty("extraInformation") != null)
		{
			String extra = config.getProperty("extraInformation");
			String[] extraParts = StringUtil.split(extra, "\r\n");
			for (String e : extraParts)
			{
				if (e.trim().length() == 0 || e.indexOf("=") == -1) continue;
				String k = e.substring(0, e.indexOf("=")).trim();
				// custom keys are lowercase and non-alphanumeric are underscore
				k = k.toLowerCase();
				k = BasicLTIUtil.mapKeyName(k);
				String v = e.substring(e.indexOf("=") + 1).trim();
				if (k != null && v != null && !info.containsKey(k)) info.setProperty("custom_" + k, v);
			}
		}
		
		if (placement.getTitle() != null && placement.getTitle().trim().length() > 0)
			setProperty(launch, "resource_link_title", placement.getTitle().trim());
		
		if (info.getProperty("launch_url", null) != null || info.getProperty("secure_launch_url", null) != null)
		{
			return true;
		}
		return false;
	}

    /**
     * Retrieve the Sakai information about users, Site Context and Etudes organization.
     **/
	public static boolean addSiteInfo(Properties props, String context, String placementId)
	{
		Site site = null;
		try
		{
			site = SiteService.getSite(context);
		}
		catch (Exception e)
		{
			dPrint("No site/page associated with Launch context=" + context);
			return false;
		}

		// Start setting the Basici LTI parameters
		setProperty(props, "resource_link_id", placementId);

		User user = UserDirectoryService.getCurrentUser();
		props = addUserInformation(props, context, user);

		if (site != null)
		{
			String context_type = site.getType();
			if (context_type != null && context_type.toLowerCase().contains("course"))
			{
				setProperty(props, "context_type", "CourseOffering");
			}
			setProperty(props, "context_id", site.getId());
			setProperty(props, "context_label", site.getTitle());
			setProperty(props, "context_title", site.getTitle());
			String courseRoster = getExternalRealmId(site.getId());
			if (courseRoster != null)
			{
				setProperty(props, "lis_course_offering_sourced_id", courseRoster);
			}
		}

		props = addOrganizationDetails(props);
		return true;
	}

   /**
    * Generic call to create lti form details
    * @param placementId
    *  for non-melete tools required
    * @param contextId
    * @param resourceId
    * 	for melete resource or later mneme resource
    * @param info
    * @param launch
    * @param autoSubmit
    *  true when we want to submit automatically just like for Melete
    * @param props
    * 	resource properties used for connecting with Melete
    * @param rb
    * @param targetFrame
    * 	to open in window or iframe
    * @return
    */
	public static String[] postLaunchHTML(String placementId, String descriptor, String contextId, String resourceId, Properties info, Properties launch, boolean autoSubmit,
			ResourceProperties props, ResourceLoader rb, String targetFrame)
	{
		if (info == null) info = new Properties();
		if (launch == null) launch = new Properties();
		if (autoSubmit) launch.setProperty("ext_autosubmit", "true");
		String resource_link_id = resourceId;

		// web tool , voicethread plugin loads information here
		if (placementId != null)
		{
			ToolConfiguration placement = SiteService.findTool(placementId);
			loadFromPlacement(info, launch, placement);
			if (contextId == null || contextId.length() == 0) contextId = placement.getContext();
			if (resource_link_id == null || resource_link_id.length() == 0)resource_link_id = placement.getId();
		}
			
		// Add user, course, etc to the launch parameters
		if (!addSiteInfo(launch, contextId, resource_link_id))
		{
			return postError("<p>" + getRB(rb, "error.missing", "Error, cannot load Sakai information for placement=") + placementId + ".</p>");
		}

		if (info.getProperty("launch_url", null) == null && info.getProperty("secure_launch_url", null) == null)
		{
			return postError("<p>" + getRB(rb, "error.nolaunch", "Not Configured.") + "</p>");
		}

	     if (targetFrame != null && targetFrame.length() > 0)
	        	setProperty(launch, "launch_presentation_document_target", targetFrame);

		return createPostLaunchHTML(info, launch, rb);
	}

     /**
     * Called from Web Content tool
     * @param placementId
     * @param returnUrl
     * @param rb
     * @return
     */
    public static String[] postLaunchHTML(String placementId, Properties info, ResourceLoader rb, String targetFrame)
    {
        if ( placementId == null ) return postError("<p>" + getRB(rb, "error.missing" ,"Error, missing placementId")+"</p>" );
        ToolConfiguration placement = SiteService.findTool(placementId);
        if ( placement == null ) return postError("<p>" + getRB(rb, "error.load" ,"Error, cannot load placement=")+placementId+".</p>");

        // Add user, course, etc to the launch parameters
        String context = placement.getContext();
        Properties launch = new Properties();
        if ( ! addSiteInfo(launch, context, placement.getId()) ) {
           return postError("<p>" + getRB(rb, "error.missing",
                "Error, cannot load Sakai information for placement=")+placementId+".</p>");
        }

        // Retrieve the launch detail
        boolean loadLTIDetails = false;
        if (info.getProperty("launch_url", null) == null &&  info.getProperty("secure_launch_url", null) == null ) {
        	loadLTIDetails = loadFromPlacement(info, launch, placement);
           }

       if (!loadLTIDetails)
           return postError("<p>" + getRB(rb, "error.nolaunch" ,"Not Configured.")+"</p>");

        if (targetFrame != null && targetFrame.length() > 0)
        	setProperty(launch, "launch_presentation_document_target", targetFrame);

    	return createPostLaunchHTML(info, launch, rb);
    }

    /**
     * Common method to create the form data from all properties
     * @param info
     * @param launch
     * @param rb
     * @return
     */
    public static String[] createPostLaunchHTML(Properties info, Properties launch, ResourceLoader rb)
    {
        String launch_url = info.getProperty("secure_launch_url");
        if ( launch_url == null ) launch_url = info.getProperty("launch_url");
        if ( launch_url == null ) return postError("<p>" + getRB(rb, "error.missing" ,"Not configured")+"</p>");

        String org_guid = ServerConfigurationService.getString("basiclti.consumer_instance_guid",null);
        String org_name = ServerConfigurationService.getString("basiclti.consumer_instance_name",null);
        String org_url = ServerConfigurationService.getString("basiclti.consumer_instance_url",null);

        // key - secret
        // Look up the local secret and key first. changed from site-wide first to local by rashmi
        String secret = toNull(info.getProperty("secret"));
        String key = toNull(info.getProperty("key"));

        // Demand LMS-wide key/secret to be both or none
        if ( key == null ) getToolConsumerInfo(launch_url,"key");
        if ( key == null ) key = org_guid;
        if ( secret == null ) secret = getToolConsumerInfo(launch_url,"secret");

         // Pull in all of the custom parameters
        for(Object okey : info.keySet() ) {
                String skey = (String) okey;
                if ( ! skey.startsWith("custom_") ) continue;
                String value = info.getProperty(skey);
                if ( value == null ) continue;
                setProperty(launch, skey, value);
        }

        String oauth_callback = ServerConfigurationService.getString("basiclti.oauth_callback",null);
	// Too bad there is not a better default callback url for OAuth
        // Actually since we are using signing-only, there is really not much point
	// In OAuth 6.2.3, this is after the user is authorized
        if ( oauth_callback == null ) oauth_callback = "about:blank";
		if (!launch.containsKey("oauth_callback")) setProperty(launch, "oauth_callback", oauth_callback);
        setProperty(launch, BasicLTIUtil.BASICLTI_SUBMIT, getRB(rb, "launch.button", "Press to Launch External Tool"));

        // Sanity checks
        if ( secret == null ) {
            return postError("<p>" + getRB(rb, "error.nosecret", "Error - must have a secret.")+"</p>");
        }
        if (  secret != null && key == null ){
            return postError("<p>" + getRB(rb, "error.nokey", "Error - must have a secret and a key.")+"</p>");
        }

        launch = BasicLTIUtil.signProperties(launch, launch_url, "POST",
            key, secret, org_guid, org_name, org_url);

        if ( launch == null ) return postError("<p>" + getRB(rb, "error.sign", "Error signing message.")+"</p>");
        dPrint("LAUNCH III="+launch);

        boolean dodebug = toNull(info.getProperty("debug")) != null;
        String postData = BasicLTIUtil.createPostLaunchHTML(launch, launch_url, dodebug);
        String [] retval = { postData, launch_url };
        return retval;
    }

    /**
     *
     * @param str
     * @return
     */
    public static String[] postError(String str) {
        String [] retval = { str };
        return retval;
     }

    /**
     *
     * @param rb
     * @param key
     * @param def
     * @return
     */
    public static String getRB(ResourceLoader rb, String key, String def)
    {
        if ( rb == null ) return def;
        return rb.getString(key, def);
    }

    /**
     *
     * @param props
     * @param key
     * @param value
     */
    public static void setProperty(Properties props, String key, String value)
    {
        if ( props == null || value == null || key == null) return;
        if ( value.trim().length() < 1 ) return;        
        if (props.containsKey(key)) return;
        props.setProperty(key, value);
    }

    /**
     *
     * @param siteId
     * @return
     */
    private static String getExternalRealmId(String siteId) {
        String realmId = SiteService.siteReference(siteId);
        String rv = null;
        try {
            AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
            rv = realm.getProviderGroupId();
        } catch (GroupNotDefinedException e) {
            dPrint("SiteParticipantHelper.getExternalRealmId: site realm not found"+e.getMessage());
        }
        return rv;
    } // getExternalRealmId

    /**
     * Look through a series of secrets from the properties based on the launchUrl
     */
    private static String getToolConsumerInfo(String launchUrl, String data)
    {
        String default_secret = ServerConfigurationService.getString("basiclti.consumer_instance_"+data,null);
        dPrint("launchUrl = "+launchUrl);
        URL url = null;
        try {
            url = new URL(launchUrl);
        }
        catch (Exception e) {
            url = null;
        }
        if ( url == null ) return default_secret;
        String hostName = url.getHost();
        dPrint("host = "+hostName);
        if ( hostName == null || hostName.length() < 1 ) return default_secret;
        // Look for the property starting with the full name
        String org_info = ServerConfigurationService.getString("basiclti.consumer_instance_"+data+"."+hostName,null);
        if ( org_info != null ) return org_info;
        for ( int i = 0; i < hostName.length(); i++ ) {
            if ( hostName.charAt(i) != '.' ) continue;
            if ( i > hostName.length()-2 ) continue;
            String hostPart = hostName.substring(i+1);
            String propName = "basiclti.consumer_instance_"+data+"."+hostPart;
            org_info = ServerConfigurationService.getString(propName,null);
            if ( org_info != null ) return org_info;
        }
        return default_secret;
    }

    /**
     *
     * @param str
     * @return
     */
    public static String toNull(String str)
    {
       if ( str == null ) return null;
       if ( str.trim().length() < 1 ) return null;
       return str;
    }
}
