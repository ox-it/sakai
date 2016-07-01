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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.AutoColumn;
import org.etudes.ambrosia.api.CompareDecision;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.EntityList;
import org.etudes.ambrosia.api.EntityListColumn;
import org.etudes.ambrosia.api.HtmlEdit;
import org.etudes.ambrosia.api.Instructions;
import org.etudes.ambrosia.api.Navigation;
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
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.StringUtil;

/**
 * MultipleChoiceQuestionImpl handles questions for the multiple choice question type.
 */
public class MultipleChoiceQuestionImpl implements TypeSpecificQuestion
{
	public class MultipleChoiceQuestionChoice
	{
		protected Boolean correct = Boolean.FALSE;

		protected String id = null;

		protected Question myQuestion = null;

		protected String text = null;

		public MultipleChoiceQuestionChoice(Question question, MultipleChoiceQuestionChoice other)
		{
			this.correct = other.correct;
			this.id = other.id;
			this.text = other.text;
			this.myQuestion = question;
		}

		public MultipleChoiceQuestionChoice(Question question, String id, String text)
		{
			this.id = id;
			this.myQuestion = question;
			this.text = StringUtil.trimToNull(text);
		}

		public Boolean getCorrect()
		{
			return this.correct;
		}

		public String getId()
		{
			return this.id;
		}

		public String getText()
		{
			return this.text;
		}

		public void setCorrect(Boolean correct)
		{
			if (correct == null) throw new IllegalArgumentException();

			if (!Different.different(correct, this.correct)) return;

			this.correct = correct;

			this.myQuestion.setChanged();
		}

		public void setText(String text)
		{
			this.text = StringUtil.trimToNull(text);
			this.myQuestion.setChanged();
		}

		protected void initCorrect()
		{
			this.correct = Boolean.TRUE;
		}
	}

	/** The maximum number of choices we support. */
	protected final static int MAX = 25;

	/** List of choices */
	protected List<MultipleChoiceQuestionChoice> answerChoices = new ArrayList<MultipleChoiceQuestionChoice>();

	/** Our messages. */
	protected transient InternationalizedMessages messages = null;

	protected transient QuestionPlugin plugin = null;

	/** The question this is a helper for. */
	protected transient Question question = null;

	/** The shuffle choices setting. */
	protected Boolean shuffleChoices = Boolean.FALSE;

	/** TRUE means single correct answer, FALSE means multiple correct answers */
	protected Boolean singleCorrect = Boolean.TRUE;

	/** Dependency: The UI service (Ambrosia). */
	protected transient UiService uiService = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public MultipleChoiceQuestionImpl(Question question, MultipleChoiceQuestionImpl other)
	{
		this.answerChoices = new ArrayList<MultipleChoiceQuestionChoice>(other.answerChoices.size());
		for (MultipleChoiceQuestionChoice choice : other.answerChoices)
		{
			this.answerChoices.add(new MultipleChoiceQuestionChoice(question, choice));
		}
		this.messages = other.messages;
		this.question = question;
		this.shuffleChoices = other.shuffleChoices;
		this.singleCorrect = other.singleCorrect;
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
	public MultipleChoiceQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
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

			// deep copy these
			((MultipleChoiceQuestionImpl) rv).answerChoices = new ArrayList<MultipleChoiceQuestionChoice>(this.answerChoices.size());
			for (MultipleChoiceQuestionChoice choice : this.answerChoices)
			{
				((MultipleChoiceQuestionImpl) rv).answerChoices.add(new MultipleChoiceQuestionChoice(question, choice));
			}

			// set the question
			((MultipleChoiceQuestionImpl) rv).question = question;

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
		boolean stayHere = false;
		boolean removeBlanks = true;

		// check for delete
		if (destination.startsWith("DEL:"))
		{
			removeBlanks = false;
			stayHere = true;

			String[] parts = StringUtil.split(destination, ":");
			if (parts.length == 2)
			{
				List newChoices = new ArrayList<MultipleChoiceQuestionChoice>();
				int i = 0;
				for (MultipleChoiceQuestionChoice choice : this.answerChoices)
				{
					// ignore the deleted one
					if (!choice.getId().equals(parts[1]))
					{
						// new position
						choice.id = Integer.toString(i++);
						newChoices.add(choice);
					}
				}

				this.answerChoices = newChoices;

				question.setChanged();
			}
		}

		// add more choices
		if (destination.startsWith("ADD:") || destination.startsWith("INIT:"))
		{
			removeBlanks = false;
			stayHere = true;

			String[] parts = StringUtil.split(destination, ":");
			if (parts.length == 2)
			{
				try
				{
					int more = Integer.parseInt(parts[1]);
					if ((this.answerChoices.size() + more) > this.MAX)
					{
						more = this.MAX - this.answerChoices.size();
					}
					if (more > 0)
					{
						int i = this.answerChoices.size();
						for (int count = 0; count < more; count++)
						{
							MultipleChoiceQuestionChoice choice = new MultipleChoiceQuestionChoice(this.question, Integer.toString(i++), "");
							if ((count == 0) && (destination.startsWith("INIT:")))
							{
								choice.initCorrect();
							}
							this.answerChoices.add(choice);
						}

						// if init, this is not enough to set as changed
						if (!destination.startsWith("INIT:"))
						{
							this.question.setChanged();
						}
					}
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		if (destination.startsWith("STAY"))
		{
			removeBlanks = false;
			stayHere = true;
		}

		// remove any blank choices unless we don't want to
		if (removeBlanks)
		{
			List newChoices = new ArrayList<MultipleChoiceQuestionChoice>();
			int i = 0;
			boolean removed = false;
			for (MultipleChoiceQuestionChoice choice : this.answerChoices)
			{
				// ignore the empty ones
				if (choice.getText() != null)
				{
					// new position
					choice.id = Integer.toString(i++);
					newChoices.add(choice);
				}
				else
				{
					removed = true;
				}
			}

			if (removed)
			{
				this.answerChoices = newChoices;

				// if mint, this is not enough to trigger a changed
				if (!this.question.getMint())
				{
					this.question.setChanged();
				}
			}
		}

		// make sure there's only one correct if we are single select
		boolean seenCorrect = false;
		for (MultipleChoiceQuestionChoice choice : this.answerChoices)
		{
			if (!seenCorrect)
			{
				if (choice.getCorrect())
				{
					seenCorrect = true;
				}
			}
			else
			{
				// make sure single correct has only one correct
				if (choice.getCorrect() && this.singleCorrect)
				{
					choice.setCorrect(Boolean.FALSE);
				}
			}
		}

		// make sure we have at least one
		if (!seenCorrect)
		{
			if (!this.answerChoices.isEmpty())
			{
				this.answerChoices.get(0).setCorrect(Boolean.TRUE);
			}
		}

		if (stayHere) return null;
		return destination;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAnswerKey()
	{
		StringBuffer rv = new StringBuffer();

		// get the choices as would be presented in delivery
		List<MultipleChoiceQuestionChoice> choices = getChoices();

		// that's the A, B, C order, so find each correct one
		int i = 0;
		for (MultipleChoiceQuestionChoice choice : choices)
		{
			if (choice.getCorrect())
			{
				// TODO: hard coding our A, B, Cs?
				rv.append((char) ('A' + i));
				rv.append(",");
			}
			i++;
		}

		if (rv.length() > 0) rv.setLength(rv.length() - 1);
		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getAuthoringUi()
	{
		// single or multiple answers
		Selection singleMultiple = uiService.newSelection();
		singleMultiple.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.singleCorrect"));
		singleMultiple.addSelection(this.uiService.newMessage().setMessage("single-choice"), this.uiService.newMessage().setTemplate("true"));
		singleMultiple.addSelection(this.uiService.newMessage().setMessage("multiple-select"), this.uiService.newMessage().setTemplate("false"));
		singleMultiple.setDestination(this.uiService.newDestination().setDestination("STAY"));
		singleMultiple.setTitle("answer", this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png"));
		
		// answer section// add more choices control
		Selection addMore = uiService.newSelection();
		addMore.addSelection(this.uiService.newMessage().setMessage("none"), this.uiService.newMessage().setTemplate("ADD:0"));
		addMore.addSelection(this.uiService.newMessage().setMessage("one"), this.uiService.newMessage().setTemplate("ADD:1"));
		addMore.addSelection(this.uiService.newMessage().setMessage("two"), this.uiService.newMessage().setTemplate("ADD:2"));
		addMore.addSelection(this.uiService.newMessage().setMessage("three"), this.uiService.newMessage().setTemplate("ADD:3"));
		addMore.addSelection(this.uiService.newMessage().setMessage("four"), this.uiService.newMessage().setTemplate("ADD:4"));
		addMore.addSelection(this.uiService.newMessage().setMessage("five"), this.uiService.newMessage().setTemplate("ADD:5"));
		addMore.setOrientation(Selection.Orientation.dropdown);
		addMore.setSubmitValue();
		addMore.setTitle("more-choices");
		addMore.setIncluded(this.uiService.newDecision().setReversed()
				.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesMaxedOut")));

		Instructions noMore = uiService.newInstructions();
		noMore.setText("no-more");
		noMore.setIncluded(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesMaxedOut")));

		// shuffle choices
		Selection shuffle = this.uiService.newSelection();
		shuffle.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.shuffleChoices"));
		shuffle.addSelection(this.uiService.newMessage().setMessage("shuffle"), this.uiService.newMessage().setTemplate("true"));

		Section answer = this.uiService.newSection();
		answer.add(singleMultiple).add(addMore).add(noMore).add(shuffle);

		// listing of choices
		EntityList choicesList = this.uiService.newEntityList();
		choicesList.setStyle(EntityList.Style.form);
		choicesList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesAsAuthored"), "choice");

		// selection column
		SelectionColumn correct = this.uiService.newSelectionColumn();
		if (this.singleCorrect)
		{
			correct.setSingle();
		}
		else
		{
			correct.setMultiple();
			correct.setSelectAll(false);
		}
		correct.setLabel("correct");
		correct.setValueProperty(this.uiService.newPropertyReference().setReference("choice.id"));
		correct.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswers"));
		correct.setIncluded(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.isSurvey"))
				.setReversed());
		choicesList.addColumn(correct);
		
		
		// A. B. C. column
		AutoColumn autoCol = this.uiService.newAutoColumn();
		choicesList.addColumn(autoCol);

		// choice text column
		EntityListColumn choiceText = this.uiService.newEntityListColumn();
		HtmlEdit edit = this.uiService.newHtmlEdit();
		edit.setSize(HtmlEdit.Sizes.small);
		edit.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.text"));
		choiceText.add(edit);
		choicesList.addColumn(choiceText);

		// delete nav column
		EntityListColumn deleteCol = this.uiService.newEntityListColumn();
		Navigation nav = this.uiService.newNavigation();
		Destination destination = this.uiService.newDestination();
		destination.setDestination("DEL:{0}", this.uiService.newPropertyReference().setReference("choice.id"));
		nav.setTitle("delete").setIcon("/icons/delete.png", Navigation.IconStyle.left).setStyle(Navigation.Style.link).setSubmit()
				.setDestination(destination);
		deleteCol.add(nav);
		choicesList.addColumn(deleteCol);
		
		// choices section
		Section choices = this.uiService.newSection();
		choices.setTitle("choices");
		choices.add(choicesList);

		return this.uiService.newFragment().setMessages(this.messages).add(answer).add(choices);
	}

	/**
	 * Access the choices as an entity (MultipleChoiceQuestionChoice) list.
	 * 
	 * @return The choices as an entity (MultipleChoiceQuestionChoice) list.
	 */
	public List<MultipleChoiceQuestionChoice> getChoices()
	{
		// get the list in order
		List<MultipleChoiceQuestionChoice> rv = getChoicesAsAuthored();

		// shuffle them if desired (and we are in a submission context)
		if (shuffleChoicesSetting() && (this.question.getPart() != null) && (this.question.getPart().getAssessment().getSubmissionContext() != null))
		{
			// set the seed based on the submission id
			long seed = (this.question.getId() + "_" + this.question.getPart().getAssessment().getSubmissionContext().getId()).hashCode();

			// mix up the answers
			Collections.shuffle(rv, new Random(seed));
		}

		return rv;
	}

	/**
	 * Access the actual choices as authored. The choices in the list can be altered, changing the question definition.
	 * 
	 * @return The choices as authored.
	 */
	public List<MultipleChoiceQuestionChoice> getChoicesAsAuthored()
	{
		// if we have no choices yet, start with 4
		if (this.answerChoices.isEmpty())
		{
			consolidate("INIT:5");
		}
		List newChoices = new ArrayList<MultipleChoiceQuestionChoice>();
		for (MultipleChoiceQuestionChoice choice : this.answerChoices)
		{
			newChoices.add(choice);
		}

		this.answerChoices = newChoices;
		return this.answerChoices;
	}

	/**
	 * Check if there are already max choices.
	 * 
	 * @return TRUE if there are already max choices, false if fewer.
	 */
	public Boolean getChoicesMaxedOut()
	{
		return Boolean.valueOf(this.answerChoices.size() >= this.MAX);
	}

	/**
	 * Access the choices, as an entity (MultipleChoiceQuestionChoice) list. Shuffle if set, even if not in a submission context.
	 * 
	 * @return The choices as an entity (MultipleChoiceQuestionChoice) list.
	 */
	public List<MultipleChoiceQuestionChoice> getChoicesShuffled()
	{
		// get the list in order
		List<MultipleChoiceQuestionChoice> rv = getChoicesAsAuthored();

		// shuffle them if desired (and we are in a submission context)
		if (shuffleChoicesSetting() && (this.question.getPart() != null))
		{
			// set the seed
			long seed = this.question.getId().hashCode();

			// mix up the answers
			Collections.shuffle(rv, new Random(seed));
		}

		return rv;
	}

	/**
	 * Access the correct answers as an array.
	 * 
	 * @return The correct answers.
	 */
	public String[] getCorrectAnswers()
	{
		Set<Integer> corrects = getCorrectAnswerSet();
		String[] rv = new String[corrects.size()];
		int index = 0;
		for (Integer correct : corrects)
		{
			rv[index++] = correct.toString();
		}

		return rv;
	}

	/**
	 * Access the correct answers as a set.
	 * 
	 * @return The correct answers.
	 */
	public Set<Integer> getCorrectAnswerSet()
	{
		Set rv = new HashSet<Integer>();
		if (this.question.getHasCorrect())
		{
			for (MultipleChoiceQuestionChoice choice : this.answerChoices)
			{
				if (choice.getCorrect())
				{
					rv.add(Integer.valueOf(choice.getId()));
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[(2 * this.answerChoices.size()) + 2];
		int i = 0;
		rv[i++] = this.singleCorrect.toString();
		rv[i++] = this.shuffleChoices.toString();
		for (MultipleChoiceQuestionChoice choice : this.answerChoices)
		{
			rv[i++] = choice.text;
			rv[i++] = choice.correct.toString();
		}

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
		selCol.setTopped();
		selCol.setNoWrap();
		if (this.singleCorrect)
		{
			selCol.setSingle();
		}
		else
		{
			selCol.setMultiple();
			selCol.setSelectAll(false);
		}
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		entityList.addColumn(selCol);

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setTopped();
		entityList.addColumn(autoCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
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
		boolean invalidText = this.question.getPresentation().getText() == null;
		boolean invalidChoices = this.answerChoices.size() < 2;

		if (!invalidText && !invalidChoices) return null;

		StringBuilder rv = new StringBuilder();
		rv.append("<ul>");

		if (invalidText)
		{
			rv.append(this.messages.getString("invalid-text"));
		}

		if (invalidChoices)
		{
			rv.append(this.messages.getString("invalid-choices"));
		}

		rv.append("</ul>");
		return rv.toString();
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

		// we need 2 or more choices
		if (this.answerChoices.size() < 2) return Boolean.FALSE;

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
		selCol.setTopped();
		selCol.setNoWrap();
		if (this.singleCorrect)
		{
			selCol.setSingle();
		}
		else
		{
			selCol.setMultiple();
			selCol.setSelectAll(false);
		}
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		selCol.setCorrect(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.correctAnswers"));
        CompareDecision compDec = this.uiService.newCompareDecision();
        compDec.setProperty(this.uiService.newPropertyReference().setReference("answer.question.part.assessment.review.showCorrectAnswer"));
        compDec.setEqualsConstant("incorrect_only");
		selCol.setExcludeCorrectMiss(compDec);

		// should we show correct marks?
		AndDecision mayReviewAndShowCorrect = this.uiService.newAndDecision();
		Decision[] decisionsMayReviewAndShowCorrect = new Decision[2];
		decisionsMayReviewAndShowCorrect[0] = this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.submission.mayReview"));
		decisionsMayReviewAndShowCorrect[1] = this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.showCorrectReview"));
		mayReviewAndShowCorrect.setRequirements(decisionsMayReviewAndShowCorrect);

		OrDecision or = this.uiService.newOrDecision();
		Decision[] decisionsOr = new Decision[3];
		decisionsOr[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("grading"));
		decisionsOr[1] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("viewWork"));
		decisionsOr[2] = mayReviewAndShowCorrect;
		or.setOptions(decisionsOr);

		Decision[] decisionsShowCorrect = new Decision[2];
		decisionsShowCorrect[0] = this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect"));
		decisionsShowCorrect[1] = or;
		Decision showCorrect = this.uiService.newAndDecision().setRequirements(decisionsShowCorrect);

		selCol.setCorrectDecision(showCorrect);

		entityList.addColumn(selCol);

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setTopped();
		entityList.addColumn(autoCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);

		/*Decision[] decisionsColorizeCorrectChoice = new Decision[2];
		decisionsColorizeCorrectChoice[0] = showCorrect;
		decisionsColorizeCorrectChoice[1] = this.uiService.newDecision().setProperty(
		this.uiService.newPropertyReference().setReference("choice.correct"));
		Decision decisionsColorize = this.uiService.newAndDecision().setRequirements(decisionsColorizeCorrectChoice);
		entityList.setColorize(decisionsColorize, "#C0FFC0");*/

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
		first.add(question)/* .add(attachments) */.add(entityList);

		Section second = this.uiService.newSection();
		second.add(answerKey);

		return this.uiService.newFragment().setMessages(this.messages).add(first).add(second);
	}

	/**
	 * @return TRUE if we should shuffle choices (set here or in override in the assessment) or FALSE if not.
	 */
	protected Boolean shuffleChoicesSetting()
	{
		Boolean rv = this.shuffleChoices;
		if ((this.question.getPart() != null) && (this.question.getPart().getAssessment() != null))
		{
			if (this.question.getPart().getAssessment().getShuffleChoicesOverride())
			{
				rv = Boolean.TRUE;
			}
		}
		return rv;
	}

	/**
	 * Access the shuffle choice as a string.
	 * 
	 * @return The shuffle choice.
	 */
	public String getShuffleChoices()
	{
		return Boolean.toString(shuffleChoicesSetting());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSingleCorrect()
	{
		return this.singleCorrect.toString();
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
		entityList
				.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.choicesAsAuthored"), "choice");
		entityList.setEmptyTitle("no-answer");

		// include each choice only if the choice has been selected by the user
		PropertyReference entityIncludedProperty = this.uiService.newPropertyReference().setReference("choice.id");
		PropertyReference entityIncludedComparison = this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers");
		CompareDecision entityIncludedDecision = this.uiService.newCompareDecision();
		entityIncludedDecision.setProperty(entityIncludedProperty);
		entityIncludedDecision.setEqualsProperty(entityIncludedComparison);
		entityList.setEntityIncluded(entityIncludedDecision);

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setTopped();
		selCol.setNoWrap();
		if (this.singleCorrect)
		{
			selCol.setSingle();
		}
		else
		{
			selCol.setMultiple();
			selCol.setSelectAll(false);
		}
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		selCol.setCorrect(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.correctAnswers"));
		selCol.setCorrectDecision(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));
		entityList.addColumn(selCol);

		// use the choice id instead of the entity list row number for the auto col (since we removed stuff).
		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setTopped();
		autoCol.setProperty(this.uiService.newPropertyReference().setReference("choice.id"));
		entityList.addColumn(autoCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
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
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesShuffled"), "choice");

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setTopped();
		selCol.setNoWrap();
		if (this.singleCorrect)
		{
			selCol.setSingle();
		}
		else
		{
			selCol.setMultiple();
			selCol.setSelectAll(false);
		}
		selCol.setValueProperty(this.uiService.newPropertyReference().setReference("choice.id"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		entityList.addColumn(selCol);

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setTopped();
		entityList.addColumn(autoCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);

		Section first = this.uiService.newSection();
		first.add(question)/* .add(attachments) */.add(entityList);

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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesAsAuthored"), "choice");

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setTopped();
		selCol.setNoWrap();
		if (this.singleCorrect)
		{
			selCol.setSingle();
		}
		else
		{
			selCol.setMultiple();
			selCol.setSelectAll(false);
		}
		selCol.setValueProperty(this.uiService.newPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswers"));
		selCol.setReadOnly(this.uiService.newTrueDecision());
		selCol.setCorrect(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswers"));
		selCol.setCorrectDecision(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		entityList.addColumn(selCol);

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setTopped();
		entityList.addColumn(autoCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section first = this.uiService.newSection();
		first.add(question)/* .add(attachments) */.add(entityList);

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
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesAsAuthored"), "choice");
		entityList.setEmptyTitle("no-answer");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.id")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatChoice", "sakai.mneme")));
		entityList.addColumn(propCol);

		
		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setTopped();
		selCol.setNoWrap();
		if (this.singleCorrect)
		{
			selCol.setSingle();
		}
		else
		{
			selCol.setMultiple();
			selCol.setSelectAll(false);
		}
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswers"));
		//selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		
		selCol.setReadOnly(this.uiService.newTrueDecision());
		selCol.setCorrect(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.correctAnswers"));
		selCol.setCorrectDecision(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		entityList.addColumn(selCol);

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setTopped();
		autoCol.setProperty(this.uiService.newPropertyReference().setReference("choice.id"));
		entityList.addColumn(autoCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.id")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatPercent", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
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
	 * Set the entire set of choices to these values.
	 * 
	 * @param choices
	 *        The choice values.
	 */
	public void setAnswerChoices(List<String> choices)
	{
		if (choices == null) throw new IllegalArgumentException();

		// check for difference
		if (choices.size() == this.answerChoices.size())
		{
			boolean different = false;
			for (int i = 0; i < choices.size(); i++)
			{
				if (!choices.get(i).equals(this.answerChoices.get(i).getText()))
				{
					different = true;
					break;
				}
			}

			if (!different) return;
		}

		int size = choices.size();
		if (size > this.MAX) size = this.MAX;
		this.answerChoices = new ArrayList<MultipleChoiceQuestionChoice>(size);

		int i = 0;
		for (String choice : choices)
		{
			if (this.answerChoices.size() > this.MAX) break;

			this.answerChoices.add(new MultipleChoiceQuestionChoice(this.question, Integer.toString(i++), choice));
		}

		this.question.setChanged();
	}

	/**
	 * Sets the correct answers.
	 * 
	 * @param correctAnswers
	 *        The correct answers.
	 */
	public void setCorrectAnswers(String[] correctAnswers)
	{
		// if we have no choices yet, start with 4
		if (this.answerChoices.isEmpty())
		{
			consolidate("INIT:5");
		}

		// put them in a set
		Set<Integer> corrects = new HashSet<Integer>();
		if (correctAnswers != null)
		{
			for (String answer : correctAnswers)
			{
				corrects.add(Integer.valueOf(answer));
			}
		}

		setCorrectAnswerSet(corrects);
	}

	/**
	 * Sets the correct answers as a set.
	 * 
	 * @param correctAnswers
	 *        The correct answers.
	 */
	public void setCorrectAnswerSet(Set<Integer> correctAnswers)
	{
		if (correctAnswers == null) throw new IllegalArgumentException();

		// check for difference
		Set<Integer> current = getCorrectAnswerSet();
		if (correctAnswers.equals(current)) return;

		// clear the correct markings
		for (MultipleChoiceQuestionChoice choice : this.answerChoices)
		{
			choice.setCorrect(Boolean.FALSE);
		}

		// mark each one given
		for (Integer answer : correctAnswers)
		{
			MultipleChoiceQuestionChoice choice = this.answerChoices.get(answer.intValue());
			if (choice != null)
			{
				choice.setCorrect(Boolean.TRUE);
			}
		}

		this.question.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		if ((data != null) && (data.length >= 2))
		{
			int numChoices = (data.length - 2) / 2;
			int i = 0;

			this.singleCorrect = Boolean.valueOf(data[i++]);
			this.shuffleChoices = Boolean.valueOf(data[i++]);

			this.answerChoices = new ArrayList<MultipleChoiceQuestionChoice>();
			for (int count = 0; count < numChoices; count++)
			{
				MultipleChoiceQuestionChoice choice = new MultipleChoiceQuestionChoice(this.question, Integer.toString(this.answerChoices.size()),
						data[i++]);
				choice.correct = Boolean.valueOf(data[i++]);
				this.answerChoices.add(choice);
			}

			this.question.setChanged();
		}
	}

	/**
	 * Set the shuffle choice, as a Boolean string.
	 * 
	 * @param shuffleChoices
	 *        The shuffle choice.
	 */
	public void setShuffleChoices(String shuffleChoices)
	{
		if (shuffleChoices == null) throw new IllegalArgumentException();

		Boolean b = Boolean.valueOf(shuffleChoices);
		if (!Different.different(b, this.shuffleChoices)) return;

		this.shuffleChoices = b;

		this.question.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSingleCorrect(String singleCorrect)
	{
		if (singleCorrect == null) throw new IllegalArgumentException();

		Boolean b = Boolean.valueOf(singleCorrect);
		if (!Different.different(b, this.singleCorrect)) return;

		this.singleCorrect = b;

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
