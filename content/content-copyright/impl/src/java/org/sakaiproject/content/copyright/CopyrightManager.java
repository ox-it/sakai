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
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;

public class CopyrightManager implements org.sakaiproject.content.copyright.api.CopyrightManager {

	static final Logger logger = LoggerFactory.getLogger(CopyrightManager.class);
	
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
	public org.sakaiproject.content.copyright.api.CopyrightInfo getCopyrightInfo(Locale locale, String [] rights, URL serverURL){
		String baseURL = getBaseURL(serverURL.getFile());
		CopyrightInfo copyrightInfo = new CopyrightInfo();
		String[] copyright_types = m_serverConfigurationService.getStrings("copyright.types");
		if (copyright_types==null) {
			active = false;
			copyright_types = (rights == null)?new String[]{}:rights;
		}

		CopyrightManager.locale = locale;
		rb = ResourceBundle.getBundle("org.sakaiproject.content.copyright.copyright",locale);
		String language = locale.getLanguage();
		for (String copyrightType:copyright_types){
			CopyrightItem item = new CopyrightItem();
			if (active) {
				item.setType(copyrightType);
				item.setText(rb.getString(copyrightType));
				if (existsFile("/library/content/copyright/" + copyrightType + "_" + language + ".html",baseURL)) {
					item.setLicenseUrl("/library/content/copyright/" + copyrightType + "_" + language + ".html");
				} else if (existsFile("/library/content/copyright/" + copyrightType + ".html",baseURL)) {
					item.setLicenseUrl("/library/content/copyright/" + copyrightType + ".html");
				}
			} else {
				item.setType(copyrightType);
				item.setText(copyrightType);
			}
			copyrightInfo.add(item);
		}
		return copyrightInfo;
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
