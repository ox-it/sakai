/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.tool;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;

/**
 * The /error view for the mneme tool.
 */
public class ErrorView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ErrorView.class);

	/** Dependency: SubmissionService. */
	protected SubmissionService submissionService = null;

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
	{
		// we would like a single parameter (error code), and perhaps one more
		String errorCode = null;
		String param = null;
		if (params.length >= 3)
		{
			errorCode = params[2];

			if (params.length >= 4)
			{
				param = params[3];
			}
		}

		// which error?
		Errors error = Errors.unknown;
		if (errorCode != null)
		{
			try
			{
				error = Errors.valueOf(errorCode);
			}
			catch (IllegalArgumentException e)
			{
			}
		}

		switch (error)
		{
			case invalid:
			{
				context.put("invalidUrl", (context.getPreviousDestination() == null) ? "" : context.getPreviousDestination());
				break;
			}

			case invalidpost:
			{
				context.put("invalidPost", Boolean.TRUE);
				break;
			}

			case unauthorized:
			{
				context.put("unauthorized", Boolean.TRUE);
				break;
			}

			case policy:
			{
				context.put("policy", Boolean.TRUE);
				break;
			}

			case unexpected:
			{
				context.put("unexpected", Boolean.TRUE);
				break;
			}

			case linear:
			{
				context.put("unauthorized", Boolean.TRUE);

				if (param != null)
				{
					// treat the param as a submission id
					Submission s = submissionService.getSubmission(param);
					if (s != null)
					{
						Question question = s.getFirstIncompleteQuestion();
						if (question != null)
						{
							// next destination: first question of submission
							String destination = "/question/" + s.getId() + "/q" + question.getId();
							context.put("testUrl", destination);
						}
					}
				}
				break;
			}

			case upload:
			{
				context.put("upload", Boolean.TRUE);

				// let them re-enter where they were
				context.put("testUrl", (context.getPreviousDestination() == null) ? "" : context.getPreviousDestination());

				// the size (megs) that was exceeded
				context.put("uploadMax", param);
				break;
			}

			case over:
			{
				context.put("over", Boolean.TRUE);
				break;
			}

			case closed:
			{
				context.put("closed", Boolean.TRUE);
				break;
			}

			case password:
			{
				context.put("password", Boolean.TRUE);
				break;
			}

			case pledge:
			{
				context.put("pledge", Boolean.TRUE);
				break;
			}
		}

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Set the submission service.
	 * 
	 * @param service
	 *        The submission service.
	 */
	public void setSubmissionService(SubmissionService service)
	{
		this.submissionService = service;
	}
}
