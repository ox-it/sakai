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

package org.etudes.mneme.impl;

import org.etudes.ambrosia.api.Attachments;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.HtmlEdit;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.Overlay;
import org.etudes.ambrosia.api.Section;
import org.etudes.ambrosia.api.Selection;
import org.etudes.ambrosia.api.Text;
import org.etudes.ambrosia.api.Toggle;
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
	 * {@inheritDoc}
	 */
	public Component getAuthoringUi()
	{
		// submission type
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.submissionType"));
		type.addSelection(this.uiService.newMessage().setMessage("inline"), this.uiService.newMessage().setTemplate("inline"));
		type.addSelection(this.uiService.newMessage().setMessage("inline-attachments"), this.uiService.newMessage().setTemplate("both"));
		type.addSelection(this.uiService.newMessage().setMessage("attachments"), this.uiService.newMessage().setTemplate("attachments"));
		type.addSelection(this.uiService.newMessage().setMessage("no-submission"), this.uiService.newMessage().setTemplate("none"));
		type.setTitle("submission", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		Section typeSection = this.uiService.newSection();
		typeSection.add(type);

		// model answer
		HtmlEdit modelAnswer = this.uiService.newHtmlEdit();
		modelAnswer.setSize(HtmlEdit.Sizes.tall);
		modelAnswer.setProperty(this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.modelAnswer"));
		modelAnswer.setTitle("model-answer-edit", this.uiService.newIconPropertyReference().setIcon("/icons/model_answer.png"));

		Section modelAnswerSection = this.uiService.newSection();
		modelAnswerSection.add(modelAnswer);

		return this.uiService.newFragment().setMessages(this.messages).add(typeSection).add(modelAnswerSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewDeliveryUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setReference("question.presentation.text"));

		Attachments attachments = this.uiService.newAttachments();
		attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		Section questionSection = this.uiService.newSection();
		questionSection.add(question).add(attachments);

		// submission type
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.submissionType"));
		type.addSelection(this.uiService.newMessage().setMessage("inline"), this.uiService.newMessage().setTemplate("inline"));
		type.addSelection(this.uiService.newMessage().setMessage("inline-attachments"), this.uiService.newMessage().setTemplate("both"));
		type.addSelection(this.uiService.newMessage().setMessage("attachments"), this.uiService.newMessage().setTemplate("attachments"));
		type.addSelection(this.uiService.newMessage().setMessage("no-submission"), this.uiService.newMessage().setTemplate("none"));
		type.setReadOnly(this.uiService.newTrueDecision());
		type.setTitle("submission", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		Section typeSection = this.uiService.newSection();
		typeSection.add(type);

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection).add(typeSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewQuestionUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setReference("question.presentation.text"));

		Attachments attachments = this.uiService.newAttachments();
		attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		Section questionSection = this.uiService.newSection();
		questionSection.add(question).add(attachments);

		// submission type
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.submissionType"));
		type.addSelection(this.uiService.newMessage().setMessage("inline"), this.uiService.newMessage().setTemplate("inline"));
		type.addSelection(this.uiService.newMessage().setMessage("inline-attachments"), this.uiService.newMessage().setTemplate("both"));
		type.addSelection(this.uiService.newMessage().setMessage("attachments"), this.uiService.newMessage().setTemplate("attachments"));
		type.addSelection(this.uiService.newMessage().setMessage("no-submission"), this.uiService.newMessage().setTemplate("none"));
		type.setReadOnly(this.uiService.newTrueDecision());
		type.setTitle("submission", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		Section typeSection = this.uiService.newSection();
		typeSection.add(type);

		// model answer
		Text modelAnswerTitle = this.uiService.newText();
		modelAnswerTitle.setText("model-answer", this.uiService.newIconPropertyReference().setIcon("/icons/model_answer.png"));

		Text modelAnswer = this.uiService.newText();
		modelAnswer.setText(null, this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.modelAnswer"));

		// overlay for the model answer
		Overlay modelAnswerOverlay = this.uiService.newOverlay();
		modelAnswerOverlay.setId("modelanswer");
		modelAnswerOverlay.add(modelAnswerTitle).add(modelAnswer).add(this.uiService.newGap());
		modelAnswerOverlay.add(this.uiService.newToggle().setTarget("modelanswer").setTitle("close").setIcon("/icons/close.png",
				Navigation.IconStyle.left));

		// control to show the model answer
		Toggle showModelAnswer = this.uiService.newToggle();
		showModelAnswer.setTarget("modelanswer");
		showModelAnswer.setTitle("view-model-answer");
		showModelAnswer.setIcon("/icons/model_answer.png", Navigation.IconStyle.left);

		Section showModelAnswerSection = this.uiService.newSection();
		showModelAnswerSection.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.modelAnswer")));
		showModelAnswerSection.add(showModelAnswer);

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection).add(typeSection).add(modelAnswerOverlay).add(
				showModelAnswerSection);
	}
}
