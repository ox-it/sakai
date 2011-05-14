/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.EntityList;
import org.etudes.ambrosia.api.FillIn;
import org.etudes.ambrosia.api.HtmlEdit;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.OrDecision;
import org.etudes.ambrosia.api.Overlay;
import org.etudes.ambrosia.api.PropertyColumn;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Section;
import org.etudes.ambrosia.api.Selection;
import org.etudes.ambrosia.api.Text;
import org.etudes.ambrosia.api.Toggle;
import org.etudes.ambrosia.api.UiService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;

/**
 * FillBlanksQuestionImpl handles questions for the true/false question type.
 */
public class FillBlanksQuestionImpl implements TypeSpecificQuestion
{
	/** TRUE means any order is OK, FALSE means it is not */
	protected Boolean anyOrder = Boolean.FALSE;

	/** TRUE means answer is case sensitive, FALSE means it is not */
	protected Boolean caseSensitive = Boolean.FALSE;

	protected InternationalizedMessages messages = null;

	protected transient QuestionPlugin plugin = null;

	/** The question this is a helper for. */
	protected transient Question question = null;

	/** TRUE means response is textual, FALSE means response is numeric */
	protected Boolean responseTextual = Boolean.TRUE;

	/** The question text. */
	protected String text = null;

	/** Dependency: The UI service (Ambrosia). */
	protected UiService uiService = null;

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public FillBlanksQuestionImpl(Question question, FillBlanksQuestionImpl other)
	{
		this.anyOrder = other.anyOrder;
		this.caseSensitive = other.caseSensitive;
		this.messages = other.messages;
		this.question = question;
		this.responseTextual = other.responseTextual;
		this.text = other.text;
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
	public FillBlanksQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
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
			((FillBlanksQuestionImpl) rv).question = question;

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
		StringBuffer answerKey = new StringBuffer();
		List<String> correctAnswers = getCorrectAnswers();

		for (String correctAnswer : correctAnswers)
		{
			answerKey.append(correctAnswer);
			answerKey.append(", ");
		}

		if (answerKey.length() > 0) answerKey.setLength(answerKey.length() - 2);

		if (this.anyOrder) answerKey.append(this.messages.getString("any-order-key"));
		if (this.caseSensitive) answerKey.append(this.messages.getString("case-sensitive-key"));
		if (!this.responseTextual) answerKey.append(this.messages.getString("numeric-key"));

		return answerKey.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAnyOrder()
	{
		return this.anyOrder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getAuthoringUi()
	{
		// question (with instructions)
		HtmlEdit question = uiService.newHtmlEdit();
		question.setSize(HtmlEdit.Sizes.tall);
		question.setProperty(this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.text"));
		question.setTitle("question");

		Overlay instructions = this.uiService.newOverlay();
		instructions.setId("instructions");
		instructions
				.add(this.uiService.newText().setText("instructions-title", this.uiService.newIconPropertyReference().setIcon("/icons/test.png")));
		instructions.add(this.uiService.newText().setText("instructions"));
		instructions.add(this.uiService.newGap());
		instructions.add(this.uiService.newToggle().setTarget("instructions").setTitle("close")
				.setIcon("/icons/close.png", Navigation.IconStyle.left));
		instructions.add(this.uiService.newGap());
		instructions.add(this.uiService.newText().setTitle("examples-title"));
		instructions.add(this.uiService.newText().setText("example1").setTitle("example1-title"));
		instructions.add(this.uiService.newText().setText("example2").setTitle("example2-title"));
		instructions.add(this.uiService.newText().setText("example3").setTitle("example3-title"));
		instructions.add(this.uiService.newText().setText("example4").setTitle("example4-title"));
		instructions.add(this.uiService.newText().setText("example5").setTitle("example5-title"));
		instructions.add(this.uiService.newGap());
		instructions.add(this.uiService.newToggle().setTarget("instructions").setTitle("close")
				.setIcon("/icons/close.png", Navigation.IconStyle.left));

		Toggle viewInstructions = this.uiService.newToggle().setTarget("instructions").setTitle("view-instructions")
				.setIcon("/icons/test.png", Navigation.IconStyle.left);

		Section questionSection = this.uiService.newSection();
		questionSection.add(question).add(instructions).add(viewInstructions);

		// answer options
		Selection response = this.uiService.newSelection();
		response.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.responseTextual"));
		response.addSelection(this.uiService.newMessage().setMessage("textual"), this.uiService.newMessage().setTemplate("true"));
		response.addSelection(this.uiService.newMessage().setMessage("numeric"), this.uiService.newMessage().setTemplate("false"));
		response.setTitle("answer", this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png"));

		Selection caseSensitive = this.uiService.newSelection();
		caseSensitive.addSelection(this.uiService.newMessage().setMessage("case-sensitive"), this.uiService.newMessage().setTemplate("true"));
		caseSensitive.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.caseSensitive"));

		Selection order = this.uiService.newSelection();
		order.addSelection(this.uiService.newMessage().setMessage("any-order"), this.uiService.newMessage().setTemplate("true"));
		order.setProperty(this.uiService.newPropertyReference().setReference("question.typeSpecificQuestion.anyOrder"));

		Section answerSection = this.uiService.newSection();
		answerSection.add(response).add(caseSensitive).add(order);
		answerSection.setIncluded(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.isSurvey"))
				.setReversed());

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection).add(answerSection);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCaseSensitive()
	{
		return this.caseSensitive.toString();
	}

	/**
	 * Get the correct answers for the question.
	 * 
	 * @return A List containing each correct answer for the fill-ins in the question.
	 */
	public List<String> getCorrectAnswers()
	{
		List<String> correctAnswers = new ArrayList<String>();

		if (!getIsValid()) return correctAnswers;

		parseCorrectAnswers(correctAnswers);

		return correctAnswers;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[4];
		rv[0] = this.anyOrder.toString();
		rv[1] = this.caseSensitive.toString();
		rv[2] = this.responseTextual.toString();
		rv[3] = this.text;

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getDeliveryUi()
	{
		FillIn fillIn = this.uiService.newFillIn();
		fillIn.setText(null, this.uiService.newHtmlPropertyReference().setReference("answer.question.typeSpecificQuestion.questionText"))
				.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers")).setWidth(20);
		fillIn.setWidth(20);
		if (!responseTextual) fillIn.setNumeric();

		Section section = this.uiService.newSection();
		section.add(fillIn);

		return this.uiService.newFragment().setMessages(this.messages).add(section);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return getQuestionText();
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
		String invalidMsg = isValid(this.text);
		return invalidMsg;
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
		if (isValid(this.text) != null) return Boolean.FALSE;

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
	 * Produce a string of the question with the answers removed.
	 * 
	 * @return The question text with the answers removed.
	 */
	public String getQuestionText()
	{
		if (this.text == null) return null;

		if (!getIsValid()) return this.text;

		String text = this.text;
		StringBuffer rv = new StringBuffer();

		while (text.indexOf("{") > -1)
		{
			int left = text.indexOf("{");
			int right = text.indexOf("}");

			String tmp = text.substring(0, left);
			text = text.substring(right + 1);
			rv.append(tmp);
			rv.append("{}");

			// there are no more "}", exit loop
			if (right == -1)
			{
				break;
			}
		}

		rv.append(text);

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getResponseTextual()
	{
		return this.responseTextual.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getReviewUi()
	{
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

		FillIn fillIn = this.uiService.newFillIn();
		fillIn.setText(null, this.uiService.newHtmlPropertyReference().setReference("answer.question.typeSpecificQuestion.questionText"));
		fillIn.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		fillIn.setWidth(20);
		fillIn.setCorrectDecision(showCorrect);
		fillIn.setReadOnly(this.uiService.newTrueDecision());
		fillIn.setCorrect(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.entryCorrects"));

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
		Decision[] andInc = new Decision[2];
		andInc[0] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.question.hasCorrect"));
		andInc[1] = this.uiService.newOrDecision().setOptions(orInc);
		answerKey.setIncluded(this.uiService.newAndDecision().setRequirements(andInc));

		Section first = this.uiService.newSection();
		first.add(fillIn);

		Section second = this.uiService.newSection();
		second.add(answerKey);

		return this.uiService.newFragment().setMessages(this.messages).add(first).add(second);
	}

	/**
	 * Access the question text.
	 * 
	 * @return The question text.
	 */
	public String getText()
	{
		return this.text;
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
		// we suppress the question presentation, using our own fields to capture the question.
		return Boolean.FALSE;
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

	public Component getViewAnswerUi()
	{
		FillIn fillIn = this.uiService.newFillIn();
		fillIn.setText(null, this.uiService.newHtmlPropertyReference().setReference("answer.question.typeSpecificQuestion.questionText"));
		fillIn.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		fillIn.setWidth(20);
		fillIn.setCorrectDecision(this.uiService.newTrueDecision());
		fillIn.setReadOnly(this.uiService.newTrueDecision());
		fillIn.setCorrect(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.entryCorrects"));
		fillIn.setCorrectDecision(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));

		return this.uiService.newFragment().setMessages(this.messages).add(fillIn);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewDeliveryUi()
	{
		FillIn fillIn = this.uiService.newFillIn();
		fillIn.setText(null, this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.questionText"));
		fillIn.setWidth(20);
		fillIn.setReadOnly(this.uiService.newTrueDecision());

		Section first = this.uiService.newSection();
		first.add(fillIn);

		return this.uiService.newFragment().setMessages(this.messages).add(first);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewQuestionUi()
	{
		FillIn fillIn = this.uiService.newFillIn();
		fillIn.setText(null, this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.questionText"));
		fillIn.setWidth(20);
		fillIn.setReadOnly(this.uiService.newTrueDecision());

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section first = this.uiService.newSection();
		first.add(fillIn);

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
		FillIn fillIn = this.uiService.newFillIn();
		fillIn.setText(null, this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.questionText"));
		fillIn.setWidth(20);
		fillIn.setReadOnly(this.uiService.newTrueDecision());

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section first = this.uiService.newSection();
		first.add(fillIn);

		Section second = this.uiService.newSection();
		second.setIncluded(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		second.add(answerKey);

		Section positions = this.uiService.newSection();
		PropertyReference iteratorRef = this.uiService.newPropertyReference().setReference("question")
				.setFormatDelegate(this.uiService.getFormatDelegate("AccessFillinPositions", "sakai.mneme"));
		positions.setIterator(iteratorRef, "position", null);
		positions.setTitle("position", this.uiService.newPropertyReference().setReference("position"));

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(
				this.uiService.newPropertyReference()
						.setFormatDelegate(this.uiService.getFormatDelegate("AccessFillinPositionValues", "sakai.mneme")), "answer");
		entityList.setEmptyTitle("no-answer");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatFillinPositionCorrect", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer"));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatFillinPositionPercents", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatFillinPositionCount", "sakai.mneme")));
		entityList.addColumn(propCol);

		positions.add(entityList);

		Text unanswered = this.uiService.newText().setText(
				null,
				this.uiService.newHtmlPropertyReference().setFormatDelegate(
						this.uiService.getFormatDelegate("FormatUnansweredPercent", "sakai.mneme")));

		return this.uiService.newFragment().setMessages(this.messages).add(first).add(second).add(positions).add(unanswered);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnyOrder(String anyOrder)
	{
		if (anyOrder == null) throw new IllegalArgumentException();

		Boolean b = Boolean.valueOf(anyOrder);
		if (!Different.different(b, this.anyOrder)) return;

		this.anyOrder = b;

		this.question.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCaseSensitive(String caseSensitive)
	{
		if (caseSensitive == null) throw new IllegalArgumentException();

		Boolean b = Boolean.valueOf(caseSensitive);
		if (!Different.different(b, this.caseSensitive)) return;

		this.caseSensitive = b;

		this.question.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		if ((data != null) && (data.length == 4))
		{
			this.anyOrder = Boolean.valueOf(data[0]);
			this.caseSensitive = Boolean.valueOf(data[1]);
			this.responseTextual = Boolean.valueOf(data[2]);
			this.text = data[3];
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setResponseTextual(String responseTextual)
	{
		if (responseTextual == null) throw new IllegalArgumentException();

		Boolean b = Boolean.valueOf(responseTextual);
		if (!Different.different(b, this.responseTextual)) return;

		this.responseTextual = b;

		this.question.setChanged();
	}

	/**
	 * Set the question text.
	 * 
	 * @param text
	 *        The question text.
	 */
	public void setText(String text)
	{
		if (!Different.different(this.text, text)) return;

		this.text = StringUtil.trimToNull(text);

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
	 * Trim the target outside and inside.
	 * 
	 * @param target
	 *        The string to trim.
	 * @return The trimmed string.
	 */
	protected String fullTrim(String target)
	{
		String working = target;
		if (working != null)
		{
			working = working.trim();

			// trim interior white space from the answer
			String[] tokens = StringUtil.split(working, " ");
			StringBuilder buf = new StringBuilder();
			buf.append(tokens[0]);
			for (int i = 1; i < tokens.length; i++)
			{
				if ((tokens[i] != null) && (tokens[i].length() > 0))
				{
					buf.append(" ");
					buf.append(tokens[i]);
				}
			}
			working = buf.toString();
		}

		return working;
	}

	/**
	 * Trim the source from any blanks, and convert any html blanks to real ones.
	 * 
	 * @param source
	 *        The source string.
	 * @return The trimmed source.
	 */
	protected static String trim(String source)
	{
		String rv = source.replace("&nbsp;", " ").trim();
		return rv;
	}

	/**
	 * Check the text for a valid fill-in question.
	 * 
	 * @param text
	 *        The question text.
	 * @return a localized error message if invalid, or null if valid.
	 */
	protected String isValid(String text)
	{
		if (text == null) return "<ul>" + this.messages.getString("invalid-null-text") + "</ul>";

		// strip surrounding <p>
		int start = 0;
		if (text.startsWith("<p>")) start += 3;
		int end = text.length();
		if (text.endsWith("</p>")) end -= 4;
		text = text.substring(start, end);

		// deal with html spaces
		text = text.replace("&nbsp;", " ");

		boolean invalidOutsideEmpty = false;
		boolean invalidUnbalanced = false;
		boolean invalidNoFillins = false;
		boolean invalidOpenAnswerInAssessment = false;

		boolean outsideTextSeen = false;
		boolean fillinSeen = false;

		boolean insideBrackets = false;
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);

			// if we are outside of brackets, see if we are going inside, and check if we have outside text
			if (!insideBrackets)
			{
				if (c == '{')
				{
					insideBrackets = true;
				}
				else
				{
					if (c == '}')
					{
						invalidUnbalanced = true;
						break;
					}

					outsideTextSeen = true;
				}
			}

			// if we are inside a bracket, see if we are at the end bracket
			else
			{
				if (c == '}')
				{
					insideBrackets = false;
					fillinSeen = true;
				}
				else
				{
					if (c == '{')
					{
						invalidUnbalanced = true;
						break;
					}
				}
			}
		}

		// if we did not see any text outside the brackets, this is an error
		if (!outsideTextSeen) invalidOutsideEmpty = true;

		// if we ended inside, this is an error
		if (insideBrackets) invalidUnbalanced = true;

		// if we saw no fill in, this is an error
		if (!fillinSeen) invalidNoFillins = true;

		// if so far valid, check for any * choices for non-survey questions
		if ((!this.question.getIsSurvey()) && !(invalidOutsideEmpty || invalidUnbalanced || invalidNoFillins))
		{
			List<String> answers = new ArrayList<String>();
			parseCorrectAnswers(answers);
			for (String answer : answers)
			{
				if ("*".equals(answer))
				{
					invalidOpenAnswerInAssessment = true;
					break;
				}
			}
		}

		// if otherwise valid, check for numeric
		// Note: lets not subject parseCorrectAnswers() to invalid
		boolean invalidNumeric = false;
		boolean invalidNumericRange = false;
		if (!(invalidOutsideEmpty || invalidUnbalanced || invalidNoFillins || invalidOpenAnswerInAssessment) && !this.responseTextual)
		{
			List<String> correctAnswers = new ArrayList<String>();
			parseCorrectAnswers(correctAnswers);

			for (String answer : correctAnswers)
			{
				// allow dot or comma for decimal point
				// TODO: this needs to be changed to respect locale, not just replace commas! -ggolden
				answer = answer.replace(',', '.');

				// if not numeric, we are in trouble
				try
				{
					// form the range of correct answers
					Float[] range = new Float[2];

					// if there's a bar in the correct pattern, split and use the first two as the range
					if (answer.indexOf("|") != -1)
					{
						String[] parts = answer.split("\\|");
						if (parts.length == 2)
						{
							range[0] = Float.parseFloat(trim(parts[0]));
							range[1] = Float.parseFloat(trim(parts[1]));
						}
						else
						{
							invalidNumericRange = true;
							break;
						}
					}

					// otherwise use the single value for both sides of the range
					else
					{
						range[0] = range[1] = Float.parseFloat(trim(answer));
					}
				}
				catch (NumberFormatException e)
				{
					invalidNumeric = true;
					break;
				}
			}
		}

		// if we are valid
		if (!(invalidOutsideEmpty || invalidUnbalanced || invalidNoFillins || invalidOpenAnswerInAssessment || invalidNumeric || invalidNumericRange))
			return null;

		// collect the errors
		StringBuilder rv = new StringBuilder();
		if (invalidUnbalanced)
		{
			rv.append(this.messages.getString("invalid-unbalanced"));
		}
		if (invalidOutsideEmpty)
		{
			rv.append(this.messages.getString("invalid-outside-empty"));
		}
		if (invalidNoFillins)
		{
			rv.append(this.messages.getString("invalid-no-fillins"));
		}
		if (invalidOpenAnswerInAssessment)
		{
			rv.append(this.messages.getString("invalid-open-assessment"));
		}
		else if (invalidNumeric)
		{
			rv.append(this.messages.getString("invalid-numeric"));
		}
		else if (invalidNumericRange)
		{
			rv.append(this.messages.getString("invalid-numeric-range"));
		}

		return "<ul>" + rv.toString() + "</ul>";
	}

	/**
	 * Parse out the correct answers into a list.
	 * 
	 * @param correctAnswers
	 *        The correct answers list.
	 */
	protected void parseCorrectAnswers(List<String> correctAnswers)
	{
		// start with the question text
		String alltext = getText();

		// remove any html comments so we don't accidently consider brackets in there
		alltext = unHtmlComment(alltext);

		while (alltext.indexOf("{") > -1)
		{
			int alltextLeftIndex = alltext.indexOf("{");
			int alltextRightIndex = alltext.indexOf("}");

			String tmp = alltext.substring(alltextLeftIndex + 1, alltextRightIndex);
			alltext = alltext.substring(alltextRightIndex + 1);

			// clean up any html in the answer
			// save the newlines which convertFormattedTextToPlaintext just strips out
			tmp = tmp.replace("\n", " ");
			tmp = FormattedText.convertFormattedTextToPlaintext(tmp);

			// Note: convertFormattedTextToPlaintext converts %nbsp; to unicode 160
			tmp = tmp.replace((char) 160, ' ');

			// convert any strange left over whitespace to space
			tmp = tmp.replace("\n", " ");
			tmp = tmp.replace("\r", " ");
			tmp = tmp.replace("\t", " ");
			tmp = tmp.replace("&nbsp;", " ");

			// trim leading and trailing white space
			tmp = tmp.trim();

			// nothing becomes *
			if (tmp.length() == 0)
			{
				tmp = "*";
			}

			// trim interior white space as well
			else
			{
				tmp = fullTrim(tmp);
			}

			correctAnswers.add(tmp);

			// there are no more "}", exit loop
			if (alltextRightIndex == -1)
			{
				break;
			}
		}
	}

	/**
	 * Remove any html comment text from the source, return the result.
	 * 
	 * @param source
	 *        The source html string.
	 * @return The source html string with all html comments removed.
	 */
	protected String unHtmlComment(String source)
	{
		if (source == null) return source;

		Pattern p = Pattern.compile("<!--(.|\\s)*?-->");
		Matcher m = p.matcher(source);
		StringBuffer sb = new StringBuffer();
		while (m.find())
		{
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);

		return sb.toString();
	}
}
