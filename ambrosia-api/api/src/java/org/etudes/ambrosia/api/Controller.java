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

package org.etudes.ambrosia.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * Controller handles a view.
 */
public interface Controller
{
	/**
	 * Handle GET
	 * 
	 * @param req
	 *        Servlet request
	 * @param res
	 *        Servlet response
	 * @param context
	 *        Ambrosia context
	 * @param params
	 *        request path, split by "/"
	 */
	void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException;

	/**
	 * Get the content hosting path for embedded media in the UI.
	 * 
	 * @return The content hosting path for embedded media in the UI.
	 */
	String getDocsPath();

	/**
	 * Get the messages.
	 * 
	 * @return The messages.
	 */
	InternationalizedMessages getMessages();

	/**
	 * Access the URL path that addresses this view.
	 * 
	 * @return The URL path.
	 */
	String getPath();

	/**
	 * Get the shared messages used by this controller.
	 * 
	 * @return The shared messages used by this controller.
	 */
	InternationalizedMessages getSharedMessages();

	/**
	 * Handle POST
	 * 
	 * @param req
	 *        Servlet request
	 * @param res
	 *        Servlet response
	 * @param context
	 *        Ambrosia context
	 * @param params
	 *        request path, split by "/"
	 */
	void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException;
}
