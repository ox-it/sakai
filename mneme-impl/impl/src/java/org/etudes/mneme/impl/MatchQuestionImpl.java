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
import java.util.List;
import java.util.Random;

import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.AutoColumn;
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
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.util.StringUtil;

/**
 * MatchQuestionImpl handles questions for the match question type.
 */
public class MatchQuestionImpl implements TypeSpecificQuestion
{
	public class MatchQuestionChoice
	{
		protected String id = null;
		protected String text = null;

		public MatchQuestionChoice(String text, String id)
		{
			this.text = text;
			this.id = id;
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

	public class MatchQuestionMatch
	{
		protected List<MatchQuestionChoice> choices = null;
		protected String id = null;
		protected String text = null;

		public MatchQuestionMatch(String text, String id, List<MatchQuestionChoice> choices)
		{
			this.text = text;
			this.id = id;
			this.choices = choices;
		}

		public List<MatchQuestionChoice> getChoices()
		{
			return this.choices;
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

	public class MatchQuestionPair
	{
		protected String choice = null;

		/** Identifies the choice, different id than that used for the match. */
		protected String choiceId = null;

		protected String choiceLabel = null;
		protected String choiceLabelFull = null;

		/** For this match, which choice matches (after shuffle). */
		protected String correctChoiceId = null;

		/** Identifies the pair, also identifies the match. */
		protected String id = null;

		protected String match = null;

		protected String matchLabel = null;

		protected Question myQuestion = null;

		public MatchQuestionPair(Question question, MatchQuestionPair other)
		{
			this.myQuestion = question;
			this.choice = other.choice;
			this.choiceId = other.choiceId;
			this.correctChoiceId = other.correctChoiceId;
			this.id = other.id;
			this.match = other.match;
			this.choiceLabel = other.choiceLabel;
			this.choiceLabelFull = other.choiceLabelFull;
			this.matchLabel = other.matchLabel;
		}

		public MatchQuestionPair(Question question, String choice, String match, int index)
		{
			this.myQuestion = question;
			this.choice = StringUtil.trimToNull(choice);
			this.choiceId = idManager.createUuid();
			this.correctChoiceId = this.choiceId;
			this.id = idManager.createUuid();
			this.match = StringUtil.trimToNull(match);
			this.choiceLabel = choiceLabels[index];
			this.choiceLabelFull = choiceLabels[index];
			this.matchLabel = matchLabels[index];
			// TODO: max!
		}

		public MatchQuestionPair(Question question, String choice, String choiceId, String match, String id, int index)
		{
			this.myQuestion = question;
			this.choice = StringUtil.trimToNull(choice);
			this.choiceId = StringUtil.trimToNull(choiceId);
			this.correctChoiceId = this.choiceId;
			this.id = StringUtil.trimToNull(id);
			this.match = StringUtil.trimToNull(match);
			this.choiceLabel = choiceLabels[index];
			this.choiceLabelFull = choiceLabels[index];
			this.matchLabel = matchLabels[index];
			// TODO: max!
		}

		public String getChoice()
		{
			return this.choice;
		}

		public String getChoiceId()
		{
			return this.choiceId;
		}

		public String getChoiceLabel()
		{
			return this.choiceLabel;
		}

		public String getChoiceLabelFull()
		{
			return this.choiceLabelFull;
		}

		public String getCorrectChoiceId()
		{
			return this.correctChoiceId;
		}

		public String getId()
		{
			return this.id;
		}

		public String getMatch()
		{
			return this.match;
		}

		public String getMatchLabel()
		{
			return this.matchLabel;
		}

		public void setChoice(String choice)
		{
			String c = StringUtil.trimToNull(choice);

			this.choice = c;
			this.myQuestion.setChanged();
		}

		public void setMatch(String match)
		{
			String m = StringUtil.trimToNull(match);

			this.match = m;
			this.myQuestion.setChanged();
		}
	}

	/** Labels for the choices. */
	protected static String[] choiceLabels =
	{ "A.", "B.", "C.", "D.", "E.", "F.", "G.", "H.", "I.", "J.", "K.", "L.", "M.", "N.", "O.", "P.", "Q.", "R.", "S.", "T.", "U.", "V.", "W.", "X.",
			"Y.", "Z." };

	/** Lables for the matches. */
	protected static String[] matchLabels =
	{ "1.", "2.", "3.", "4.", "5.", "6.", "7.", "8.", "9.", "10.", "11.", "12.", "13.", "14.", "15.", "16.", "17.", "18.", "19.", "20.", "21.",
			"22.", "23.", "24.", "25.", "26." };

	/** The maximum number of choices we support. */
	protected final static int MAX = 25;

	/** String that holds the distractor choice */
	protected MatchQuestionPair distractor = null;

	/** Dependency: IdManager. */
	protected IdManager idManager = null;

	/** Our messages. */
	protected transient InternationalizedMessages messages = null;

	/** List of choices */
	protected List<MatchQuestionPair> pairs = new ArrayList<MatchQuestionPair>();

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
	public MatchQuestionImpl(Question question, MatchQuestionImpl other)
	{
		if (other.distractor != null) this.distractor = new MatchQuestionPair(question, other.distractor);
		this.pairs = new ArrayList<MatchQuestionPair>(other.pairs.size());
		for (MatchQuestionPair choice : other.pairs)
		{
			this.pairs.add(new MatchQuestionPair(question, choice));
		}
		this.messages = other.messages;
		this.uiService = other.uiService;
		this.idManager = other.idManager;
		this.plugin = other.plugin;
		this.question = question;
	}

	/**
	 * Construct.
	 * 
	 * @param uiService
	 *        the UiService.
	 * @param question
	 *        The Question this is a helper for.
	 */
	public MatchQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, IdManager idManager, Question question)
	{
		this.idManager = idManager;
		this.plugin = plugin;
		this.messages = messages;
		this.uiService = uiService;
		this.question = question;
	}

	/**
	 * Add a pair of choice - match.
	 * 
	 * @param choice
	 *        The Choice.
	 * @param match
	 *        The Match.
	 */
	public void addPair(String choice, String match)
	{
		// take no more than the max
		if (this.pairs.size() == this.MAX) return;

		this.pairs.add(new MatchQuestionPair(this.question, choice, match, this.pairs.size()));
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
			((MatchQuestionImpl) rv).pairs = new ArrayList<MatchQuestionPair>(this.pairs.size());
			for (MatchQuestionPair choice : this.pairs)
			{
				((MatchQuestionImpl) rv).pairs.add(new MatchQuestionPair(question, choice));
			}

			if (this.distractor != null) ((MatchQuestionImpl) rv).distractor = new MatchQuestionPair(question, this.distractor);

			// set the question
			((MatchQuestionImpl) rv).question = question;

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
			stayHere = true;
			removeBlanks = false;

			String[] parts = StringUtil.split(destination, ":");
			if (parts.length == 2)
			{
				List newChoices = new ArrayList<MatchQuestionPair>();
				for (MatchQuestionPair pair : this.pairs)
				{
					// ignore the deleted one
					if (!pair.getId().equals(parts[1]))
					{
						newChoices.add(pair);
					}
				}

				this.pairs = newChoices;
				this.question.setChanged();
			}
		}

		// add more choices
		if (destination.startsWith("ADD:") || destination.startsWith("INIT:"))
		{
			stayHere = true;
			removeBlanks = false;

			String[] parts = StringUtil.split(destination, ":");
			if (parts.length == 2)
			{
				try
				{
					int more = Integer.parseInt(parts[1]);
					if ((this.pairs.size() + more) > this.MAX)
					{
						more = this.MAX - this.pairs.size();
					}
					if (more > 0)
					{
						for (int count = 0; count < more; count++)
						{
							this.pairs.add(new MatchQuestionPair(this.question, null, null, this.pairs.size()));
						}

						// for init, don't mark the question as changed
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

		// remove blank pairs
		if (removeBlanks)
		{
			List newChoices = new ArrayList<MatchQuestionPair>();
			boolean anyRemoved = false;
			for (MatchQuestionPair pair : this.pairs)
			{
				// ignore the deleted one
				if (!((pair.getChoice() == null) && (pair.getMatch() == null)))
				{
					newChoices.add(pair);
				}
				else
				{
					anyRemoved = true;
				}
			}

			if (anyRemoved)
			{
				this.pairs = newChoices;

				// if mint, this is not enought to cause it to be marked changed
				if (!this.question.getMint())
				{
					this.question.setChanged();
				}
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
		List<MatchQuestionPair> pairs = getPairsForDelivery();

		StringBuilder rv = new StringBuilder();

		for (MatchQuestionPair pair : pairs)
		{
			if (pair.getMatch() == null) continue;

			rv.append(pair.getMatchLabel().substring(0, pair.getMatchLabel().length() - 1));
			rv.append(" - ");

			for (MatchQuestionPair matchingChoice : pairs)
			{
				if (matchingChoice.getChoiceId().equals(pair.getCorrectChoiceId()))
				{
					rv.append(matchingChoice.getChoiceLabel().substring(0, pair.getChoiceLabel().length() - 1));
					rv.append(", ");
					break;
				}
			}
		}

		if (rv.length() > 0)
		{
			rv.setLength(rv.length() - 2);
		}

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getAuthoringUi()
	{
		// list of choices
		EntityList choices = this.uiService.newEntityList();
		choices.setStyle(EntityList.Style.form);
		choices.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairs"), "pair");

		AutoColumn autoCol = this.uiService.newAutoColumn();
		choices.addColumn(autoCol);

		EntityListColumn col = this.uiService.newEntityListColumn();
		HtmlEdit edit = this.uiService.newHtmlEdit();
		edit.setSize(HtmlEdit.Sizes.small);
		edit.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.choice"));
		col.setTitle("choice");
		col.add(edit);
		choices.addColumn(col);

		col = this.uiService.newEntityListColumn();
		edit = this.uiService.newHtmlEdit();
		edit.setSize(HtmlEdit.Sizes.small);
		edit.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.match"));
		col.setTitle("match");
		col.add(edit);
		choices.addColumn(col);

		col = this.uiService.newEntityListColumn();
		Navigation nav = this.uiService.newNavigation();
		Destination destination = this.uiService.newDestination();
		destination.setDestination("DEL:{0}", this.uiService.newPropertyReference().setReference("pair.id"));
		nav.setTitle("delete").setIcon("/icons/delete.png", Navigation.IconStyle.left).setStyle(Navigation.Style.link).setSubmit()
				.setDestination(destination);
		col.add(nav);
		choices.addColumn(col);

		HtmlEdit distractor = this.uiService.newHtmlEdit();
		distractor.setTitle("distractor", this.uiService.newIconPropertyReference().setIcon("/icons/distractor_add.png"));
		distractor.setSize(HtmlEdit.Sizes.small);
		distractor.setProperty(this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.distractor"));

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
				.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsMaxedOut")));

		Instructions noMore = uiService.newInstructions();
		noMore.setText("no-more");
		noMore.setIncluded(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsMaxedOut")));

		Section choicesSection = this.uiService.newSection();
		choicesSection.setTitle("choices", this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png"));
		choicesSection.add(choices).add(distractor).add(addMore).add(noMore);

		return this.uiService.newFragment().setMessages(this.messages).add(choicesSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[4 * (this.pairs.size() + 1)];
		int i = 0;
		if (this.distractor != null)
		{
			rv[i++] = this.distractor.choice;
			rv[i++] = this.distractor.choiceId;
			rv[i++] = this.distractor.match;
			rv[i++] = this.distractor.id;
		}
		else
		{
			rv[i++] = null;
			rv[i++] = null;
			rv[i++] = null;
			rv[i++] = null;

		}
		for (MatchQuestionPair pair : this.pairs)
		{
			rv[i++] = pair.choice;
			rv[i++] = pair.choiceId;
			rv[i++] = pair.match;
			rv[i++] = pair.id;
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

		Section quesitonSection = this.uiService.newSection();
		quesitonSection.add(question)/* .add(attachments) */;

		// the choices display
		EntityList choiceEntityList = this.uiService.newEntityList();
		choiceEntityList.setStyle(EntityList.Style.form);
		choiceEntityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		PropertyColumn choiceLabel = this.uiService.newPropertyColumn();
		choiceLabel.setTopped();
		choiceLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.choiceLabel"));
		choiceEntityList.addColumn(choiceLabel);

		PropertyColumn choice = this.uiService.newPropertyColumn();
		choice.setTopped();
		choice.setTitle("choice");
		choice.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.choice"));
		choiceEntityList.addColumn(choice);

		Section choiceSection = this.uiService.newSection();
		choiceSection.add(choiceEntityList);

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		// match display
		PropertyColumn matchLabel = this.uiService.newPropertyColumn();
		matchLabel.setTopped();
		matchLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.matchLabel"));
		entityList.addColumn(matchLabel);

		PropertyColumn match = this.uiService.newPropertyColumn();
		match.setTopped();
		match.setTitle("match");
		match.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.match"));
		entityList.addColumn(match);

		// match
		Selection selection = this.uiService.newSelection();
		selection.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
				.addProperty(this.uiService.newPropertyReference().setReference("pair.id")));
		selection.setSelectionModel(
				this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery"),
				"choice",
				this.uiService.newMessage().setMessage(null, this.uiService.newPropertyReference().setReference("choice.choiceId")),
				this.uiService.newMessage().setMessage(null,
						this.uiService.newTextPropertyReference().setStripHtml().setMaxLength(100).setReference("choice.choiceLabelFull")));
		selection.setOrientation(Selection.Orientation.dropdown);
		selection.addSelection(this.uiService.newMessage().setMessage("select"), null);

		EntityListColumn matchCol = this.uiService.newEntityListColumn();
		matchCol.setTopped();
		matchCol.add(selection);
		matchCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("pair.match")), null);
		entityList.addColumn(matchCol);

		Section matchSection = this.uiService.newSection();
		matchSection.add(entityList);

		// Section alternateUI = getDeliveryUiMatchSection();

		return this.uiService.newFragment().setMessages(this.messages).add(quesitonSection).add(choiceSection).add(matchSection)
		// .add(alternateUI)
		;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return this.question.getPresentation().getText();
	}

	/**
	 * Access the distractor's string value.
	 * 
	 * @return The distractor's string value, or null if not defined.
	 */
	public String getDistractor()
	{
		if (this.distractor == null) return null;
		return this.distractor.getChoice();
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
		boolean invalidPairs = this.pairs.size() < 2;

		boolean invalidPartialPairs = false;
		for (MatchQuestionPair pair : this.pairs)
		{
			if ((pair.choice == null) || (pair.match == null))
			{
				invalidPartialPairs = true;
				break;
			}
		}

		if (!(invalidText || invalidPairs || invalidPartialPairs)) return null;

		StringBuilder rv = new StringBuilder();
		rv.append("<ul>");

		if (invalidText)
		{
			rv.append(this.messages.getString("invalid-text"));
		}

		if (invalidPairs)
		{
			rv.append(this.messages.getString("invalid-pairs"));
		}

		if (invalidPartialPairs)
		{
			rv.append(this.messages.getString("invalid-partial"));
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
		// text must be defined
		if (this.question.getPresentation().getText() == null) return Boolean.FALSE;

		// we need more than one pair defined
		if (this.pairs.size() < 2) return Boolean.FALSE;

		boolean invalidPartialPairs = false;
		for (MatchQuestionPair pair : this.pairs)
		{
			if ((pair.choice == null) || (pair.match == null))
			{
				invalidPartialPairs = true;
				break;
			}
		}
		if (invalidPartialPairs) return Boolean.FALSE;

		return Boolean.TRUE;
	}

	public List<MatchQuestionMatch> getMatchesForDelivery()
	{
		List<MatchQuestionMatch> rv = new ArrayList<MatchQuestionMatch>(this.pairs.size());

		// shuffle if we can based on the submission, etc.
		long seed = 0;
		if ((this.question.getPart() != null) && (this.question.getPart().getAssessment().getSubmissionContext() != null))
		{
			// set the seed based on the submission id and the question
			seed = (this.question.getId() + "_" + this.question.getPart().getAssessment().getSubmissionContext().getId()).hashCode();
		}
		else
		{
			seed = (this.question.getId() + "_987654321").hashCode();
		}
		Random sequence = new Random(seed);

		// make the set of choices
		List<MatchQuestionChoice> choices = new ArrayList<MatchQuestionChoice>();
		for (MatchQuestionPair pair : this.pairs)
		{
			choices.add(new MatchQuestionChoice(pair.choice, pair.choiceId));
		}
		// add the distractor (need to be at the end of the rv, i.e. after that shuffle)
		if ((this.distractor != null) && (distractor.getChoice() != null))
		{
			choices.add(new MatchQuestionChoice(this.distractor.choice, this.distractor.choiceId));
		}
		// shuffle the choices
		Collections.shuffle(choices, sequence);

		for (MatchQuestionPair pair : this.pairs)
		{
			rv.add(new MatchQuestionMatch(pair.match, pair.id, choices));
		}
		// shuffle the matches
		Collections.shuffle(rv, sequence);

		return rv;
	}

	/**
	 * Access the pairs as an entity (MatchQuestionChoice) list in as-authored order.
	 * 
	 * @return The pairs as an entity (MatchQuestionChoice) list in as-authored order.
	 */
	public List<MatchQuestionPair> getPairs()
	{
		if (this.pairs.size() == 0)
		{
			consolidate("INIT:4");
		}
		return this.pairs;
	}

	/**
	 * Access the pairs properly shuffled for delivery.
	 * 
	 * @return The pairs properly shuffled for delivery.
	 */
	public List<MatchQuestionPair> getPairsForDelivery()
	{
		// shuffle if we can based on the submission, etc.
		long seed = 0;
		if ((this.question.getPart() != null) && (this.question.getPart().getAssessment().getSubmissionContext() != null))
		{
			// set the seed based on the submission id and the question
			seed = (this.question.getId() + "_" + this.question.getPart().getAssessment().getSubmissionContext().getId()).hashCode();
		}
		else
		{
			seed = (this.question.getId() + "_987654321").hashCode();
		}
		Random sequence = new Random(seed);

		// deep copy to shuffle and modify
		List<MatchQuestionPair> rv = new ArrayList<MatchQuestionPair>(this.pairs.size());
		for (MatchQuestionPair choice : this.pairs)
		{
			rv.add(new MatchQuestionPair(this.question, choice));
		}

		// shuffle once for the matches
		Collections.shuffle(rv, sequence);

		// shuffle another copy for the choices
		List<MatchQuestionPair> choices = new ArrayList<MatchQuestionPair>(rv.size());
		for (MatchQuestionPair choice : this.pairs)
		{
			choices.add(new MatchQuestionPair(this.question, choice));
		}

		// add the distractor (need to be at the end of the rv, i.e. after that shuffle)
		if ((this.distractor != null) && (distractor.getChoice() != null))
		{
			choices.add(new MatchQuestionPair(this.question, this.distractor));
			rv.add(new MatchQuestionPair(this.question, this.distractor));
		}

		Collections.shuffle(choices, sequence);

		// move the choices into the rv, and the labels in order
		for (int i = 0; i < rv.size(); i++)
		{
			MatchQuestionPair rvPair = rv.get(i);
			MatchQuestionPair choicePair = choices.get(i);

			rvPair.choice = choicePair.choice;
			rvPair.choiceId = choicePair.choiceId;

			if (rvPair.getMatch() != null)
			{
				rvPair.matchLabel = this.matchLabels[i];
			}
			else
			{
				rvPair.matchLabel = null;
			}
			rvPair.choiceLabel = this.choiceLabels[i];
			rvPair.choiceLabelFull = this.choiceLabels[i] + " " + choicePair.choice;
		}

		return rv;
	}

	/**
	 * Check if there are already max pairs.
	 * 
	 * @return TRUE if there are already max pairs, false if fewer.
	 */
	public Boolean getPairsMaxedOut()
	{
		return Boolean.valueOf(this.pairs.size() >= this.MAX);
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

		Section quesitonSection = this.uiService.newSection();
		quesitonSection.add(question)/* .add(attachments) */;

		// the choices display
		EntityList choiceEntityList = this.uiService.newEntityList();
		choiceEntityList.setStyle(EntityList.Style.form);
		choiceEntityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		PropertyColumn choiceLabel = this.uiService.newPropertyColumn();
		choiceLabel.setTopped();
		choiceLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.choiceLabel"));
		choiceEntityList.addColumn(choiceLabel);

		PropertyColumn choice = this.uiService.newPropertyColumn();
		choice.setTopped();
		choice.setTitle("choice");
		choice.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.choice"));
		choiceEntityList.addColumn(choice);

		Section choiceSection = this.uiService.newSection();
		choiceSection.add(choiceEntityList);

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		// match display
		PropertyColumn matchLabel = this.uiService.newPropertyColumn();
		matchLabel.setTopped();
		matchLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.matchLabel"));
		entityList.addColumn(matchLabel);

		PropertyColumn match = this.uiService.newPropertyColumn();
		match.setTopped();
		match.setTitle("match");
		match.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.match"));
		entityList.addColumn(match);

		// correct / incorrect
		Text correct = this.uiService.newText();
		correct.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/correct.png"));
		correct.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("pair.correctChoiceId"))
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("pair.id"))));

		Text incorrect = this.uiService.newText();
		incorrect.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/incorrect.png"));
		incorrect.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("pair.correctChoiceId"))
				.setReversed()
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("pair.id"))));

		EntityListColumn correctCol = this.uiService.newEntityListColumn();
		correctCol.setTopped();
		correctCol.setWidth(16);
		correctCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("pair.match")), null);
		correctCol.add(correct).add(incorrect);

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

		correctCol.setIncluded(showCorrect);

		entityList.addColumn(correctCol);

		// match
		Selection selection = this.uiService.newSelection();
		selection.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
				.addProperty(this.uiService.newPropertyReference().setReference("pair.id")));
		selection.setSelectionModel(
				this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery"),
				"choice",
				this.uiService.newMessage().setMessage(null, this.uiService.newPropertyReference().setReference("choice.choiceId")),
				this.uiService.newMessage().setMessage(null,
						this.uiService.newTextPropertyReference().setStripHtml().setMaxLength(100).setReference("choice.choiceLabelFull")));
		selection.setOrientation(Selection.Orientation.dropdown);
		selection.setReadOnly(this.uiService.newTrueDecision());
		selection.addSelection(this.uiService.newMessage().setMessage("select"), null);

		EntityListColumn matchCol = this.uiService.newEntityListColumn();
		matchCol.setTopped();
		matchCol.add(selection);
		matchCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("pair.match")), null);
		entityList.addColumn(matchCol);

		Section matchSection = this.uiService.newSection();
		matchSection.add(entityList);

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

		Section answerKeySection = this.uiService.newSection();
		answerKeySection.add(answerKey);

		return this.uiService.newFragment().setMessages(this.messages).add(quesitonSection).add(choiceSection).add(matchSection)
				.add(answerKeySection);
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
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewAnswerUi()
	{
		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		// the choices display
		EntityList choiceEntityList = this.uiService.newEntityList();
		choiceEntityList.setStyle(EntityList.Style.form);
		choiceEntityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		PropertyColumn choiceLabel = this.uiService.newPropertyColumn();
		choiceLabel.setTopped();
		choiceLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.choiceLabel"));
		choiceEntityList.addColumn(choiceLabel);

		PropertyColumn choice = this.uiService.newPropertyColumn();
		choice.setTopped();
		choice.setTitle("choice");
		choice.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.choice"));
		choiceEntityList.addColumn(choice);

		Section choiceSection = this.uiService.newSection();
		choiceSection.add(choiceEntityList);

		// match display
		PropertyColumn matchLabel = this.uiService.newPropertyColumn();
		matchLabel.setTopped();
		matchLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.matchLabel"));
		entityList.addColumn(matchLabel);

		PropertyColumn match = this.uiService.newPropertyColumn();
		match.setTopped();
		match.setTitle("match");
		match.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.match"));
		entityList.addColumn(match);

		// correct / incorrect
		Text correct = this.uiService.newText();
		correct.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/correct.png"));
		correct.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("pair.correctChoiceId"))
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("pair.id"))));

		Text incorrect = this.uiService.newText();
		incorrect.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/incorrect.png"));
		incorrect.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("pair.correctChoiceId"))
				.setReversed()
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("pair.id"))));

		EntityListColumn correctCol = this.uiService.newEntityListColumn();
		correctCol.setTopped();
		correctCol.setWidth(16);
		correctCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("pair.match")), null);
		correctCol.add(correct).add(incorrect);
		correctCol.setIncluded(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));
		entityList.addColumn(correctCol);

		// match
		Selection selection = this.uiService.newSelection();
		selection.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
				.addProperty(this.uiService.newPropertyReference().setReference("pair.id")));
		selection.setSelectionModel(
				this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.pairsForDelivery"),
				"choice",
				this.uiService.newMessage().setMessage(null, this.uiService.newPropertyReference().setReference("choice.choiceId")),
				this.uiService.newMessage().setMessage(null,
						this.uiService.newTextPropertyReference().setStripHtml().setMaxLength(100).setReference("choice.choiceLabelFull")));
		selection.setOrientation(Selection.Orientation.dropdown);
		selection.setReadOnly(this.uiService.newTrueDecision());
		selection.addSelection(this.uiService.newMessage().setMessage("select"), null);

		EntityListColumn matchCol = this.uiService.newEntityListColumn();
		matchCol.setTopped();
		matchCol.add(selection);
		matchCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("pair.match")), null);
		entityList.addColumn(matchCol);

		Section matchSection = this.uiService.newSection();
		matchSection.add(entityList);

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("answer.question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section answerKeySection = this.uiService.newSection();
		answerKeySection.setIncluded(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));
		answerKeySection.add(answerKey);

		return this.uiService.newFragment().setMessages(this.messages).add(choiceSection).add(matchSection).add(answerKeySection);
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

		Section quesitonSection = this.uiService.newSection();
		quesitonSection.add(question)/* .add(attachments) */;

		// the choices display
		EntityList choiceEntityList = this.uiService.newEntityList();
		choiceEntityList.setStyle(EntityList.Style.form);
		choiceEntityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		PropertyColumn choiceLabel = this.uiService.newPropertyColumn();
		choiceLabel.setTopped();
		choiceLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.choiceLabel"));
		choiceEntityList.addColumn(choiceLabel);

		PropertyColumn choice = this.uiService.newPropertyColumn();
		choice.setTopped();
		choice.setTitle("choice");
		choice.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.choice"));
		choiceEntityList.addColumn(choice);

		Section choiceSection = this.uiService.newSection();
		choiceSection.add(choiceEntityList);

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		// match display
		PropertyColumn matchLabel = this.uiService.newPropertyColumn();
		matchLabel.setTopped();
		matchLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.matchLabel"));
		entityList.addColumn(matchLabel);

		PropertyColumn match = this.uiService.newPropertyColumn();
		match.setTopped();
		match.setTitle("match");
		match.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.match"));
		entityList.addColumn(match);

		// match
		Selection selection = this.uiService.newSelection();
		selection.setSelectionModel(
				this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsForDelivery"),
				"choice",
				this.uiService.newMessage().setMessage(null, this.uiService.newPropertyReference().setReference("choice.choiceId")),
				this.uiService.newMessage().setMessage(null,
						this.uiService.newTextPropertyReference().setStripHtml().setMaxLength(100).setReference("choice.choiceLabelFull")));
		selection.setOrientation(Selection.Orientation.dropdown);
		selection.addSelection(this.uiService.newMessage().setMessage("select"), null);

		EntityListColumn matchCol = this.uiService.newEntityListColumn();
		matchCol.setTopped();
		matchCol.add(selection);
		matchCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("pair.match")), null);
		entityList.addColumn(matchCol);

		Section matchSection = this.uiService.newSection();
		matchSection.add(entityList);

		return this.uiService.newFragment().setMessages(this.messages).add(quesitonSection).add(choiceSection).add(matchSection);
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

		Section quesitonSection = this.uiService.newSection();
		quesitonSection.add(question)/* .add(attachments) */;

		// the choices display
		EntityList choiceEntityList = this.uiService.newEntityList();
		choiceEntityList.setStyle(EntityList.Style.form);
		choiceEntityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		PropertyColumn choiceLabel = this.uiService.newPropertyColumn();
		choiceLabel.setTopped();
		choiceLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.choiceLabel"));
		choiceEntityList.addColumn(choiceLabel);

		PropertyColumn choice = this.uiService.newPropertyColumn();
		choice.setTopped();
		choice.setTitle("choice");
		choice.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.choice"));
		choiceEntityList.addColumn(choice);

		Section choiceSection = this.uiService.newSection();
		choiceSection.add(choiceEntityList);

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsForDelivery")
				.setIndexReference("id"), "pair");

		// match display
		PropertyColumn matchLabel = this.uiService.newPropertyColumn();
		matchLabel.setTopped();
		matchLabel.setProperty(this.uiService.newHtmlPropertyReference().setReference("pair.matchLabel"));
		entityList.addColumn(matchLabel);

		PropertyColumn match = this.uiService.newPropertyColumn();
		match.setTopped();
		match.setTitle("match");
		match.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("pair.match"));
		entityList.addColumn(match);

		// match
		Selection selection = this.uiService.newSelection();
		selection.setSelectionModel(
				this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.pairsForDelivery"),
				"choice",
				this.uiService.newMessage().setMessage(null, this.uiService.newPropertyReference().setReference("choice.choiceId")),
				this.uiService.newMessage().setMessage(null,
						this.uiService.newTextPropertyReference().setStripHtml().setMaxLength(100).setReference("choice.choiceLabelFull")));
		selection.setOrientation(Selection.Orientation.dropdown);
		selection.addSelection(this.uiService.newMessage().setMessage("select"), null);

		EntityListColumn matchCol = this.uiService.newEntityListColumn();
		matchCol.setTopped();
		matchCol.add(selection);
		matchCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("pair.match")), null);
		entityList.addColumn(matchCol);

		Section matchSection = this.uiService.newSection();
		matchSection.add(entityList);

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section answerKeySection = this.uiService.newSection();
		answerKeySection.setIncluded(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		answerKeySection.add(answerKey);

		return this.uiService.newFragment().setMessages(this.messages).add(quesitonSection).add(choiceSection).add(matchSection)
				.add(answerKeySection);
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

		Section quesitonSection = this.uiService.newSection();
		quesitonSection.add(question)/* .add(attachments) */;

		Section matches = this.uiService.newSection();
		PropertyReference iteratorRef = this.uiService.newPropertyReference().setReference("question")
				.setFormatDelegate(this.uiService.getFormatDelegate("AccessMatchMatches", "sakai.mneme"));
		matches.setIterator(iteratorRef, "match", null);
		matches.setTitle(
				"match-summary",
				this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("match")
						.setFormatDelegate(this.uiService.getFormatDelegate("FormatMatchMatch", "sakai.mneme")));

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(
				this.uiService.newPropertyReference().setReference("question")
						.setFormatDelegate(this.uiService.getFormatDelegate("AccessMatchChoices", "sakai.mneme")), "choice");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newPropertyReference().setFormatDelegate(
				this.uiService.getFormatDelegate("FormatMatchUserChoice", "sakai.mneme")));
		entityList.addColumn(propCol);
		
		propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newPropertyReference().setFormatDelegate(
				this.uiService.getFormatDelegate("FormatMatchCorrect", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatMatchChoice", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newPropertyReference().setFormatDelegate(
				this.uiService.getFormatDelegate("FormatMatchPercents", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newPropertyReference().setFormatDelegate(
				this.uiService.getFormatDelegate("FormatMatchCount", "sakai.mneme")));
		entityList.addColumn(propCol);

		matches.add(entityList);

		Text unanswered = this.uiService.newText().setText(
				null,
				this.uiService.newHtmlPropertyReference().setFormatDelegate(
						this.uiService.getFormatDelegate("FormatUnansweredPercent", "sakai.mneme")));

		return this.uiService.newFragment().setMessages(this.messages).add(quesitonSection).add(matches).add(unanswered);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		if ((data != null) && (data.length > 0))
		{
			int numPairs = ((data.length / 4) - 1);
			int i = 0;

			String choice = data[i++];
			String choiceId = data[i++];
			String match = data[i++];
			String id = data[i++];
			if (choice != null)
			{
				this.distractor = new MatchQuestionPair(this.question, choice, choiceId, match, id, 0);
			}

			this.pairs = new ArrayList<MatchQuestionPair>();
			for (int count = 0; count < numPairs; count++)
			{
				choice = data[i++];
				choiceId = data[i++];
				match = data[i++];
				id = data[i++];
				this.pairs.add(new MatchQuestionPair(this.question, choice, choiceId, match, id, this.pairs.size()));
			}

			this.question.setChanged();
		}
	}

	/**
	 * Set the distractor's string value.
	 * 
	 * @param distractor
	 *        The distractor's string value.
	 */
	public void setDistractor(String distractor)
	{
		if (this.distractor == null)
		{
			if (distractor == null) return;

			this.distractor = new MatchQuestionPair(this.question, distractor, null, 0);
			this.question.setChanged();
			return;
		}

		this.distractor.setChoice(distractor);
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
	 * Get a section for one match
	 */
	protected Section getDeliveryUiMatchSection()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("match.text"));

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("match.choices"), "choice");

		SelectionColumn selCol = this.uiService.newSelectionColumn();
		selCol.setTopped();
		selCol.setNoWrap();
		selCol.setSingle();
		selCol.setValueProperty(this.uiService.newTextPropertyReference().setReference("choice.id"));
		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));

		selCol.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
				.addProperty(this.uiService.newPropertyReference().setReference("match.id")));

		entityList.addColumn(selCol);

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setTopped();
		entityList.addColumn(autoCol);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);

		Section section = this.uiService.newSection();
		section.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.matchesForDelivery")
				.setIndexReference("id"), "match", null);
		section.add(question).add(entityList);

		return section;
	}
}
