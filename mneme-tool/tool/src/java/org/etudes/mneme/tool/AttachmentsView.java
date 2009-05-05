/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Grid;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Attachment;
import org.etudes.mneme.api.AttachmentService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;

/**
 * The /attachments view for the mneme tool.
 */
public class AttachmentsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AttachmentsView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Attachment service. */
	protected AttachmentService attachmentService = null;

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
		if (params.length != 3)
		{
			throw new IllegalArgumentException();
		}
		String type = params[2];

		// get getting called twice for some reason...
		if (type.equals("null"))
		{
			M_log.warn("get: /null detected");
			uiService.undoPrepareGet(req, res);
			return;
		}

		// collect the attachments in this context
		List<Attachment> attachments = null;

		if (type.equals("image"))
		{
			attachments = this.attachmentService.findImages(AttachmentService.MNEME_APPLICATION, this.toolManager.getCurrentPlacement().getContext(),
					AttachmentService.DOCS_AREA);
		}
		else
		{
			attachments = this.attachmentService.findFiles(AttachmentService.MNEME_APPLICATION, this.toolManager.getCurrentPlacement().getContext(),
					AttachmentService.DOCS_AREA);
		}

		// load them into a 4 column grid
		Grid grid = this.uiService.newGrid();
		grid.setWidth(4);
		grid.load(attachments);

		context.put("attachments", grid);

		context.put("title", messages.getString("title_" + type));

		// render
		uiService.render(ui, context);

		// we do NOT want to be kept track of as the current destination, since we are a popup in another window.
		uiService.undoPrepareGet(req, res);
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
		// one parameter
		if (params.length != 3)
		{
			throw new IllegalArgumentException();
		}
		String type = params[2];

		// get getting called twice for some reason...
		if (type.equals("null"))
		{
			M_log.warn("post: /null detected");
			return;
		}

		// for the upload of attachments
		Upload upload = new Upload(this.toolManager.getCurrentPlacement().getContext(), AttachmentService.DOCS_AREA, this.attachmentService);
		context.put("upload", upload);

		// read the form
		String destination = uiService.decode(req, context);

		// check for file upload error
		boolean uploadError = ((req.getAttribute("upload.status") != null) && (!req.getAttribute("upload.status").equals("ok")));

		// save the attachments upload
		if (upload.getUpload() != null)
		{
			Reference ref = upload.getUpload();
		}

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
