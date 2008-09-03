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

package org.etudes.mneme.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.UiService;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.TypeSpecificAnswer;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.ResourceLoader;

/**
 * TaskPlugin handles the Task question type.
 */
public class TaskPlugin implements QuestionPlugin
{
	private static Log M_log = LogFactory.getLog(TaskPlugin.class);

	/** Dependency: AttachmentService */
	protected AttachmentService attachmentService = null;

	/** Messages bundle name. */
	protected String bundle = null;

	/** Localized messages. */
	protected InternationalizedMessages messages = null;

	protected MnemeService mnemeService = null;

	/** Dependency: The UI service (Ambrosia). */
	protected UiService uiService = null;

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getPopularity()
	{
		return Integer.valueOf(40);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "mneme:Task";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTypeName()
	{
		return this.messages.getString("name");
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// messages
		if (this.bundle != null) this.messages = new ResourceLoader(this.bundle);

		// register with Mneme as a question plugin
		this.mnemeService.registerQuestionPlugin(this);

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public TypeSpecificAnswer newAnswer(Answer answer)
	{
		return new TaskAnswerImpl(answer, this.attachmentService);
	}

	/**
	 * {@inheritDoc}
	 */
	public TypeSpecificQuestion newQuestion(Question question)
	{
		return new TaskQuestionImpl(this, this.messages, this.uiService, question);
	}

	/**
	 * Dependency: AttachmentService.
	 * 
	 * @param service
	 *        The AttachmentService.
	 */
	public void setAttachmentService(AttachmentService service)
	{
		attachmentService = service;
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
	 * Dependency: MnemeService.
	 * 
	 * @param service
	 *        The MnemeService.
	 */
	public void setMnemeService(MnemeService service)
	{
		this.mnemeService = service;
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
