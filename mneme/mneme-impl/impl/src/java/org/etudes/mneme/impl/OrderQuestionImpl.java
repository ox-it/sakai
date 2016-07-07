/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/OrderQuestionImpl.java $
 * $Id: OrderQuestionImpl.java 9340 2014-11-24 22:35:03Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2015 Etudes, Inc.
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
import java.util.LinkedHashSet;
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
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.OrDecision;
import org.etudes.ambrosia.api.PropertyColumn;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Section;
import org.etudes.ambrosia.api.Selection;
import org.etudes.ambrosia.api.Text;
import org.etudes.ambrosia.api.UiService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.etudes.util.HtmlHelper;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.StringUtil;

/**
 * OrderQuestionImpl handles questions for the order question type.
 */
public class OrderQuestionImpl implements TypeSpecificQuestion
{
	public class OrderQuestionChoice
	{
		// Used to report the ids and position numbers of items for re-ordering in the UI
		public class Position
		{
			String id;
			String position;

			public Position(String position, String id)
			{
				this.position = position;
				this.id = id;
			}

			public String getId()
			{
				return this.id;
			}

			public String getPosition()
			{
				return this.position;
			}
		}

		protected String id = null;

		protected Question myQuestion = null;

		protected String text = null;
		
		//To stored correct position
		protected String correctPos = null;

		public OrderQuestionChoice(Question question, OrderQuestionChoice other)
		{
			this.id = other.id;
			this.text = other.text;
			this.myQuestion = question;
			this.correctPos = other.correctPos;
		}

		public OrderQuestionChoice(Question question, String id, String text)
		{
			this.id = id;
			this.myQuestion = question;
			this.text = StringUtil.trimToNull(text);
		}

		public String getId()
		{
			return this.id;
		}

		/**
		 * {@inheritDoc}
		 */
		public Integer getPosition()
		{
			if (this.myQuestion == null) return null;

			List<OrderQuestionImpl.OrderQuestionChoice> choicesList = ((OrderQuestionImpl) this.myQuestion.getTypeSpecificQuestion())
					.getChoicesAsAuthored();
			int index = choicesList.indexOf(this);

			return index + 1;
			
		}

		/**
		 * @return The detail's id; this is a match to setPositioning, which will re-order based on ids.
		 */
		public String getPositioning()
		{
			return this.id;
		}

		/**
		 * @return The map id for the get / set positioning methods.
		 */
		/*
		 * public String getPositioning() { return this.getMapId(); }
		 */

		/**
		 * @return a set of Position objects for the item's map's items - with the detail position and id.
		 */
		public List<Position> getPositions()
		{
			List<Position> rv = new ArrayList<Position>();
			if (question == null) return rv;
			List<OrderQuestionImpl.OrderQuestionChoice> choicesList = ((OrderQuestionImpl) question.getTypeSpecificQuestion()).getChoicesAsAuthored();
			for (int i = 0; i < choicesList.size(); i++)
			{
				Position p = new Position(Integer.valueOf(i + 1).toString(), choicesList.get(i).getId());
				rv.add(p);
			}
			
			return rv;
		}

		public String getText()
		{
			return this.text;
		}

		/**
		 * Change the detail's position within the part
		 * 
		 * @param id
		 *        The id in the position this one wants to move to.
		 */
		public void setPositioning(String id)
		{
			if (id == null) return;
			
			if (id.equals(this.getId())) return;
			int curPos = getPosition().intValue();

			List<OrderQuestionImpl.OrderQuestionChoice> choicesList = ((OrderQuestionImpl) myQuestion.getTypeSpecificQuestion())
					.getChoicesAsAuthored();
			int newPos = -1;
			for (OrderQuestionImpl.OrderQuestionChoice oqc : choicesList)
			{
				if (oqc.id.equals(id))
				{
					newPos = oqc.getPosition().intValue();
					break;
				}
			}
			if (newPos == -1) return;
			if (curPos == newPos) return;

			((OrderQuestionImpl) myQuestion.getTypeSpecificQuestion()).setMoveObj(this);
			((OrderQuestionImpl) myQuestion.getTypeSpecificQuestion()).setNewPos(newPos-1);
		}

		public void setText(String text)
		{
			this.text = StringUtil.trimToNull(text);
			this.myQuestion.setChanged();
		}

		public String getCorrectPos()
		{
			return correctPos;
		}

		public void setCorrectPos(String correctPos)
		{
			this.correctPos = correctPos;
		}

	}

	/** The maximum number of events we support. */
	protected final static int MAX = 10;

	/** List of choices */
	protected List<OrderQuestionChoice> answerChoices = new ArrayList<OrderQuestionChoice>();

	/** Our messages. */
	protected transient InternationalizedMessages messages = null;

	protected transient OrderQuestionChoice moveObj = null;

	protected transient int newPos;

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
	public OrderQuestionImpl(Question question, OrderQuestionImpl other)
	{
		this.answerChoices = new ArrayList<OrderQuestionChoice>(other.answerChoices.size());
		for (OrderQuestionChoice choice : other.answerChoices)
		{
			this.answerChoices.add(new OrderQuestionChoice(question, choice));
		}
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
	public OrderQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
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
			((OrderQuestionImpl) rv).answerChoices = new ArrayList<OrderQuestionChoice>(this.answerChoices.size());
			for (OrderQuestionChoice choice : this.answerChoices)
			{
				((OrderQuestionImpl) rv).answerChoices.add(new OrderQuestionChoice(question, choice));
			}

			// set the question
			((OrderQuestionImpl) rv).question = question;

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
				List newChoices = new ArrayList<OrderQuestionChoice>();
				int i = 0;
				for (OrderQuestionChoice choice : this.answerChoices)
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
							OrderQuestionChoice choice = new OrderQuestionChoice(this.question, Integer.toString(i++), "");
							if ((count == 0) && (destination.startsWith("INIT:")))
							{
								// choice.initOrder();
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
		
		if (destination.startsWith("REORDER"))
		{
			removeBlanks = false;
			stayHere = true;
			List<OrderQuestionImpl.OrderQuestionChoice> choicesList = getChoicesAsAuthored();
			// remove
			choicesList.remove(this.moveObj);

			// re-insert
			choicesList.add(this.newPos, this.moveObj);

			List<String> localAnswerChoices = new ArrayList();
			for (OrderQuestionImpl.OrderQuestionChoice oqc : choicesList)
			{
				localAnswerChoices.add(oqc.getText());
			}
			setAnswerChoices(localAnswerChoices);

			// mark as changed
			this.question.setChanged();
		}

		// remove any blank choices unless we don't want to
		if (removeBlanks)
		{
			List newChoices = new ArrayList<OrderQuestionChoice>();
			int i = 0;
			boolean removed = false;
			for (OrderQuestionChoice choice : this.answerChoices)
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
		List<OrderQuestionChoice> choices = getChoicesAsAuthored();

		// that's the A, B, C order, so find each correct one
		int i = 0;
		for (OrderQuestionChoice choice : choices)
		{
			String choiceText = stripP(choice.getText());
			choiceText = HtmlHelper.clean(choiceText, true);
			choiceText = HtmlHelper.stripForms(choiceText);
			rv.append("<BR>"+(i+1)+". "+choiceText);
			i++;
		}

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getAuthoringUi()
	{
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

		Section answer = this.uiService.newSection();
		answer.add(addMore).add(noMore);

		// listing of choices
		EntityList choicesList = this.uiService.newEntityList();
		choicesList.setStyle(EntityList.Style.form);
		choicesList.setDndReorder();
		choicesList.setDndColHide("dropdown");
		choicesList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesAsAuthored")
				.setIndexReference("id"), "choice");
		
		EntityListColumn dropDownCol = this.uiService.newEntityListColumn();
		dropDownCol.setId("dropdown");
		Selection sel = this.uiService.newSelection();
		sel.setOrientation(Selection.Orientation.dropdown);
		sel.setProperty(this.uiService.newPropertyReference().setReference("choice.positioning"));
		Message valueMessage = this.uiService.newMessage();
		valueMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.id"));
		Message displayMessage = this.uiService.newMessage();
		displayMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.position"));
		Destination dest = this.uiService.newDestination();
		dest.setDestination("REORDER");
		sel.setDestination(dest);

		sel.setSelectionModel(this.uiService.newPropertyReference().setReference("choice.positions"), "pos", valueMessage, displayMessage);
		dropDownCol.add(sel);
		choicesList.addColumn(dropDownCol);
		
		
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
	 * Access the choices as an entity (OrderQuestionChoice) list.
	 * 
	 * @return The choices as an entity (OrderQuestionChoice) list.
	 */
	public List<OrderQuestionChoice> getChoices()
	{
		// get the list in order
		List<OrderQuestionChoice> rv = getChoicesAsAuthored();
		List<OrderQuestionChoice> origRv = getChoicesAsAuthored();

		// shuffle them if desired (and we are in a submission context)
		if ((this.question.getPart() != null) && (this.question.getPart().getAssessment().getSubmissionContext() != null))
		{
			// set the seed based on the submission id
			long seed = (this.question.getId() + "_" + this.question.getPart().getAssessment().getSubmissionContext().getId()).hashCode();

			// mix up the answers
			Collections.shuffle(rv, new Random(seed));
		}

		int i = 0;
		for (OrderQuestionChoice oqc : origRv)
		{
			 //((OrderQuestionChoice)rv.get(i)).setCorrectPos(((OrderQuestionChoice)origRv.get(i)).getId());
			for (OrderQuestionChoice oqc2 : rv)
			{
				if (oqc2.getId().equals(oqc.getId()))
				{
					oqc2.setCorrectPos(String.valueOf(i));
					break;
				}
			}
			i++;
		}
		
		return rv;
	}

	/**
	 * Access the actual choices as authored. The choices in the list can be altered, changing the question definition.
	 * 
	 * @return The choices as authored.
	 */
	public List<OrderQuestionChoice> getChoicesAsAuthored()
	{
		// if we have no choices yet, start with 4
		if (this.answerChoices.isEmpty())
		{
			consolidate("INIT:5");
		}
		List newChoices = new ArrayList<OrderQuestionChoice>();
		int i = 0;
		for (OrderQuestionChoice choice : this.answerChoices)
		{
			newChoices.add(choice);
			choice.setCorrectPos(String.valueOf(i));
			i++;
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
	 * Access the choices, as an entity (OrderQuestionChoice) list. Shuffle if set, even if not in a submission context.
	 * 
	 * @return The choices as an entity (OrderQuestionChoice) list.
	 */
	public List<OrderQuestionChoice> getChoicesShuffled()
	{
		// get the list in order
		List<OrderQuestionChoice> rv = getChoicesAsAuthored();
		
		// shuffle them if desired (and we are in a submission context)
		if ((this.question.getPart() != null))
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
	public LinkedHashSet<Integer> getCorrectAnswerSet()
	{
		LinkedHashSet rv = new LinkedHashSet<Integer>();
		if (this.question.getHasCorrect())
		{
			for (OrderQuestionChoice choice : this.answerChoices)
			{
				rv.add(Integer.valueOf(choice.getId()));
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
		for (OrderQuestionChoice choice : this.answerChoices)
		{
			rv[i++] = choice.text;
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
		entityList.setDndReorder();
		entityList.setDndColHide("dropdown");
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.choices").setIndexReference("id"),
				"choice");
		entityList.setOrderProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.newOrder"));

		EntityListColumn dropDownCol = this.uiService.newEntityListColumn();
		dropDownCol.setId("dropdown");
		Selection sel = this.uiService.newSelection();
		sel.setOrientation(Selection.Orientation.dropdown);
		sel.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
				.addProperty(this.uiService.newPropertyReference().setReference("choice.id")));
		
	
		Message valueMessage = this.uiService.newMessage();
		valueMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.id"));
		Message displayMessage = this.uiService.newMessage();
		displayMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.position"));

		sel.setSelectionModel(this.uiService.newPropertyReference().setReference("choice.positions"), "pos", valueMessage, displayMessage);
		sel.addSelection(this.uiService.newMessage().setMessage("select"), null);

		dropDownCol.add(sel);
		
		EntityListColumn reorderCol = this.uiService.newEntityListColumn();
		Navigation nav = this.uiService.newNavigation();
		nav.setDescription("reorder").setIcon("/icons/reorder.png", Navigation.IconStyle.left).setStyle(Navigation.Style.link).setDisabled(this.uiService.newTrueDecision());
		reorderCol.add(nav);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);

		entityList.addColumn(reorderCol);

		entityList.addColumn(dropDownCol);

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

	public OrderQuestionChoice getMoveObj()
	{
		return moveObj;
	}

	public int getNewPos()
	{
		return newPos;
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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.choices"), "choice");
		
		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);
		
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
				
		// correct / incorrect
		Text correct = this.uiService.newText();
		correct.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/correct.png"));
		correct.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("choice.correctPos"))
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("choice.id"))));

		Text incorrect = this.uiService.newText();
		incorrect.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/incorrect.png"));
		incorrect.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("choice.correctPos"))
				.setReversed()
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("choice.id"))));

		EntityListColumn correctCol = this.uiService.newEntityListColumn();
		correctCol.setTopped();
		correctCol.setWidth(16);
		correctCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("choice.id")), null);
		correctCol.add(correct).add(incorrect);
		correctCol.setIncluded(showCorrect);
		entityList.addColumn(correctCol);

		EntityListColumn dropDownCol = this.uiService.newEntityListColumn();
		dropDownCol.setId("dropdown");
		Selection sel = this.uiService.newSelection();
		sel.setOrientation(Selection.Orientation.dropdown);
		sel.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
				.addProperty(this.uiService.newPropertyReference().setReference("choice.id")));
		sel.setReadOnly(this.uiService.newTrueDecision());
		//sel.setCorrect(this.uiService.newPropertyReference().setReference("choice.correctPos"));
		sel.addSelection(this.uiService.newMessage().setMessage("select"), null);

		Message valueMessage = this.uiService.newMessage();
		valueMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.id"));
		Message displayMessage = this.uiService.newMessage();
		displayMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.position"));
	
		sel.setSelectionModel(this.uiService.newPropertyReference().setReference("choice.positions"), "pos", valueMessage, displayMessage);
		
		
		/*AndDecision andComp = this.uiService.newAndDecision();
		Decision[] compDec = new Decision[2];
		compDec[0] = this.uiService.newCompareDecision();
		compDec[0].setProperty(this.uiService.newPropertyReference().setReference("answer.question.part.assessment.review.showCorrectAnswer"));
		((CompareDecision) compDec[0]).setEqualsConstant("incorrect_only");
		compDec[1] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("grading")).setReversed();
		andComp.setRequirements(compDec);*/
				
		sel.setCorrectDecision(showCorrect);

		dropDownCol.add(sel);

		entityList.addColumn(dropDownCol);

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("answer.question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Decision[] innerOrInc = new Decision[2];
		innerOrInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("showIncorrect")).setReversed();
		innerOrInc[1] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("hideKey")).setReversed();

		Decision[] innerAndInc = new Decision[2];
		innerAndInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.showCompletelyCorrectReview"));
		innerAndInc[1] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("review"));
		Decision[] orInc = new Decision[2];
		orInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("grading"));
		orInc[1] = this.uiService.newAndDecision().setRequirements(innerAndInc);
		Decision[] andInc = new Decision[4];
		andInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.question.hasCorrect"));
		andInc[1] = this.uiService.newOrDecision().setOptions(orInc);
		andInc[2] = this.uiService.newOrDecision().setOptions(innerOrInc);
		andInc[3] = this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.part.assessment.allowedPoints"));
		answerKey.setIncluded(this.uiService.newAndDecision().setRequirements(andInc));

		Section first = this.uiService.newSection();
		first.add(question)/* .add(attachments) */.add(entityList);

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
		entityList
				.setIterator(this.uiService.newPropertyReference().setReference("answer.question.typeSpecificQuestion.choices"), "choice");
		entityList.setEmptyTitle("no-answer");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);
		
		// correct / incorrect
		Text correct = this.uiService.newText();
		correct.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/correct.png"));
		correct.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("choice.correctPos"))
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("choice.id"))));

		Text incorrect = this.uiService.newText();
		incorrect.setText(null, this.uiService.newIconPropertyReference().setIcon("!/ambrosia_library/icons/incorrect.png"));
		incorrect.setIncluded(this.uiService
				.newCompareDecision()
				.setEqualsProperty(this.uiService.newPropertyReference().setReference("choice.correctPos"))
				.setReversed()
				.setProperty(
						this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
								.addProperty(this.uiService.newPropertyReference().setReference("choice.id"))));

		EntityListColumn correctCol = this.uiService.newEntityListColumn();
		correctCol.setTopped();
		correctCol.setWidth(16);
		correctCol.setEntityIncluded(
				this.uiService.newHasValueDecision().setProperty(this.uiService.newPropertyReference().setReference("choice.id")), null);
		correctCol.add(correct).add(incorrect);
		correctCol.setIncluded(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));
		entityList.addColumn(correctCol);

		EntityListColumn dropDownCol = this.uiService.newEntityListColumn();
		dropDownCol.setId("dropdown");
		Selection sel = this.uiService.newSelection();
		sel.setOrientation(Selection.Orientation.dropdown);
		sel.addSelection(this.uiService.newMessage().setMessage("select"), null);

		sel.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answer.{0}.value")
				.addProperty(this.uiService.newPropertyReference().setReference("choice.id")));
		sel.setReadOnly(this.uiService.newTrueDecision());
		//sel.setCorrect(this.uiService.newPropertyReference().setReference("choice.correctPos"));
		//sel.setHideDropDown(this.uiService.newTrueDecision());
		Message valueMessage = this.uiService.newMessage();
		valueMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.id"));
		Message displayMessage = this.uiService.newMessage();
		displayMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.position"));
		sel.setCorrectDecision(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));

		sel.setSelectionModel(this.uiService.newPropertyReference().setReference("choice.positions"), "pos", valueMessage, displayMessage);
		dropDownCol.add(sel);
		entityList.addColumn(dropDownCol);

		return this.uiService.newFragment().setMessages(this.messages).add(entityList);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewDeliveryUi()
	{
		Text question = this.uiService.newText();
		question.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.presentation.text"));

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesShuffled"), "choice");

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setNumeric();
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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesAsAuthored"), "choice");

		EntityListColumn dropDownCol = this.uiService.newEntityListColumn();
		dropDownCol.setId("dropdown");
		Selection sel = this.uiService.newSelection();
		sel.setOrientation(Selection.Orientation.dropdown);
		//sel.setProperty(this.uiService.newPropertyReference().setReference("choice.positioning"));
		sel.setReadOnly(this.uiService.newTrueDecision());
		Message valueMessage = this.uiService.newMessage();
		valueMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.id"));
		Message displayMessage = this.uiService.newMessage();
		displayMessage.setMessage(null, this.uiService.newPropertyReference().setReference("pos.position"));
		
		sel.setSelectionModel(this.uiService.newPropertyReference().setReference("choice.positions"), "pos", valueMessage, displayMessage);
		sel.addSelection(this.uiService.newMessage().setMessage("select"), null);

		dropDownCol.add(sel);

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setDirty().setStripP().setReference("choice.text"));
		entityList.addColumn(propCol);
		
		EntityListColumn reorderCol = this.uiService.newEntityListColumn();
		Navigation nav = this.uiService.newNavigation();
		nav.setDescription("reorder").setIcon("/icons/reorder.png", Navigation.IconStyle.left).setStyle(Navigation.Style.link).setDisabled(this.uiService.newTrueDecision());
		reorderCol.add(nav);
	    
		entityList.addColumn(reorderCol);

		entityList.addColumn(dropDownCol);

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

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.choicesAsAuthored"), "choice");
		entityList.setEmptyTitle("no-answer");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.id")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatOrderChoice", "sakai.mneme")));
		entityList.addColumn(propCol);

		AutoColumn autoCol = this.uiService.newAutoColumn();
		autoCol.setNumeric();
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
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatOrderPercent", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setTopped();
		propCol.setProperty(this.uiService.newHtmlPropertyReference().setReference("choice.id")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatOrderCount", "sakai.mneme")));
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
				if (choices.get(i) != null && this.answerChoices.get(i).getText() != null)
				{
					if (!choices.get(i).equals(this.answerChoices.get(i).getText()))
					{
						different = true;
						break;
					}
				}
			}

			if (!different) return;
		}

		int size = choices.size();
		if (size > this.MAX) size = this.MAX;
		this.answerChoices = new ArrayList<OrderQuestionChoice>(size);

		int i = 0;
		for (String choice : choices)
		{
			if (this.answerChoices.size() > this.MAX) break;
			
			this.answerChoices.add(new OrderQuestionChoice(this.question, Integer.toString(i++), choice));
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
		Set<Integer> corrects = new LinkedHashSet<Integer>();
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

			this.answerChoices = new ArrayList<OrderQuestionChoice>();
			for (int count = 0; count < numChoices; count++)
			{
				OrderQuestionChoice choice = new OrderQuestionChoice(this.question, Integer.toString(this.answerChoices.size()), data[i++]);
				this.answerChoices.add(choice);
			}

			this.question.setChanged();
		}
	}

	public void setMoveObj(OrderQuestionChoice moveObj)
	{
		this.moveObj = moveObj;
	}
	
	public void setNewPos(int newPos)
	{
		this.newPos = newPos;
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
	 * String surrounding <p> tags
	 */
	private String stripP(String value)
	{
		int start = 0;
		if (value.startsWith("<p>")) start += 3;

		int end = value.length();
		if (value.endsWith("</p>")) end -= 4;

		value = value.substring(start, end);
		return value;
	}
}
