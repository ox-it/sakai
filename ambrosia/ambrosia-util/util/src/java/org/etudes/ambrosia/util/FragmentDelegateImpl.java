/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.ambrosia.util;

import java.io.IOException;

import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Fragment;
import org.etudes.ambrosia.api.FragmentDelegate;
import org.etudes.ambrosia.api.UiService;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.core.io.ClassPathResource;

/**
 * A FragmentDelegate.
 */
public class FragmentDelegateImpl implements FragmentDelegate
{
	/** Messages bundle name. */
	protected String bundle = null;

	/** The id. */
	protected String id = null;

	/** Localized messages. */
	protected InternationalizedMessages messages = null;

	/** The tool id. */
	protected String toolId = null;

	/** The UI. */
	protected Fragment ui = null;

	/** ui service reference. */
	protected UiService uiService = null;

	/** The view declaration xml path. */
	protected String viewPath = null;

	/**
	 * {@inheritDoc}
	 */
	public Fragment getFragment(Context context, Object focus)
	{
		return this.ui;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public InternationalizedMessages getMessages()
	{
		return this.messages;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// register
		this.uiService.registerFragmentDelegate(this, this.id, this.toolId);

		// messages
		this.messages = new ResourceLoader(this.bundle);

		// interface from XML in the class path
		if (viewPath != null)
		{
			try
			{
				ClassPathResource rsrc = new ClassPathResource(viewPath);
				this.ui = uiService.newFragment(rsrc.getInputStream());
			}
			catch (IOException e)
			{
			}
		}
	}

	/**
	 * Set the message bundle.
	 * 
	 * @param bundle
	 *        The message bundle.
	 */
	public void setBundle(String name)
	{
		this.bundle = name;
	}

	/**
	 * Set the class path to the components XML declaration for the view.
	 * 
	 * @param path
	 *        The class path to the components XML declaration for the view.
	 */
	public void setComponents(String path)
	{
		this.viewPath = path;
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 *        The id.
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Set the tool id.
	 * 
	 * @param id
	 *        The tool id.
	 */
	public void setToolId(String id)
	{
		this.toolId = id;
	}

	/**
	 * Set the UI service.
	 * 
	 * @param service
	 *        The UI service.
	 */
	public void setUi(UiService service)
	{
		this.uiService = service;
	}
}
