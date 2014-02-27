/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
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

import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.Attachments;
import org.etudes.ambrosia.api.CompareDecision;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.EntityList;
import org.etudes.ambrosia.api.FileUpload;
import org.etudes.ambrosia.api.HtmlEdit;
import org.etudes.ambrosia.api.Instructions;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.PropertyColumn;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Section;
import org.etudes.ambrosia.api.Selection;
import org.etudes.ambrosia.api.Text;
import org.etudes.ambrosia.api.UiService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.StringUtil;

/**
 * EssayQuestionImpl handles questions for the essay question type.
 */
public class EssayQuestionImpl implements TypeSpecificQuestion
{
	/** An enumerate type that declares the types of submissions */
	public enum SubmissionType
	{
		attachments, both, inline, none;
	}

	/** Our messages. */
	protected transient InternationalizedMessages messages = null;

	/** The model answer */
	protected String modelAnswer = null;

	protected transient QuestionPlugin plugin = null;

	/** The question this is a helper for. */
	protected transient Question question = null;

	/** The type of submission */
	protected SubmissionType submissionType = SubmissionType.inline;

	/** Dependency: The UI service (Ambrosia). */
	protected transient UiService uiService = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public EssayQuestionImpl(Question question, EssayQuestionImpl other)
	{
		this.messages = other.messages;
		this.question = other.question;
		this.modelAnswer = other.modelAnswer;
		this.submissionType = other.submissionType;
		this.uiService = other.uiService;
		this.plugin = other.plugin;
	}

	/**
	 * Construct.
	 * 
	 * @param uiService
	 *        the UiService.
	 * @param question
	 *        The Question this is a helper for.
	 */
	public EssayQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
	{
		this.plugin = plugin;
		this.messages = messages;
		this.uiService = uiService;
		this.question = question;
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
			((EssayQuestionImpl) rv).question = question;

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
	public String consolidate(String destination)
	{
		return destination;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getAuthoringUi()
	{
		// submission type
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.submissionType"));
		this.addTypeSelection(type);
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
	public String[] getData()
	{
		String[] rv = new String[2];
		rv[0] = this.submissionType.toString();
		rv[1] = this.modelAnswer;

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getDeliveryUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.question.presentation.text"));

		Attachments attachments = this.uiService.newAttachments();
		attachments.setAttachments(this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments"), null);
		attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments")));

		Section questionSection = this.uiService.newSection();
		questionSection.add(question).add(attachments);

		Section answerSection = this.uiService.newSection();

		// the text entry
		HtmlEdit edit = this.uiService.newHtmlEdit();
		edit.setTitle("answer", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));
		edit.setSize(HtmlEdit.Sizes.tall);
		edit.setIncluded(this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.inline.toString(), SubmissionType.both.toString())
				.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")));
		edit.setProperty(this.uiService.newHtmlPropertyReference().setReference("answer.typeSpecificAnswer.answerData"));
		answerSection.add(edit);

		// the upload
		FileUpload upload = this.uiService.newFileUpload();
		upload.setTitle("upload-title");
		upload.setUpload("upload-button");
		upload.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.upload"));
		upload.setIncluded(this.uiService.newCompareDecision()
				.setEqualsConstant(SubmissionType.attachments.toString(), SubmissionType.both.toString())
				.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")));
		upload.setDestination(this.uiService.newDestination().setDestination("STAY_UPLOAD:{0}",
				this.uiService.newPropertyReference().setReference("answer.question.id")));

		Attachments uploaded = this.uiService.newAttachments();
		uploaded.setAttachments(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded"), "attachment");
		uploaded.setSize(false).setTimestamp(false);
		uploaded.setIncluded(this.uiService.newCompareDecision()
				.setEqualsConstant(SubmissionType.attachments.toString(), SubmissionType.both.toString())
				.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")));

		Navigation remove = this.uiService.newNavigation();
		remove.setTitle("upload-remove").setStyle(Navigation.Style.link).setSubmit().setSmall();
		remove.setIcon("/icons/delete.png", Navigation.IconStyle.none);
		remove.setDestination(this.uiService.newDestination().setDestination("STAY_REMOVE:{0}:{1}",
				this.uiService.newPropertyReference().setReference("answer.question.id"),
				this.uiService.newPropertyReference().setReference("attachment.reference")));
		remove.setConfirm(this.uiService.newTrueDecision(), "cancel", "/icons/cancel.gif", "confirm-remove");
		uploaded.addNavigation(remove);

		answerSection.add(upload).add(uploaded);

		// if no submission
		Instructions noSub = this.uiService.newInstructions();
		noSub.setText("no-submission");
		noSub.setIncluded(this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.none.toString())
				.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")));

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection).add(answerSection).add(noSub);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return this.question.getPresentation().getText();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasCorrect()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasPoints()
	{
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInvalidMessage()
	{
		// we need text
		if (this.question.getPresentation().getText() == null) return "<ul>" + this.messages.getString("invalid") + "</ul>";

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsSurvey()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// we need presentation set
		if (this.question.getPresentation().getText() == null) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	/**
	 * @return the modelAnswer (rich text)
	 */
	public String getModelAnswer()
	{
		return this.modelAnswer;
	}

	/**
	 * {@inheritDoc}
	 */
	public QuestionPlugin getPlugin()
	{
		return this.plugin;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getReviewUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.question.presentation.text"));

		Attachments attachments = this.uiService.newAttachments();
		attachments.setAttachments(this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments"), null);
		attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments")));

		// for grading (single question assessments only) - in a collapsed section
		Section questionSection = this.uiService.newSection();
		questionSection.setMaxHeight(400);
		questionSection.setTreatment("inlay");
		questionSection.setCollapsed(true);
		questionSection.setMinHeight(48);
		questionSection.add(question).add(attachments);
		questionSection.setTitle(getQuestionTitle(), this.uiService.newIconPropertyReference().setIcon("/icons/question_view.png"));
		questionSection.setTitlePlain(this.uiService.newTrueDecision());
		questionSection.setIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("grading")),
				this.uiService.newDecision().setProperty(
						this.uiService.newPropertyReference().setReference("answer.submission.assessment.isSingleQuestion")));

		// for not grading, or for multi-question assessments
		Section questionSection2 = this.uiService.newSection();
		questionSection2.add(question).add(attachments);
		questionSection2.setIncluded(this.uiService.newOrDecision().setOptions(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("grading")).setReversed(),
				this.uiService.newDecision()
						.setProperty(this.uiService.newPropertyReference().setReference("answer.submission.assessment.isSingleQuestion"))
						.setReversed()));

		// submission type (grading only)
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType"));
		addTypeSelection(type);
		type.setReadOnlyCollapsed(this.uiService.newTrueDecision());
		type.setTitle("submission", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));
		type.setIncluded(this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("grading")));

		Section answerSection = this.uiService.newSection();
		answerSection.setTitle("answer", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		// in-line text: for review mode, show the evaluated answer text, if released
		Text answer = this.uiService.newText();
		answer.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.typeSpecificAnswer.answerEvaluatedFormatted"));
		answer.setIncluded(this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.inline.toString(), SubmissionType.both.toString())
				.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")),
				this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.submission.isReleased")),
				this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("review")));
		answerSection.add(answer);
		// or if not released, show the actual answer text
		answer = this.uiService.newText();
		answer.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.typeSpecificAnswer.answerData"));
		answer.setIncluded(
				this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.inline.toString(), SubmissionType.both.toString())
						.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")),
				this.uiService.newDecision().setReversed()
						.setProperty(this.uiService.newPropertyReference().setReference("answer.submission.isReleased")), this.uiService
						.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("review")));
		answerSection.add(answer);

		// in-line text: for grading, command to delete annotation
		Navigation delAnnotation = this.uiService.newNavigation();
		delAnnotation.setTitle("delete-annotation").setStyle(Navigation.Style.link).setSubmit();
		delAnnotation.setIcon("/icons/comments_erase.png", Navigation.IconStyle.left);
		delAnnotation.setDestination(this.uiService.newDestination().setDestination("STAY_DELETE_ANNOTATION:{0}",
				this.uiService.newPropertyReference().setReference("answer.id")));
		delAnnotation.setConfirm(this.uiService.newTrueDecision(), "cancel", "/icons/cancel.gif", "confirm-delete-annotation");

		// for grading, include the editor for marking up the answer (if there is an answer)
		HtmlEdit edit = this.uiService.newHtmlEdit();
		edit.setSize(HtmlEdit.Sizes.tall);
		edit.setProperty(this.uiService.newHtmlPropertyReference().setReference("answer.typeSpecificAnswer.answerEvaluated"));
		edit.setIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("grading")),
				this.uiService.newHasValueDecision().setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answerEvaluated")));
		edit.addAction(delAnnotation);
		answerSection.add(edit);

		// if no in-line text, and type allows it, message
		Instructions notAnswered = this.uiService.newInstructions();
		notAnswered.setText("not-answered");
		notAnswered.setIncluded(
				this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.inline.toString(), SubmissionType.both.toString())
						.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")),
				this.uiService.newHasValueDecision().setReversed()
						.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answerData")));
		answerSection.add(notAnswered);

		// if no in-line text expected, message
		notAnswered = this.uiService.newInstructions();
		notAnswered.setText("no-submission");
		notAnswered.setIncluded(this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.none.toString())
				.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")));
		answerSection.add(notAnswered);

		// attachments display
		Attachments uploaded = this.uiService.newAttachments();
		uploaded.setAttachments(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded"), "attachment");
		uploaded.setSize(false).setTimestamp(false);
		uploaded.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded")));
		answerSection.add(uploaded);

		// if no attachments, and type allows it, message
		notAnswered = this.uiService.newInstructions();
		notAnswered.setText("not-attached");
		notAnswered.setIncluded(
				this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.attachments.toString(), SubmissionType.both.toString())
						.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")),
				this.uiService.newHasValueDecision().setReversed()
						.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded")));
		answerSection.add(notAnswered);

		// model answer
		Text modelAnswer = this.uiService.newText();
		modelAnswer.setText(null,
				this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.question.typeSpecificQuestion.modelAnswer"));

		Section showModelAnswerSection = this.uiService.newSection();
		showModelAnswerSection.setCollapsed(true);
		showModelAnswerSection.setMaxHeight(400);
		showModelAnswerSection.setTreatment("inlay");
		showModelAnswerSection.setTitle("model-answer", this.uiService.newIconPropertyReference().setIcon("/icons/model_answer.png"));
		showModelAnswerSection.setTitlePlain(this.uiService.newTrueDecision());
		showModelAnswerSection.setIncluded(
				this.uiService.newHasValueDecision().setProperty(
						this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.modelAnswer")),
				this.uiService.newOrDecision().setOptions(
						this.uiService.newAndDecision()
								.setRequirements(
										this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("review")),
										this.uiService.newDecision().setProperty(
												this.uiService.newPropertyReference().setReference("submission.isReleased")),
										this.uiService.newDecision().setProperty(
												this.uiService.newPropertyReference().setReference("submission.assessment.showModelAnswer"))),
						this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("grading"))));
		showModelAnswerSection.add(modelAnswer);

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection).add(questionSection2).add(type).add(answerSection)
				.add(showModelAnswerSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public SubmissionType getSubmissionType()
	{
		return this.submissionType;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getUseFeedback()
	{
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getUseHints()
	{
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getUseQuestionPresentation()
	{
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getUseQuestionPresentationAttachments()
	{
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getUseReason()
	{
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewAnswerUi()
	{
		Section answerSection = this.uiService.newSection();
		answerSection.setTitle("answer", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		// command to delete annotation
		Navigation delAnnotation = this.uiService.newNavigation();
		delAnnotation.setTitle("delete-annotation").setStyle(Navigation.Style.link).setSubmit();
		delAnnotation.setIcon("/icons/comments_erase.png", Navigation.IconStyle.left);
		delAnnotation.setDestination(this.uiService.newDestination().setDestination("STAY_DELETE_ANNOTATION:{0}",
				this.uiService.newPropertyReference().setReference("answer.id")));
		delAnnotation.setConfirm(this.uiService.newTrueDecision(), "cancel", "/icons/cancel.gif", "confirm-delete-annotation");

		// editor for marking up the answer
		HtmlEdit answer = this.uiService.newHtmlEdit();
		answer.setSize(HtmlEdit.Sizes.tall);
		answer.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answerEvaluated")));
		answer.setProperty(this.uiService.newHtmlPropertyReference().setReference("answer.typeSpecificAnswer.answerEvaluated"));
		answer.addAction(delAnnotation);
		answerSection.add(answer);

		// if no in-line text, and type allows it, message
		Instructions notAnswered = this.uiService.newInstructions();
		notAnswered.setText("not-answered");
		notAnswered.setIncluded(
				this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.inline.toString(), SubmissionType.both.toString())
						.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")),
				this.uiService.newHasValueDecision().setReversed()
						.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answerData")));
		answerSection.add(notAnswered);

		// if no in-line text expected, message
		notAnswered = this.uiService.newInstructions();
		notAnswered.setText("no-submission");
		notAnswered.setIncluded(this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.none.toString())
				.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")));
		answerSection.add(notAnswered);

		// attachments
		Attachments uploaded = this.uiService.newAttachments();
		uploaded.setAttachments(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded"), "attachment");
		uploaded.setSize(false).setTimestamp(false);
		uploaded.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded")));
		answerSection.add(uploaded);

		// if no attachments, and type allows it, message
		notAnswered = this.uiService.newInstructions();
		notAnswered.setText("not-attached");
		notAnswered.setIncluded(
				this.uiService.newCompareDecision().setEqualsConstant(SubmissionType.attachments.toString(), SubmissionType.both.toString())
						.setProperty(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.submissionType")),
				this.uiService.newHasValueDecision().setReversed()
						.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded")));
		answerSection.add(notAnswered);

		return this.uiService.newFragment().setMessages(this.messages).add(answerSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewDeliveryUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.presentation.text"));

		Attachments attachments = this.uiService.newAttachments();
		attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		Section questionSection = this.uiService.newSection();
		questionSection.add(question).add(attachments);

		// submission type
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.submissionType"));
		addTypeSelection(type);
		type.setReadOnlyCollapsed(this.uiService.newTrueDecision());
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
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.presentation.text"));

		Attachments attachments = this.uiService.newAttachments();
		attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		// for grading
		Section questionSection = this.uiService.newSection();
		questionSection.setMaxHeight(400);
		questionSection.setTreatment("inlay");
		questionSection.setCollapsed(true);
		questionSection.setMinHeight(48);
		questionSection.setTitle(getQuestionTitle(), this.uiService.newIconPropertyReference().setIcon("/icons/question_view.png"));
		questionSection.setTitlePlain(this.uiService.newTrueDecision());
		questionSection.add(question).add(attachments);
		questionSection.setIncluded(this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("grading")));

		// for not grading
		Section questionSection2 = this.uiService.newSection();
		questionSection2.add(question).add(attachments);
		questionSection2.setIncluded(this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("grading"))
				.setReversed());

		// submission type
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.submissionType"));
		addTypeSelection(type);
		type.setReadOnlyCollapsed(this.uiService.newTrueDecision());
		type.setTitle("submission", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		Section typeSection = this.uiService.newSection();
		typeSection.add(type);

		// model answer
		Text modelAnswer = this.uiService.newText();
		modelAnswer.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.typeSpecificQuestion.modelAnswer"));

		Section showModelAnswerSection = this.uiService.newSection();
		showModelAnswerSection.setCollapsed(true);
		showModelAnswerSection.setMaxHeight(400);
		showModelAnswerSection.setTreatment("inlay");
		showModelAnswerSection.setTitle("model-answer", this.uiService.newIconPropertyReference().setIcon("/icons/model_answer.png"));
		showModelAnswerSection.setTitlePlain(this.uiService.newTrueDecision());		
		showModelAnswerSection.setIncluded(this.uiService.newAndDecision().setRequirements(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.modelAnswer")),
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("testprintformat")).setReversed()));
		
		showModelAnswerSection.add(modelAnswer);
		
		Section showModelExpAnswerSection = this.uiService.newSection();
		showModelExpAnswerSection.setMaxHeight(400);
		showModelExpAnswerSection.setTreatment("inlay");
		showModelExpAnswerSection.setTitle("model-answer", this.uiService.newIconPropertyReference().setIcon("/icons/model_answer.png"));
		showModelExpAnswerSection.setTitlePlain(this.uiService.newTrueDecision());
		
		showModelExpAnswerSection.setIncluded(this.uiService.newAndDecision().setRequirements(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.modelAnswer")),
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("testprintformat"))));
		
		showModelExpAnswerSection.add(modelAnswer);

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection).add(questionSection2).add(typeSection)
				.add(showModelAnswerSection).add(showModelExpAnswerSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewStatsUi()
	{
		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);

		PropertyReference iteratorRef = this.uiService.newPropertyReference().setReference("submissions")
				.setFormatDelegate(this.uiService.getFormatDelegate("AccessSubmissionsQuestionScores", "sakai.mneme"));
		entityList.setIterator(iteratorRef, "score");

		entityList.setEmptyTitle("no-answer");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("score.text"));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("score.percent"));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("score.count"));
		entityList.addColumn(propCol);

		Section distributionSection = this.uiService.newSection();
		distributionSection.setTitle("score-distribution");
		distributionSection.add(entityList);

		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.presentation.text"));

		Attachments attachments = this.uiService.newAttachments();
		attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		Section questionSection = this.uiService.newSection();
		questionSection.add(question).add(attachments);

		// submission type
		Selection type = uiService.newSelection();
		type.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.submissionType"));
		addTypeSelection(type);
		type.setReadOnlyCollapsed(this.uiService.newTrueDecision());
		type.setTitle("submission", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		Section typeSection = this.uiService.newSection();
		typeSection.add(type);

		// model answer
		Text modelAnswer = this.uiService.newText();
		modelAnswer.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.typeSpecificQuestion.modelAnswer"));

		
		Section showModelAnswerSection = this.uiService.newSection();
		showModelAnswerSection.setCollapsed(true);
		showModelAnswerSection.setMaxHeight(400);
		showModelAnswerSection.setTreatment("inlay");
		showModelAnswerSection.setTitle("model-answer", this.uiService.newIconPropertyReference().setIcon("/icons/model_answer.png"));
		showModelAnswerSection.setTitlePlain(this.uiService.newTrueDecision());
		showModelAnswerSection.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.modelAnswer")));
		showModelAnswerSection.add(modelAnswer);

		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/your-choice.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.typeSpecificAnswer.answerData");
		Text answer = this.uiService.newText();
		answer.setText("essay-answer", refs);
		
		refs = new PropertyReference[1];
		refs[0] = this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.typeSpecificAnswer.answerData");
		Text answer2 = this.uiService.newText();
		answer2.setText("essay-noimg-answer", refs);
		
		Attachments uploaded = this.uiService.newAttachments();
		uploaded.setAttachments(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded"), "attachment");
		uploaded.setSize(false).setTimestamp(false);
		uploaded.setIncluded(this.uiService.newHasValueDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded")));

		PropertyReference ansIncludedProperty = this.uiService.newPropertyReference().setReference("answer.submission.userId");
		PropertyReference ansIncludedComparison = this.uiService.newPropertyReference().setReference("currentUserId");
		CompareDecision ansIncludedDecision = this.uiService.newCompareDecision();
		ansIncludedDecision.setProperty(ansIncludedProperty);
		ansIncludedDecision.setEqualsProperty(ansIncludedComparison);
		
		Decision gradingDecision = this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("grading"));
		
		Decision orDecision = this.uiService.newOrDecision().setOptions(ansIncludedDecision, gradingDecision);
		
				
		AndDecision showAnswerDecision = this.uiService.newAndDecision();
		Decision[] decisionsShowAnswer = new Decision[2];
		decisionsShowAnswer[0] = this.uiService.newOrDecision().setOptions(
				this.uiService.newHasValueDecision().setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answerData")),
				this.uiService.newHasValueDecision().setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded")));
		decisionsShowAnswer[1] = orDecision;
		showAnswerDecision.setRequirements(decisionsShowAnswer);
		
		Section section = this.uiService.newSection();
		iteratorRef = this.uiService.newPropertyReference().setReference("submissions")
				.setFormatDelegate(this.uiService.getFormatDelegate("AccessSubmissionsQuestionAnswers", "sakai.mneme"));
		section.setIterator(iteratorRef, "answer", this.uiService.newMessage().setMessage("no-answers"));
		/*section.setEntityIncluded(this.uiService.newOrDecision().setOptions(
				this.uiService.newHasValueDecision().setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answerData")),
				this.uiService.newHasValueDecision().setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.uploaded"))));
		*/
		answer.setIncluded(ansIncludedDecision);
		answer2.setIncluded(gradingDecision);
		section.setEntityIncluded(showAnswerDecision);
		
		section.add(answer).add(answer2).add(uploaded);
		section.setTitle("answer-summary");

		Section unansweredSection = this.uiService.newSection();
		unansweredSection.setTitle("unanswered-summary");
		Text unanswered = this.uiService.newText().setText(
				null,
				this.uiService.newHtmlPropertyReference().setFormatDelegate(
						this.uiService.getFormatDelegate("FormatUnansweredPercent", "sakai.mneme")));
		unansweredSection.add(unanswered);

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection).add(typeSection).add(showModelAnswerSection)
				.add(distributionSection).add(section).add(unansweredSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		if ((data != null) && (data.length == 2))
		{
			this.submissionType = SubmissionType.valueOf(data[0]);
			this.modelAnswer = data[1];

			this.question.setChanged();
		}
	}

	/**
	 * Set the model answer.
	 * 
	 * @param modelAnswer
	 *        The model answer. Must be well formed HTML or plain text.
	 */
	public void setModelAnswer(String modelAnswer)
	{
		modelAnswer = StringUtil.trimToNull(modelAnswer);

		this.modelAnswer = modelAnswer;
		this.question.setChanged();
	}

	/**
	 * Set the submission type
	 * 
	 * @param setting
	 *        The submission type.
	 */
	public void setSubmissionType(SubmissionType setting)
	{
		if (setting == null) throw new IllegalArgumentException();
		if (!Different.different(setting, this.submissionType)) return;

		this.submissionType = setting;

		this.question.setChanged();
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
	}

	/**
	 * @return the message bundle selector for the title of the collapsed question text.
	 */
	protected String getQuestionTitle()
	{
		return "view-essay-question";
	}
}
