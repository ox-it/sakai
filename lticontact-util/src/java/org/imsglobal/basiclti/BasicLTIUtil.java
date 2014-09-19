/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/ltiContact/trunk/lticontact-util/src/java/org/imsglobal/basiclti/BasicLTIUtil.java $
 * $Id: BasicLTIUtil.java 8795 2014-09-18 17:37:40Z rashmim $
 **********************************************************************************
 *
 * Copyright (c) 2008 IMS GLobal Learning Consortium
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
package org.imsglobal.basiclti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.signature.OAuthSignatureMethod;

public class BasicLTIUtil {

    // We use the built-in Java logger because this code needs to be very generic
    private static Logger M_log = Logger.getLogger(BasicLTIUtil.class.toString());

    public static final String BASICLTI_SUBMIT = "ext_basiclti_submit";

    // Simple Debug Print Mechanism
    public static void dPrint(String str)
    {
        // System.out.println(str);
        M_log.fine(str);
    }

    private static void setErrorMessage(Properties retProp, String message)
    {
        retProp.setProperty("message",message);
        retProp.setProperty("status","fail");
    }

    public static String validateDescriptor(String descriptor)
    {
        if ( descriptor == null ) return null;
        if ( descriptor.indexOf("<basic_lti_link") < 0 ) return null;

        Map<String,Object> tm = XMLMap.getFullMap(descriptor.trim());
        if ( tm == null ) return null;

        // We demand at least an endpoint
        String ltiSecureLaunch = XMLMap.getString(tm,"/basic_lti_link/secure_launch_url");
        // We demand at least an endpoint
        if ( ltiSecureLaunch != null && ltiSecureLaunch.trim().length() > 0 ) return ltiSecureLaunch;
        String ltiLaunch = XMLMap.getString(tm,"/basic_lti_link/launch_url");
        if ( ltiLaunch != null && ltiLaunch.trim().length() > 0 ) return ltiLaunch;
        return null;
    }

    // Remove any properties which we wil not send
    public static Properties cleanupProperties(Properties newMap) {
        Properties newProp = new Properties();
        for(Object okey : newMap.keySet() )
        {
                if ( ! (okey instanceof String) ) continue;
                String key = (String) okey;
                if ( key == null ) continue;
                String value = newMap.getProperty(key);
                if ( value == null ) continue;
                if ( key.startsWith("internal_") ) continue;
                if ( key.startsWith("_") ) continue;
                if ( "action".equalsIgnoreCase(key) ) continue;
                if ( "launchurl".equalsIgnoreCase(key) ) continue;
                if ( value.equals("") ) continue;
                newProp.setProperty(key, value);
         }
         return newProp;
    }

    /**
     * Sort properties alphabetically for signature generation
     * @param postProp
     * @return
     */
	public static Properties sortParameters(Properties postProp)
	{
		Properties sortProp = new Properties();
		ArrayList<String> ss = new ArrayList<String>();

		Enumeration keys = postProp.keys();
		while (keys.hasMoreElements())
		{
			ss.add((String) keys.nextElement());
		}
		Collections.sort(ss);

		for (String s : ss)
		{
			sortProp.put(s, postProp.getProperty(s));
		}
		return sortProp;
	}
    
    // Add the necessary fields and sign
    public static Properties signProperties(Properties postProp, String url, String method, 
        String oauth_consumer_key, String oauth_consumer_secret, 
	String org_id, String org_desc, String org_url)
    {
        postProp = BasicLTIUtil.cleanupProperties(postProp);
        postProp.setProperty("lti_version","LTI-1p0");
        postProp.setProperty("lti_message_type","basic-lti-launch-request");
	// Allow caller to internatonalize this for us...
        if ( postProp.getProperty(BASICLTI_SUBMIT) == null ) {
            postProp.setProperty(BASICLTI_SUBMIT, "Launch Endpoint with BasicLTI Data");
        }
        if ( org_id != null ) postProp.setProperty("tool_consumer_instance_guid", org_id);
        if ( org_desc != null ) postProp.setProperty("tool_consumer_instance_description", org_desc);
        if ( org_url != null ) postProp.setProperty("tool_consumer_instance_url", org_desc);

        if ( postProp.getProperty("oauth_callback") == null ) postProp.setProperty("oauth_callback","about:blank");

        if ( oauth_consumer_key == null || oauth_consumer_secret == null ) {
            dPrint("No signature generated in signProperties");
            return postProp;
        }

        url = url.trim();
        
        Properties sortProp = new Properties();
        sortProp = sortParameters(postProp);
        OAuthMessage oam = new OAuthMessage(method, url,sortProp.entrySet());
        OAuthConsumer cons = new OAuthConsumer("about:blank",
            oauth_consumer_key, oauth_consumer_secret, null);
        OAuthAccessor acc = new OAuthAccessor(cons);
        try {
            oam.addRequiredParameters(acc);
            List<Map.Entry<String, String>> params = oam.getParameters();
            
            Properties nextProp = new Properties();
            // Convert to Properties
            for (Map.Entry<String,String> e : params) {
                nextProp.setProperty(e.getKey(), e.getValue());
            }
	    return nextProp;
        } catch (net.oauth.OAuthException e) {
            M_log.warning("BasicLTIUtil.signProperties OAuth Exception "+e.getMessage());
            return null;
        } catch (java.io.IOException e) {
            M_log.warning("BasicLTIUtil.signProperties IO Exception "+e.getMessage());
            return null;
        } catch (java.net.URISyntaxException e) {
            M_log.warning("BasicLTIUtil.signProperties URI Syntax Exception "+e.getMessage());
            return null;
        }
    
    }

    // Create the HTML to render a POST form and then automatically submit it
    // Make sure to call cleanupProperties before signing
    public static String createPostLaunchHTML(Properties newMap, String endpoint, boolean debug) {
        if ( endpoint == null ) return null;
        StringBuffer text = new StringBuffer();
        boolean addSubmit = false;
        
        text.append("<html>\n");
    	text.append("<head> \n <meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\" /> </head>\n");
    	text.append("<body>\n <div id=\"ltiLaunchFormSubmitArea\">\n");
    	text.append("<form action=\"" + endpoint
    				+ "\" name=\"ltiLaunchForm\" id=\"ltiLaunchForm\" method=\"post\" encType=\"application/x-www-form-urlencoded\">\n");
    	    	
		for (Object okey : newMap.keySet())
		{
			if (!(okey instanceof String)) continue;
			String key = (String) okey;
			if (key == null) continue;
			String value = newMap.getProperty(key);
			if (value == null) continue;
			// This will escape the contents pretty much - at least
			// we will be safe and not generate dangerous HTML
			//key = htmlspecialchars(key);
			// value = htmlspecialchars(value);
			if (key.equals("ext_autosubmit")) addSubmit = true;
			
			text.append("<input type=\"hidden\" name=\"");
			text.append(key);
			text.append("\" value=\"");
			text.append(value);
			text.append("\"/>\n");

			if (key.equals(BASICLTI_SUBMIT))
			{
				text.append("<input type=\"submit\" id=\"" + key + "\" value=\"" + value + "\"/>\n");
			}
		}
        String htmltext = writeHtmlEnd(text, addSubmit);
        return htmltext;
    }

	/**
	 * 
	 * @param text
	 * @param addSubmit
	 * @return
	 */
	public static String writeHtmlEnd(StringBuffer text, boolean addSubmit)
	{
		text.append("</form>\n" + "</div>\n </body> \n");
		text.append("<script language=\"javascript\"> \n" + "document.getElementById(\"ltiLaunchFormSubmitArea\").style.display = \"none\";\n");
		if (addSubmit) text.append("document.ltiLaunchForm.submit(); \n");
		text.append(" </script> \n </html>");
		String htmltext = text.toString();
		return htmltext;
	}
        
    /**
     * read descriptor and put as properties
     * @param descriptor
     * @return
     */
	public static Properties parseDescriptor(String descriptor)
	{
		Properties info = new Properties();
		Map<String, Object> tm = null;
		try
		{
			tm = XMLMap.getFullMap(descriptor.trim());
		}
		catch (Exception e)
		{
			M_log.warning("BasicLTIUtil exception parsing BasicLTI descriptor" + e.getMessage());
			return info;
		}
		if (tm == null)
		{
			M_log.warning("Unable to parse XML in parseDescriptor");
			return info;
		}

		String launch_url = toNull(XMLMap.getString(tm, "/basic_lti_link/launch_url"));
		String secure_launch_url = toNull(XMLMap.getString(tm, "/basic_lti_link/secure_launch_url"));
		if (launch_url != null) setProperty(info, "launch_url", launch_url);
		if (secure_launch_url != null) setProperty(info, "launch_url", secure_launch_url);

		// Extensions for hand-authored placements - The export process should scrub these
		setProperty(info, "key", toNull(XMLMap.getString(tm, "/basic_lti_link/x-secure/launch_key")));
		setProperty(info, "secret", toNull(XMLMap.getString(tm, "/basic_lti_link/x-secure/launch_secret")));

		List<Map<String, Object>> theList = XMLMap.getList(tm, "/basic_lti_link/custom/parameter");
		for (Map<String, Object> setting : theList)
		{
			String key = XMLMap.getString(setting, "/!key"); // Get the key atribute
			String value = XMLMap.getString(setting, "/"); // Get the value
			if (key == null || value == null) continue;
			key = "custom_" + mapKeyName(key);
			setProperty(info, key, value);
		}
		return info;
	}
    
    /**
     * 
     * @param launch_info
     * @param postProp
     * @param descriptor
     * @return
     */
	public static boolean parseDescriptor(Properties launch_info, Properties postProp, String descriptor)
	{
		Map<String, Object> tm = null;
		try
		{
			tm = XMLMap.getFullMap(descriptor.trim());
		}
		catch (Exception e)
		{
			M_log.warning("BasicLTIUtil exception parsing BasicLTI descriptor" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		if (tm == null)
		{
			M_log.warning("Unable to parse XML in parseDescriptor");
			return false;
		}

		boolean retVal = false;

		String launch_url = toNull(XMLMap.getString(tm, "/basic_lti_link/launch_url"));
		String secure_launch_url = toNull(XMLMap.getString(tm, "/basic_lti_link/secure_launch_url"));
		if (launch_url == null && secure_launch_url == null) return false;

		setProperty(launch_info, "launch_url", launch_url);
		setProperty(launch_info, "secure_launch_url", secure_launch_url);

		// Extensions for hand-authored placements - The export process should scrub these
		setProperty(launch_info, "key", toNull(XMLMap.getString(tm, "/basic_lti_link/x-secure/launch_key")));
		setProperty(launch_info, "secret", toNull(XMLMap.getString(tm, "/basic_lti_link/x-secure/launch_secret")));

		List<Map<String, Object>> theList = XMLMap.getList(tm, "/basic_lti_link/custom/parameter");
		for (Map<String, Object> setting : theList)
		{
			dPrint("Setting=" + setting);
			String key = XMLMap.getString(setting, "/!key"); // Get the key atribute
			String value = XMLMap.getString(setting, "/"); // Get the value
			if (key == null || value == null) continue;
			key = "custom_" + mapKeyName(key);
			dPrint("key=" + key + " val=" + value);
			postProp.setProperty(key, value);
		}
		return true;
	}

    // Remove fields that should not be exported
    public static String prepareForExport(String descriptor)
    {
        Map<String,Object> tm = null;
        try
        {
                tm = XMLMap.getFullMap(descriptor.trim());
        } 
        catch (Exception e) {
                M_log.warning("BasicLTIUtil exception parsing BasicLTI descriptor"+e.getMessage());
		e.printStackTrace();
		return null;
        }
        if ( tm == null ) {
            M_log.warning("Unable to parse XML in prepareForExport");
            return null;
        }
        XMLMap.removeSubMap(tm,"/basic_lti_link/x-secure");
        String retval = XMLMap.getXML(tm, true);
        return retval;
    }

    /*
        The parameter name is mapped to lower case and any character that 
         is neither a number or letter is replaced with an "underscore".  
         So if a custom entry was as follows:

         <parameter name="Vendor:Chapter">1.2.56</parameter>

         Would map to: 
           custom_vendor_chapter=1.2.56
    */

    public static String mapKeyName(String keyname)
    {
       StringBuffer sb = new StringBuffer();
       if ( keyname == null ) return null;
       keyname = keyname.trim();
       if ( keyname.length() < 1 ) return null;
       for(int i=0; i < keyname.length(); i++) {
           Character ch = Character.toLowerCase(keyname.charAt(i));
           if ( Character.isLetter(ch) || Character.isDigit(ch) ) {
               sb.append(ch);
           } else {
               sb.append('_');
	   }
       }
       return sb.toString();
    }

    public static String toNull(String str)
    {
       if ( str == null ) return null;
       if ( str.trim().length() < 1 ) return null;
       return str;
    }

    public static void setProperty(Properties props, String key, String value)
    {
        if ( value == null ) return;
        if ( value.trim().length() < 1 ) return;
        props.setProperty(key, value);
    }

    // Basic utility to encode form text - handle the "safe cases"
    public static String htmlspecialchars(String input)
    {
        if ( input == null ) return null;
	String retval = input.replace("&", "&amp;");
	retval = retval.replace("\"", "&quot;");
	retval = retval.replace("<", "&lt;");
	retval = retval.replace(">", "&gt;");
	retval = retval.replace(">", "&gt;");
	retval = retval.replace("=", "&#61;");
	return retval;
    }

}

/* Sample Descriptor 

<?xml version="1.0" encoding="UTF-8"?>
<basic_lti_link
     xmlns="http://www.imsglobal.org/xsd/imsbasiclti_v1p0"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <title>generated by tp+user</title>
  <description>generated by tp+user</description>
  <custom>
    <parameter key="keyname">value</parameter>
  </custom>
  <extensions platform="www.lms.com">
    <parameter key="keyname">value</parameter>
  </extensions>
  <launch_url>url to the basiclti launch URL</launch_url>
  <secure_launch_url>url to the basiclti launch URL</secure_launch_url>
  <icon>url to an icon for this tool (optional)</icon>
  <secure_icon>url to an icon for this tool (optional)</secure_icon>
  <cartridge_icon identifierref="BLTI001_Icon" />
  	  <vendor>
		  <code>vendor.com</code>
        <name>Vendor Name</name>
         <description>
           This is a Grade Book that supports many column types.
         </description>
         <contact>
            <email>support@vendor.com</email>
         </contact>
         <url>http://www.vendor.com/product</url>
	  </vendor>
</basic_lti_link>


*/
