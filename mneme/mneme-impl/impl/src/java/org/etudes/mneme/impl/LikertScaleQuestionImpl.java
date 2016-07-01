/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013 Etudes, Inc.
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

import org.etudes.ambrosia.api.CompareDecision;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.EntityList;
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
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * LikertScaleQuestionImpl handles questions for the Likert Scale question type.
 */
public class LikertScaleQuestionImpl implements TypeSpecificQuestion
{
	public class LikertScaleQuestionChoice
	{
		protected String id;

		protected String text;

		public LikertScaleQuestionChoice(String id, String text)
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

		public void setText(String text)
		{
			this.text = text;
		}
	}

	/** Our messages. */
	protected transient InternationalizedMessages messages = null;

	protected transient QuestionPlugin plugin = null;

	/** The question this is a helper for. */
	protected transient Question question = null;

	/** Which scale option to use for this question (0-agree 1-good 2-average 3-yes 4-numbers 5-rocks/sucks) */
	protected Integer scale = new Integer(0);

	/** Dependency: The UI service (Ambrosia). */
	protected transient UiService uiService = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public LikertScaleQuestionImpl(Question question, LikertScaleQuestionImpl other)
	{
		this.scale = other.scale;
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
	public LikertScaleQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
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
			((LikertScaleQuestionImpl) rv).question = question;

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
		// scale
		Selection scale = uiService.newSelection();
		scale.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.scale"));
		scale.setSelectionModel(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.scales"), "scale", this.uiService
				.newMessage().setMessage(null, this.uiService.newPropertyReference().setReference("scale.id")), this.uiService.newMessage()
				.setMessage(null, this.uiService.newPropertyReference().setReference("scale.text")));
		scale.setTitle("scale", this.uiService.newIconPropertyReference().setIcon("/icons/answer.png"));

		// scale section
		Section section = this.uiService.newSection();
		section.add(scale);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
	}

	/**
	 * Access the selected scale choices as an entity (LikertScaleQuestionChoice) list.
	 * 
	 * @return The selected scale choices as an entity (LikertScaleQuestionChoice) list.
	 */
	public List<LikertScaleQuestionChoice> getChoices()
	{
		int optionIndex = this.scale.intValue();
		List<LikertScaleQuestionChoice> rv = null;
		if (optionIndex == 0)
		{
			rv = new ArrayList<LikertScaleQuestionChoice>(5);
			rv.add(new LikertScaleQuestionChoice("0", this.messages.getString("strongly-agree")));
			rv.add(new LikertScaleQuestionChoice("1", this.messages.getString("agree")));
			rv.add(new LikertScaleQuestionChoice("2", this.messages.getString("undecided")));
			rv.add(new LikertScaleQuestionChoice("3", this.messages.getString("disagree")));
			rv.add(new LikertScaleQuestionChoice("4", this.messages.getString("strongly-disagree")));
		}
		if (optionIndex == 1)
		{
			rv = new ArrayList<LikertScaleQuestionChoice>(4);
			rv.add(new LikertScaleQuestionChoice("0", this.messages.getString("excellent")));
			rv.add(new LikertScaleQuestionChoice("1", this.messages.getString("good")));
			rv.add(new LikertScaleQuestionChoice("2", this.messages.getString("poor")));
			rv.add(new LikertScaleQuestionChoice("3", this.messages.getString("unacceptable")));
		}
		if (optionIndex == 2)
		{
			rv = new ArrayList<LikertScaleQuestionChoice>(3);
			rv.add(new LikertScaleQuestionChoice("0", this.messages.getString("above-average")));
			rv.add(new LikertScaleQuestionChoice("1", this.messages.getString("average")));
			rv.add(new LikertScaleQuestionChoice("2", this.messages.getString("below-average")));
		}
		if (optionIndex == 3)
		{
			rv = new ArrayList<LikertScaleQuestionChoice>(2);
			rv.add(new LikertScaleQuestionChoice("0", this.messages.getString("yes")));
			rv.add(new LikertScaleQuestionChoice("1", this.messages.getString("no")));
		}
		if (optionIndex == 4)
		{
			rv = new ArrayList<LikertScaleQuestionChoice>(5);
			rv.add(new LikertScaleQuestionChoice("0", this.messages.getString("five")));
			rv.add(new LikertScaleQuestionChoice("1", this.messages.getString("four")));
			rv.add(new LikertScaleQuestionChoice("2", this.messages.getString("three")));
			rv.add(new LikertScaleQuestionChoice("3", this.messages.getString("two")));
			rv.add(new LikertScaleQuestionChoice("4", this.messages.getString("one")));
		}
		if (optionIndex == 5)
		{
			rv = new ArrayList<LikertScaleQuestionChoice>(2);
			rv.add(new LikertScaleQuestionChoice("0", this.messages.getString("it-rocks")));
			rv.add(new LikertScaleQuestionChoice("1", this.messages.getString("it-sucks")));
		}
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[1];
		rv[0] = this.scale.toString();

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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.choices"), "choice");

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setSingle();
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));

		entityList.addColumn(selCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.text"));
		entityList.addColumn(propCol);

		Section section = this.uiService.newSection();
		section.add(question)/* .add(attachments) */.add(entityList);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
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
		return Boolean.FALSE;
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
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		// we need the text set
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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.choices"), "choice");

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setSingle();

		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		entityList.addColumn(selCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.text"));
		entityList.addColumn(propCol);

		Section section = this.uiService.newSection();
		section.add(question)/* .add(attachments) */.add(entityList);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
	}

	/**
	 * Access the scale.
	 * 
	 * @return The scale.
	 */
	public String getScale()
	{
		return this.scale.toString();
	}

	/**
	 * Access the scales as an entity (LikertScaleQuestionChoice) list.
	 * 
	 * @return The options as an entity (LikertScaleQuestionChoice) list.
	 */
	public List<LikertScaleQuestionChoice> getScales()
	{
		List<LikertScaleQuestionChoice> rv = new ArrayList<LikertScaleQuestionChoice>(5);
		StringBuffer optionText = new StringBuffer();
		optionText.append(this.messages.getString("strongly-agree"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("agree"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("undecided"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("disagree"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("strongly-disagree"));

		rv.add(new LikertScaleQuestionChoice("0", optionText.toString()));
		optionText.setLength(0);

		optionText.append(this.messages.getString("excellent"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("good"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("poor"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("unacceptable"));

		rv.add(new LikertScaleQuestionChoice("1", optionText.toString()));
		optionText.setLength(0);

		optionText.append(this.messages.getString("above-average"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("average"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("below-average"));

		rv.add(new LikertScaleQuestionChoice("2", optionText.toString()));
		optionText.setLength(0);

		optionText.append(this.messages.getString("yes"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("no"));

		rv.add(new LikertScaleQuestionChoice("3", optionText.toString()));
		optionText.setLength(0);

		optionText.append(this.messages.getString("five"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("four"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("three"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("two"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("one"));

		rv.add(new LikertScaleQuestionChoice("4", optionText.toString()));
		optionText.setLength(0);

		optionText.append(this.messages.getString("it-rocks"));
		optionText.append(" / ");
		optionText.append(this.messages.getString("it-sucks"));

		rv.add(new LikertScaleQuestionChoice("5", optionText.toString()));

		return rv;
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
		return Boolean.FALSE;
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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choices"), "choice");

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setSingle();
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		entityList.addColumn(selCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.text"));
		entityList.addColumn(propCol);

		Section section = this.uiService.newSection();
		section.add(question)/* .add(attachments) */.add(entityList);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choices"), "choice");

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setSingle();
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		entityList.addColumn(selCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.text"));
		entityList.addColumn(propCol);

		Section section = this.uiService.newSection();
		section.add(question)/* .add(attachments) */.add(entityList);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
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
		// selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
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

		Text unanswered = this.uiService.newText().setText(
				null,
				this.uiService.newHtmlPropertyReference().setFormatDelegate(
						this.uiService.getFormatDelegate("FormatUnansweredPercent", "sakai.mneme")));

		Section section = this.uiService.newSection();
		section.add(question)/* .add(attachments) */.add(entityList).add(unanswered);

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

		return this.uiService.newFragment().setMessages(this.messages).add(section).add(reasonSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		if ((data != null) && (data.length == 1))
		{
			this.scale = Integer.valueOf(data[0]);

			this.question.setChanged();
		}
	}

	/**
	 * Set the scale
	 * 
	 * @param scale
	 *        The scale number as a string.
	 */
	public void setScale(String scale)
	{
		if (scale == null) throw new IllegalArgumentException();

		Integer s = Integer.valueOf(scale);
		if (!Different.different(this.scale, s)) return;

		this.scale = s;

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
