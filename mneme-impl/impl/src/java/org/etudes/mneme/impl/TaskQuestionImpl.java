/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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

package org.etudes.mneme.impl;

import org.etudes.ambrosia.api.Selection;
import org.etudes.ambrosia.api.UiService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * TaskQuestionImpl handles questions for the Task question type.
 */
public class TaskQuestionImpl extends EssayQuestionImpl
{
	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public TaskQuestionImpl(Question question, TaskQuestionImpl other)
	{
		super(question, other);
	}

	/**
	 * Construct.
	 * 
	 * @param uiService
	 *        the UiService.
	 * @param question
	 *        The Question this is a helper for.
	 */
	public TaskQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
	{
		super(plugin, messages, uiService, question);
		this.submissionType = SubmissionType.none;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone(Question question)
	{
		try
		{
			// get an exact, bit-by-bit copy
			Object rv = super.clone();

			// set the question
			((TaskQuestionImpl) rv).question = question;

			return rv;
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

	/**
	 * Add the type options to the Selection.
	 * 
	 * @param type
	 *        The selection.
	 */
	protected void addTypeSelection(Selection type)
	{
		type.addSelection(this.uiService.newMessage().setMessage("inline"), this.uiService.newMessage().setTemplate("inline"));
		type.addSelection(this.uiService.newMessage().setMessage("inline-attachments"), this.uiService.newMessage().setTemplate("both"));
		type.addSelection(this.uiService.newMessage().setMessage("attachments"), this.uiService.newMessage().setTemplate("attachments"));
		type.addSelection(this.uiService.newMessage().setMessage("no-submission"), this.uiService.newMessage().setTemplate("none"));
	}

	/**
	 * @return the message bundle selector for the title of the collapsed question text.
	 */
	protected String getQuestionTitle()
	{
		return "view-task-question";
	}
}
