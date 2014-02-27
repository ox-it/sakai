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

import java.util.ArrayList;
import java.util.List;

import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.CompareDecision;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.EntityList;
import org.etudes.ambrosia.api.OrDecision;
import org.etudes.ambrosia.api.PropertyColumn;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Section;
import org.etudes.ambrosia.api.Selection;
import org.etudes.ambrosia.api.SelectionColumn;
import org.etudes.ambrosia.api.Text;
import org.etudes.ambrosia.api.UiService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.etudes.mneme.impl.MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.FormattedText;

/**
 * TrueFalseQuestionImpl handles questions for the true/false question type.
 */
public class TrueFalseQuestionImpl implements TypeSpecificQuestion
{
	public class TrueFalseQuestionChoice
	{
		protected String id;

		protected String text;

		public TrueFalseQuestionChoice(MultipleChoiceQuestionChoice other)
		{
			this.id = other.id;
			this.text = other.text;
		}

		public TrueFalseQuestionChoice(String id, String text)
		{
			this.id = id;
			this.text = text;
		}

		public String getId()
		{
			return this.id;
		}

		public String getText()
		{
			return this.text;
		}
	}

	/** The correct answer: TRUE or FALSE. */
	protected Boolean correctAnswer = Boolean.TRUE;

	protected transient InternationalizedMessages messages = null;

	protected transient QuestionPlugin plugin = null;

	/** The question this is a helper for. */
	protected transient Question question = null;

	/** Dependency: The UI service (Ambrosia). */
	protected transient UiService uiService = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public TrueFalseQuestionImpl(Question question, TrueFalseQuestionImpl other)
	{
		this.correctAnswer = other.correctAnswer;
		this.messages = other.messages;
		this.question = question;
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
	public TrueFalseQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
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

			// nothing to deep copy

			// set the question
			((TrueFalseQuestionImpl) rv).question = question;

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
	public String getAnswerKey()
	{
		return this.correctAnswer ? this.messages.getString("true") : this.messages.getString("false");
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getAuthoringUi()
	{
		Selection selection = this.uiService.newSelection();
		selection.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswer"));
		selection.addSelection(this.uiService.newMessage().setMessage("true"), this.uiService.newMessage().setTemplate("true"));
		selection.addSelection(this.uiService.newMessage().setMessage("false"), this.uiService.newMessage().setTemplate("false"));
		selection.setTitle("correct-answer", this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png"));
		selection.setReadOnly(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.isSurvey")));

		Section section = this.uiService.newSection();
		section.add(selection);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
	}

	/**
	 * Access the choices as an entity (TrueFalseQuestionChoice) list.
	 * 
	 * @return The choices as an entity (TrueFalseQuestionChoice) list.
	 */
	public List<TrueFalseQuestionChoice> getChoices()
	{
		// get the list in order
		List<TrueFalseQuestionChoice> rv = new ArrayList<TrueFalseQuestionChoice>(2);
		rv.add(new TrueFalseQuestionChoice("true", this.messages.getString("true")));
		rv.add(new TrueFalseQuestionChoice("false", this.messages.getString("false")));

		return rv;
	}

	/**
	 * Access the correct answer as a string.
	 * 
	 * @return The correct answer.
	 */
	public String getCorrectAnswer()
	{
		if (!this.question.getHasCorrect())
		{
			return "";
		}

		return this.correctAnswer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[1];
		rv[0] = this.correctAnswer.toString();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getDeliveryUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.question.presentation.text"));

		// Attachments attachments = this.uiService.newAttachments();
		// attachments.setAttachments(this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments"), null);
		// attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
		// this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments")));

		Selection selection = this.uiService.newSelection();
		selection.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));
		selection.addSelection(this.uiService.newMessage().setMessage("true"), this.uiService.newMessage().setTemplate("true"));
		selection.addSelection(this.uiService.newMessage().setMessage("false"), this.uiService.newMessage().setTemplate("false"));

		Section section = this.uiService.newSection();
		section.add(question)/* .add(attachments) */.add(selection);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return FormattedText.convertFormattedTextToPlaintext(this.question.getPresentation().getText());
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasCorrect()
	{
		return Boolean.TRUE;
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
		// we need text
		if (this.question.getPresentation().getText() == null) return Boolean.FALSE;

		return Boolean.TRUE;
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

		// Attachments attachments = this.uiService.newAttachments();
		// attachments.setAttachments(this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments"), null);
		// attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
		// this.uiService.newPropertyReference().setReference("answer.question.presentation.attachments")));

		Selection selection = this.uiService.newSelection();
		selection.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));
		selection.addSelection(this.uiService.newMessage().setMessage("true"), this.uiService.newMessage().setTemplate("true"));
		selection.addSelection(this.uiService.newMessage().setMessage("false"), this.uiService.newMessage().setTemplate("false"));
		selection.setReadOnly(this.uiService.newTrueDecision());
		selection.setCorrect(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.correctAnswer"));

		// should we show correct marks?
		AndDecision mayReviewAndShowCorrect = this.uiService.newAndDecision();
		Decision[] decisionsMayReviewAndShowCorrect = new Decision[2];
		decisionsMayReviewAndShowCorrect[0] = this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.submission.mayReview"));
		decisionsMayReviewAndShowCorrect[1] = this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.showCorrectReview"));
		mayReviewAndShowCorrect.setRequirements(decisionsMayReviewAndShowCorrect);

		OrDecision or = this.uiService.newOrDecision();
		Decision[] decisionsOr = new Decision[2];
		decisionsOr[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("grading"));
		decisionsOr[1] = mayReviewAndShowCorrect;
		or.setOptions(decisionsOr);

		Decision[] decisionsShowCorrect = new Decision[2];
		decisionsShowCorrect[0] = this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect"));
		decisionsShowCorrect[1] = or;
		Decision showCorrect = this.uiService.newAndDecision().setRequirements(decisionsShowCorrect);

		selection.setCorrectDecision(showCorrect);

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("answer.question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Decision[] innerAndInc = new Decision[2];
		innerAndInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.showCorrectReview"));
		innerAndInc[1] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("review"));
		Decision[] orInc = new Decision[2];
		orInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("grading"));
		orInc[1] = this.uiService.newAndDecision().setRequirements(innerAndInc);
		Decision[] andInc = new Decision[4];
		andInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.question.hasCorrect"));
		andInc[1] = this.uiService.newOrDecision().setOptions(orInc);
		andInc[2] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("showIncorrect")).setReversed();
		andInc[3] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.question.part.assessment.allowedPoints"));
		
		answerKey.setIncluded(this.uiService.newAndDecision().setRequirements(andInc));

		Section first = this.uiService.newSection();
		first.add(question)/* .add(attachments) */.add(selection);

		Section second = this.uiService.newSection();
		second.add(answerKey);

		return this.uiService.newFragment().setMessages(this.messages).add(first).add(second);
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
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getUseReason()
	{
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewAnswerUi()
	{
		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.choices"), "choice");
		entityList.setEmptyTitle("no-answer");

		// include each choice only if the choice has been selected by the user
		PropertyReference entityIncludedProperty = this.uiService.newPropertyReference().setReference("choice.id");
		PropertyReference entityIncludedComparison = this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer");
		CompareDecision entityIncludedDecision = this.uiService.newCompareDecision();
		entityIncludedDecision.setProperty(entityIncludedProperty);
		entityIncludedDecision.setEqualsProperty(entityIncludedComparison);
		entityList.setEntityIncluded(entityIncludedDecision);

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setSingle();
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		selCol.setCorrect(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.correctAnswer"));
		selCol.setCorrectDecision(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));
		entityList.addColumn(selCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.text"));
		entityList.addColumn(propCol);

		return this.uiService.newFragment().setMessages(this.messages).add(entityList);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewDeliveryUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.presentation.text"));

		// Attachments attachments = this.uiService.newAttachments();
		// attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		// attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
		// this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		Selection selection = this.uiService.newSelection();
		selection.addSelection(this.uiService.newMessage().setMessage("true"), this.uiService.newMessage().setTemplate("true"));
		selection.addSelection(this.uiService.newMessage().setMessage("false"), this.uiService.newMessage().setTemplate("false"));
		selection.setReadOnly(this.uiService.newTrueDecision());

		Section first = this.uiService.newSection();
		first.add(question)/* .add(attachments) */.add(selection);

		return this.uiService.newFragment().setMessages(this.messages).add(first);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewQuestionUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.presentation.text"));

		// Attachments attachments = this.uiService.newAttachments();
		// attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		// attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
		// this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		Selection selection = this.uiService.newSelection();
		selection.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswer"));
		selection.addSelection(this.uiService.newMessage().setMessage("true"), this.uiService.newMessage().setTemplate("true"));
		selection.addSelection(this.uiService.newMessage().setMessage("false"), this.uiService.newMessage().setTemplate("false"));
		selection.setReadOnly(this.uiService.newTrueDecision());
		selection.setCorrect(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswer"));
		selection.setCorrectDecision(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.hasCorrect")));

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section first = this.uiService.newSection();
		first.add(question)/* .add(attachments) */.add(selection);

		Section second = this.uiService.newSection();
		second.setIncluded(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		second.add(answerKey);

		return this.uiService.newFragment().setMessages(this.messages).add(first).add(second);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewStatsUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.presentation.text"));

		// Attachments attachments = this.uiService.newAttachments();
		// attachments.setAttachments(this.uiService.newPropertyReference().setReference("question.presentation.attachments"), null);
		// attachments.setIncluded(this.uiService.newHasValueDecision().setProperty(
		// this.uiService.newPropertyReference().setReference("question.presentation.attachments")));

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choices"), "choice");
		entityList.setEmptyTitle("no-answer");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.id")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatChoice", "sakai.mneme")));
		entityList.addColumn(propCol);

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setSingle();
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswer"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		selCol.setCorrect(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswer"));
		selCol.setCorrectDecision(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		entityList.addColumn(selCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.text"));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.id")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatPercent", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.id")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatCount", "sakai.mneme")));
		entityList.addColumn(propCol);

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Text unanswered = this.uiService.newText().setText(
				null,
				this.uiService.newHtmlPropertyReference().setFormatDelegate(
						this.uiService.getFormatDelegate("FormatUnansweredPercent", "sakai.mneme")));

		Section first = this.uiService.newSection();
		first.add(question)/* .add(attachments) */.add(entityList).add(unanswered);

		Section second = this.uiService.newSection();
		second.setIncluded(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		second.add(answerKey);

		// show collected reasons, if reason is being collected
		Section reasonSection = this.uiService.newSection();
		PropertyReference iteratorRef = this.uiService.newPropertyReference().setReference("submissions")
				.setFormatDelegate(this.uiService.getFormatDelegate("AccessSubmissionsQuestionReasons", "sakai.mneme"));
		reasonSection.setIterator(iteratorRef, "answer", this.uiService.newMessage().setMessage("no-reasons"));
		Text reason = this.uiService.newText();
		reason.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.reason"));
		reasonSection.add(reason);
		reasonSection.setTitle("answer-reason");
		reasonSection.setIncluded(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.explainReason")));

		return this.uiService.newFragment().setMessages(this.messages).add(first).add(second).add(reasonSection);
	}

	/**
	 * Set the correct answer, as a Boolean string.
	 * 
	 * @param correctAnswer
	 *        The correct answer.
	 */
	public void setCorrectAnswer(String correctAnswer)
	{
		if (correctAnswer == null) throw new IllegalArgumentException();

		Boolean b = Boolean.valueOf(correctAnswer);
		if (!Different.different(b, this.correctAnswer)) return;

		this.correctAnswer = b;

		this.question.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		if ((data != null) && (data.length == 1))
		{
			this.correctAnswer = Boolean.valueOf(data[0]);
		}

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
}
