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

package org.etudes.mneme.tool;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.util.AmbrosiaServlet;
import org.etudes.mneme.api.AssessmentService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;

/**
 * The Mneme servlet; extending AmbrosiaServlet for a permissions-based default view.
 */
public class MnemeServlet extends AmbrosiaServlet
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(MnemeServlet.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** tool manager reference. */
	protected ToolManager toolManager = null;

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Mneme";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		// self-inject
		this.assessmentService = (AssessmentService) ComponentManager.get(AssessmentService.class);
		this.toolManager = (ToolManager) ComponentManager.get(ToolManager.class);

		M_log.info("init()");
	}

	/**
	 * Get the default view.
	 * 
	 * @return The default view.
	 */
	protected String getDefaultView()
	{
		String context = this.toolManager.getCurrentPlacement().getContext();

		// if the user can manage, start in /assessments
		if (this.assessmentService.allowManageAssessments(context))
		{
			return "assessments";
		}
		
		// otherwise, if the user can see the delivery list, go there
		if (this.assessmentService.allowListDeliveryAssessment(context))
		{
			return "list";
		}

		// see if there is guest access
		if (this.assessmentService.allowGuest(context))
		{
			return "guest_list";
		}

		// no access - let the default view handle it
		return this.defaultView;
	}
}
