/**
 * $Id: ValidationLogicDao.java 81430 2010-08-18 14:12:46Z david.horwitz@uct.ac.za $
 * $URL: https://source.sakaiproject.org/svn/reset-pass/trunk/account-validator-impl/src/java/org/sakaiproject/accountvalidator/dao/impl/ValidationLogicDao.java $
 *
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.copyright;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;

public class CopyrightManager implements org.sakaiproject.content.copyright.api.CopyrightManager {

	static final Logger logger = LoggerFactory.getLogger(CopyrightManager.class);

	private static final String SAK_PROP_USE_CUSTOM_COPYRIGHT = "copyright.useCustom";
	private static final String SAK_PROP_USE_CUSTOM_COPYRIGHT_REQ_CHOICE = "copyright.useCustom.requireChoice";
	private static final Boolean SAK_PROP_USE_CUSTOM_COPYRIGHT_DEFAULT = false;
	private static final Boolean SAK_PROP_USE_CUSTOM_COPYRIGHT_REQ_CHOICE_DEFAULT = false;
	private static final String CUSTOM_COPYRIGHT_MSG_BUNDLE_KEY_PREFIX = "custom.copyright.";
	private static final String CUSTOM_COPYRIGHT_MSG_BUNDLE_REQ_CHOICE_KEY = "custom.copyright.requireChoice";
	private static final String CUSTOM_COPYRIGHT_MSG_BUNDLE = "org.sakaiproject.content.copyright.copyright";
	private static final String CUSTOM_COPYRIGHT_LICENSE_FILE_LOCATION = "/library/content/copyright/";
	private static final String CUSTOM_COPYRIGHT_LICENSE_FILE_TYPE = ".html";

	protected boolean active = true;

	private static ResourceBundle rb = null;

	private static Locale locale = null;
	
	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;

	/**
	 * Dependency: ServerConfigurationService.
	 * 
	 * @param service
	 *        The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service) {
		m_serverConfigurationService = service;
	}

	@Override
	public CopyrightInfo getCopyrightInfo(Locale locale, String [] rights, URL serverURL){

		TreeMap<String, String> copyrightMap = new TreeMap<>();

		// If sakai.properties says to use custom copyright, grab all key/values from copyright.properties that start with the defined prefix 'custom.copyright.option.'
		boolean useCustomCopyright = m_serverConfigurationService.getBoolean(SAK_PROP_USE_CUSTOM_COPYRIGHT, SAK_PROP_USE_CUSTOM_COPYRIGHT_DEFAULT);
		if (useCustomCopyright) {
			rb = ResourceBundle.getBundle(CUSTOM_COPYRIGHT_MSG_BUNDLE, locale);
			for (String key : rb.keySet()) {
				if (StringUtils.startsWith(key, CUSTOM_COPYRIGHT_MSG_BUNDLE_KEY_PREFIX) && !CUSTOM_COPYRIGHT_MSG_BUNDLE_REQ_CHOICE_KEY.equals(key)) {
					copyrightMap.put(key, rb.getString(key));
				}
			}
		}

		// If the map is still empty at this point, fall back to the default copyright values passed into this function (provided the array is not null)
		if (copyrightMap.isEmpty()) {
			active = false;
			if (rights != null) {
				for (int i = 0; i < rights.length; i++) {
					copyrightMap.put(i + "", rights[i]);
				}
			}
		}

		String baseURL = getBaseURL(serverURL.getFile());
		CopyrightInfo copyrightInfo = new CopyrightInfo();
		CopyrightManager.locale = locale;
		String language = locale.getLanguage();

		// Loop through the map to build the copyright options
		for (String key : copyrightMap.keySet()) {
			copyrightInfo.add(buildCopyrightItem(key, language, baseURL, copyrightMap));
		}

		/*
		 ****************************** OWL NOTE ******************************
		 *
		 * This block of code breaks OWL because of our historical data (custom copyright keys being stored in the resource's XML blob).
		 * For the time being (and for OWL only) we comment out this block of code. After community contribution, commenting out this block
		 * should be the only local customization we have to carry forward to new versions of Sakai, with regards to using custom copyright options.
		 * See OWL-1752 for more details.
		 */
//		if (useCustomCopyright && !copyrightInfo.getItems().isEmpty()) {
//			boolean customCopyrightRequireChoice = m_serverConfigurationService.getBoolean(SAK_PROP_USE_CUSTOM_COPYRIGHT_REQ_CHOICE, SAK_PROP_USE_CUSTOM_COPYRIGHT_REQ_CHOICE_DEFAULT);
//			if (customCopyrightRequireChoice) {
//				copyrightMap.put(CUSTOM_COPYRIGHT_MSG_BUNDLE_REQ_CHOICE_KEY, rb.getString(CUSTOM_COPYRIGHT_MSG_BUNDLE_REQ_CHOICE_KEY));
//				copyrightInfo.addToBeginning(buildCopyrightItem(CUSTOM_COPYRIGHT_MSG_BUNDLE_REQ_CHOICE_KEY, language, baseURL, copyrightMap));
//			}
//		}

		return copyrightInfo;
	}

	/**
	 * Utility method to build CopyrightItem objects to reduce code duplication.
	 * @param key the key in the map that corresponds to the correct user facing text
	 * @param language the language of the user
	 * @param baseURL the base URL where the HTML file licenses are stored
	 * @param copyrightMap the map of keys to user facing messages
	 * @return a built CopyrightItem object
	 */
	private CopyrightItem buildCopyrightItem(String key, String language, String baseURL, TreeMap<String, String> copyrightMap) {
		CopyrightItem item = new CopyrightItem();

		// If custom copyright options are 'active', try to find the corresponding HTML license file on the file system
		if (active) {
			item.setType(key);
			item.setText(copyrightMap.get(key));
			if (existsFile(CUSTOM_COPYRIGHT_LICENSE_FILE_LOCATION + key + "_" + language + CUSTOM_COPYRIGHT_LICENSE_FILE_TYPE, baseURL)) {
				item.setLicenseUrl(CUSTOM_COPYRIGHT_LICENSE_FILE_LOCATION + key + "_" + language + CUSTOM_COPYRIGHT_LICENSE_FILE_TYPE);
			} else if (existsFile(CUSTOM_COPYRIGHT_LICENSE_FILE_LOCATION + key + CUSTOM_COPYRIGHT_LICENSE_FILE_TYPE, baseURL)) {
				item.setLicenseUrl(CUSTOM_COPYRIGHT_LICENSE_FILE_LOCATION + key + CUSTOM_COPYRIGHT_LICENSE_FILE_TYPE);
			}
		} else {
			String copyrightText = copyrightMap.get(key);
			item.setType(copyrightText);
			item.setText(copyrightText);
		}

		return item;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getCopyrightString( String messageKey )
	{
		if( (rb == null && locale == null) || StringUtils.isBlank( messageKey ) )
		{
			return "";
		}

		String copyright = "";
		try
		{
			if( rb == null && locale != null )
			{
				rb = ResourceBundle.getBundle( "org.sakaiproject.content.copyright.copyright", locale );
			}

			copyright = rb.getString( messageKey );
		}
		catch (MissingResourceException | ClassCastException ex)
		{
			// no copyright bundle or no message found for key, log and continue, will return empty string, OR
			// object found for key was not a string, log and continue, will return empty string
			logger.warn(ex.getMessage(), ex);
		}

		return copyright;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setLocale( Locale locale )
	{
		CopyrightManager.locale = locale;
	}

	public String getUseThisCopyright(String [] rights) {
		if (active) {
			return CopyrightManager.USE_THIS_COPYRIGHT;
		} else {
			if (rights == null || rights.length == 0) {
				return null;
			} else {
				return rights[rights.length-1];
			}
		}
	}

	private String getBaseURL(String serverURL) {
		return serverURL.substring(0,serverURL.indexOf("WEB-INF"))+"..";
	}

	private boolean existsFile(String file,String baseURL) {
		File f = new File(baseURL+file);
		return f.exists();
	}
}
