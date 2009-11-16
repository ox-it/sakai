/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.Part;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /part_manage view for the mneme tool.
 */
public class PartManageView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(PartManageView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Dependency: AttachmentService. */
	protected AttachmentService attachmentService = null;

	/** Dependency: EntityManager. */
	protected EntityManager entityManager = null;

	/** tool manager reference. */
	protected ToolManager toolManager = null;

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
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// aid, return
		if (params.length < 3)
		{
			throw new IllegalArgumentException();
		}

		String assessmentId = params[2];
		String destination = null;
		if (params.length > 3)
		{
			destination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");
		}

		// if not specified, go to the main assessment_edit view
		else
		{
			destination = "/assessment_edit/" + assessmentId;
		}
		context.put("return", destination);

		final Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// security check
		if (!assessmentService.allowEditAssessment(assessment))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// // clear the assessment of any empty parts (if not mint, which would end up causing it to become a stale mint and vanish!)
		// if (!assessment.getMint())
		// {
		// try
		// {
		// assessment.getParts().removeEmptyParts();
		// this.assessmentService.saveAssessment(assessment);
		// }
		// catch (AssessmentPermissionException e)
		// {
		// // redirect to error
		// res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
		// return;
		// }
		// catch (AssessmentPolicyException e)
		// {
		// // redirect to error
		// res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
		// return;
		// }
		// }

		context.put("parts", assessment.getParts().getParts());

		// collect information: the selected assessment
		context.put("assessment", assessment);

		// value holders for the selection check boxes
		Values values = this.uiService.newValues();
		context.put("ids", values);

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
		// aid, return
		if (params.length < 3)
		{
			throw new IllegalArgumentException();
		}

		String assessmentId = params[2];

		String returnDestination = null;
		if (params.length > 3)
		{
			returnDestination = "/" + StringUtil.unsplit(params, 3, params.length - 3, "/");
		}

		// if not specified, go to the main assessment_edit view
		else
		{
			returnDestination = "/assessment_edit/" + assessmentId;
		}

		final Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// security check
		if (!assessmentService.allowEditAssessment(assessment))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// for editing the parts titles
		PopulatingSet parts = uiService.newPopulatingSet(new Factory()
		{
			public Object get(String id)
			{
				Part part = assessment.getParts().getPart(id);
				return part;
			}
		}, new Id()
		{
			public String getId(Object o)
			{
				return ((Part) o).getId();
			}
		});
		context.put("parts", parts);

		// setup the model: the selected assessment
		context.put("assessment", assessment);

		// value holders for the selection check boxes
		Values values = this.uiService.newValues();
		context.put("ids", values);

		// for the upload of attachments
		Upload upload = new Upload(this.toolManager.getCurrentPlacement().getContext(), AttachmentService.DOCS_AREA, this.attachmentService);
		context.put("upload", upload);

		// read the form
		String destination = uiService.decode(req, context);

		// save the attachments upload
		if (upload.getUpload() != null)
		{
			assessment.getPresentation().addAttachment(upload.getUpload());
		}

		// // handle an attachments remove
		// if (destination.startsWith("REMOVE:"))
		// {
		// String[] parts = StringUtil.split(destination, ":");
		// if (parts.length != 2)
		// {
		// throw new IllegalArgumentException();
		// }
		// String refString = parts[1];
		// Reference ref = this.entityManager.newReference(refString);
		//
		// // remove from the assessment
		// assessment.getPresentation().removeAttachment(ref);
		//
		// // remove the attachment
		// // TODO: really?
		// this.attachmentService.removeAttachment(ref);
		//
		// // stay here
		// destination = context.getDestination();
		// }

		try
		{
			if (destination.equals("ADD"))
			{
				assessment.getParts().addPart();
				this.assessmentService.saveAssessment(assessment);

				destination = context.getDestination();
			}

			else if (destination.equals("DELETE"))
			{
				for (String id : values.getValues())
				{
					Part part = assessment.getParts().getPart(id);
					if (part == null)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
						return;
					}

					// remove part
					assessment.getParts().removePart(part);
				}
				this.assessmentService.saveAssessment(assessment);

				destination = context.getDestination();
			}

			else if (destination.equals("SAVE"))
			{
				this.assessmentService.saveAssessment(assessment);

				destination = context.getDestination();
			}

			else if (destination.equals("INSTRUCTIONS"))
			{
				// save the assessment
				this.assessmentService.saveAssessment(assessment);

				destination = "/instructions_edit/" + assessmentId + "/part_manage/" + assessmentId + returnDestination;
			}

			else
			{
				this.assessmentService.saveAssessment(assessment);
			}
		}
		catch (AssessmentPermissionException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}
		catch (AssessmentPolicyException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
			return;
		}

		// redirect to the next destination
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Set the AttachmentService.
	 * 
	 * @param service
	 *        The AttachmentService.
	 */
	public void setAttachmentService(AttachmentService service)
	{
		this.attachmentService = service;
	}

	/**
	 * Set the EntityManager.
	 * 
	 * @param manager
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager manager)
	{
		entityManager = manager;
	}

	/**
	 * Set the tool manager.
	 * 
	 * @param manager
	 *        The tool manager.
	 */
	public void setToolManager(ToolManager manager)
	{
		toolManager = manager;
	}
}
