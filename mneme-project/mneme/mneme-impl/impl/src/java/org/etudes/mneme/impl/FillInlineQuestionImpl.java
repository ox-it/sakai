/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/FillBlanksQuestionImpl.java $
 * $Id: FillBlanksQuestionImpl.java 9421 2014-12-02 23:19:27Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014, 2015 Etudes, Inc.
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.CompareDecision;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Container;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.EntityList;
import org.etudes.ambrosia.api.FillInline;
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
import org.etudes.util.HtmlHelper;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;

/**
 * FillInlineQuestionImpl handles questions for the fill in question type.
 */
public class FillInlineQuestionImpl implements TypeSpecificQuestion
{
	protected InternationalizedMessages messages = null;

	protected transient QuestionPlugin plugin = null;

	/** The question this is a helper for. */
	protected transient Question question = null;

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
	public FillInlineQuestionImpl(Question question, FillInlineQuestionImpl other)
	{
		this.messages = other.messages;
		this.question = question;
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
	public FillInlineQuestionImpl(QuestionPlugin plugin, InternationalizedMessages messages, UiService uiService, Question question)
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
			((FillInlineQuestionImpl) rv).question = question;

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

		return answerKey.toString();
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
		instructions.add(this.uiService.newText().setText("example1"));
		
		instructions.add(this.uiService.newGap());
		instructions.add(this.uiService.newToggle().setTarget("instructions").setTitle("close")
				.setIcon("/icons/close.png", Navigation.IconStyle.left));

		Toggle viewInstructions = this.uiService.newToggle().setTarget("instructions").setTitle("view-instructions")
				.setIcon("/icons/test.png", Navigation.IconStyle.left);

		Section questionSection = this.uiService.newSection();
		questionSection.add(question).add(instructions).add(viewInstructions);

		return this.uiService.newFragment().setMessages(this.messages).add(questionSection);
	}

	/**
	 * Get selection lists for this question.
	 * 
	 * @return A List containing lists for the fill-ins in the question.
	 */
	public List<ArrayList<String>> getSelectionLists()
	{
		List<ArrayList<String>> selectionLists = new ArrayList<ArrayList<String>>();
		List<String> correctAnswers = new ArrayList<String>();

		if (!getIsValid()) return selectionLists;

		parseSelectionLists(selectionLists, correctAnswers);

		return selectionLists;
	}
	
	/**
	 * Get the correct answers for the question.
	 * 
	 * @return A List containing each correct answer for the fill-ins in the question.
	 */
	public List<String> getCorrectAnswers()
	{
		List<ArrayList<String>> selectionLists = new ArrayList<ArrayList<String>>();
		List<String> correctAnswers = new ArrayList<String>();

		if (!getIsValid()) return correctAnswers;

		parseSelectionLists(selectionLists, correctAnswers);

		return correctAnswers;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[1];
		rv[0] = this.text;

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getDeliveryUi()
	{
		FillInline FillInline = this.uiService.newFillInline();
		FillInline.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.question.typeSpecificQuestion.questionText"))
				.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"))
				.setSelectionLists(getSelectionLists());
		
		Section section = this.uiService.newSection();
		section.add(FillInline);

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
	public String getId()
	{
		return this.question.getId();
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
		text = text.replaceAll("\\{\\{", "_mnemelb_");
		text = text.replaceAll("\\}\\}", "_mnemerb_");
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
		String completeStr = rv.toString();
		completeStr = completeStr.replaceAll("_mnemelb_", "{");
		completeStr = completeStr.replaceAll("_mnemerb_", "}");

		return completeStr;
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

		FillInline FillInline = this.uiService.newFillInline();
		FillInline.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.question.typeSpecificQuestion.questionText"));
		FillInline.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		FillInline.setSelectionLists(getSelectionLists());
		FillInline.setCorrectDecision(showCorrect);
		
		FillInline.setShowResponse(this.uiService.newTrueDecision());
		FillInline.setCorrect(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.entryCorrects"));

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("answer.question.typeSpecificQuestion.answerKey");
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
		andInc[3] = this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("answer.question.part.assessment.allowedPoints"));
		answerKey.setIncluded(this.uiService.newAndDecision().setRequirements(andInc));

		Section first = this.uiService.newSection();
		first.add(FillInline);

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
		FillInline FillInline = this.uiService.newFillInline();
		FillInline.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("answer.question.typeSpecificQuestion.questionText"));
		FillInline.setSelectionLists(getSelectionLists());
		FillInline.setProperty(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.answers"));
		FillInline.setCorrectDecision(this.uiService.newTrueDecision());
		FillInline.setShowResponse(this.uiService.newTrueDecision());
		FillInline.setCorrect(this.uiService.newPropertyReference().setReference("answer.typeSpecificAnswer.entryCorrects"));
		FillInline.setCorrectDecision(this.uiService.newDecision().setProperty(
				this.uiService.newPropertyReference().setReference("answer.question.hasCorrect")));

		return this.uiService.newFragment().setMessages(this.messages).add(FillInline);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewDeliveryUi()
	{
		FillInline FillInline = this.uiService.newFillInline();
		FillInline.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.typeSpecificQuestion.questionText"));
		FillInline.setSelectionLists(getSelectionLists());
		FillInline.setShowResponse(this.uiService.newTrueDecision());

		Section first = this.uiService.newSection();
		first.add(FillInline);

		return this.uiService.newFragment().setMessages(this.messages).add(first);
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getViewQuestionUi()
	{
		FillInline FillInline = this.uiService.newFillInline();
		FillInline.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.typeSpecificQuestion.questionText"));
		FillInline.setSelectionLists(getSelectionLists());
		//FillInline.setReadOnly(this.uiService.newTrueDecision());

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section first = this.uiService.newSection();
		first.add(FillInline);

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
		FillInline FillInline = this.uiService.newFillInline();
		FillInline.setText(null, this.uiService.newHtmlPropertyReference().setDirty().setReference("question.typeSpecificQuestion.questionText"));
		FillInline.setSelectionLists(getSelectionLists());
		//FillInline.setReadOnly(this.uiService.newTrueDecision());

		Text answerKey = this.uiService.newText();
		PropertyReference[] refs = new PropertyReference[2];
		refs[0] = this.uiService.newIconPropertyReference().setIcon("/icons/answer_key.png");
		refs[1] = this.uiService.newHtmlPropertyReference().setReference("question.typeSpecificQuestion.answerKey");
		answerKey.setText("answer-key", refs);

		Section first = this.uiService.newSection();
		first.add(FillInline);

		Section second = this.uiService.newSection();
		second.setIncluded(this.uiService.newDecision().setProperty(this.uiService.newPropertyReference().setReference("question.hasCorrect")));
		second.add(answerKey);

		Section positions = this.uiService.newSection();
		PropertyReference iteratorRef = this.uiService.newPropertyReference().setReference("question")
				.setFormatDelegate(this.uiService.getFormatDelegate("AccessFillInlinePositions", "sakai.mneme"));
		positions.setIterator(iteratorRef, "position", null);
		positions.setTitle("position", this.uiService.newPropertyReference().setReference("position"));

		EntityList entityList = this.uiService.newEntityList();
		entityList.setStyle(EntityList.Style.form);
		entityList.setIterator(
				this.uiService.newPropertyReference()
						.setFormatDelegate(this.uiService.getFormatDelegate("AccessFillInlinePositionValues", "sakai.mneme")), "answer");
		entityList.setEmptyTitle("no-answer");

		PropertyColumn propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatFillInlinePositionChoice", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatFillInlinePositionCorrect", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer"));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatFillInlinePositionPercents", "sakai.mneme")));
		entityList.addColumn(propCol);

		propCol = this.uiService.newPropertyColumn();
		propCol.setRight();
		propCol.setProperty(this.uiService.newPropertyReference().setReference("answer")
				.setFormatDelegate(this.uiService.getFormatDelegate("FormatFillInlinePositionCount", "sakai.mneme")));
		entityList.addColumn(propCol);

		positions.add(entityList);

		Text unanswered = this.uiService.newText().setText(
				null,
				this.uiService.newHtmlPropertyReference().setFormatDelegate(
						this.uiService.getFormatDelegate("FormatUnansweredPercent", "sakai.mneme")));

		Section section = this.uiService.newSection();
		//section.add(first).add(second);
		section.add(first).add(second).add(positions).add(unanswered);

		// show collected reasons, if reason is being collected
		Section reasonSection = this.uiService.newSection();
		PropertyReference iteratorRefR = this.uiService.newPropertyReference().setReference("submissions")
				.setFormatDelegate(this.uiService.getFormatDelegate("AccessSubmissionsQuestionReasons", "sakai.mneme"));
		reasonSection.setIterator(iteratorRefR, "answer", this.uiService.newMessage().setMessage("no-reasons"));
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
			this.text = data[0];
			this.question.setChanged();
		}
	}

	/**
	 * Set the question text.
	 * 
	 * @param text
	 *        The question text.
	 */
	public void setText(String text)
	{
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
		
		if (this.question.getIsSurvey() && !text.contains("|")) return "<ul>" + this.messages.getString("invalid-no-pipe") + "</ul>";

		boolean invalidOutsideEmpty = false;
		boolean invalidUnbalanced = false;
		boolean invalidNoFillins = false;
		boolean invalidOpenAnswerInAssessment = false;
		boolean invalidSelectSetup = false;

		boolean outsideTextSeen = false;
		boolean fillinSeen = false;

		boolean insideBrackets = false;
		text = text.replaceAll("\\{\\{", "_mnemelb_");
		text = text.replaceAll("\\}\\}", "_mnemerb_");
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

		String parseResult = null;
		// if so far valid, check for any * choices for non-survey questions
		if ((!this.question.getIsSurvey()) && !(invalidOutsideEmpty || invalidUnbalanced || invalidNoFillins))
		{
			List<ArrayList<String>> selectionLists = new ArrayList<ArrayList<String>>();
			List<String> answers = new ArrayList<String>();
			parseResult = parseSelectionLists(selectionLists, answers);
			if (parseResult != null) invalidSelectSetup = true;
			for (String answer : answers)
			{
				if ("*".equals(answer))
				{
					invalidOpenAnswerInAssessment = true;
					break;
				}
			}
		}

		

		// if we are valid
		if (!(invalidOutsideEmpty || invalidUnbalanced || invalidNoFillins || invalidOpenAnswerInAssessment || invalidSelectSetup))
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
		if (invalidSelectSetup)
		{
			rv.append(parseResult);
		}

		return "<ul>" + rv.toString() + "</ul>";
	}

	/**
	 * Parse out the correct answers into a list.
	 * 
	 * @param correctAnswers
	 *        The correct answers list.
	 * @return a localized error message if invalid, or null if valid.        
	 */
	protected String parseSelectionLists(List<ArrayList<String>> selectionLists, List<String> correctAnswers)
	{
		// start with the question text
		String alltext = getText();

		// remove any html comments so we don't accidently consider brackets in there
		alltext = unHtmlComment(alltext);
		
		alltext = alltext.replaceAll("\\{\\{", "_mnemelb_");
		alltext = alltext.replaceAll("\\}\\}", "_mnemerb_");

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
			
			// restore entities for special UNICODE characters
			tmp = HtmlHelper.stripBadEncodingCharacters(tmp);

			tmp = tmp.replaceAll("_mnemelb_", "{");
			tmp = tmp.replaceAll("_mnemerb_", "}");

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

			if (!tmp.contains("|")) return this.messages.getString("specify-choices");
			if (!question.getIsSurvey() && !tmp.contains("*")) return this.messages.getString("correct-answer-missing");
			
			String[] valid = tmp.split("\\|");
			//Need atleast two choices
			if (valid.length < 2) return this.messages.getString("specify-choices");
			ArrayList<String> selList = new ArrayList();
			
			boolean correctFound = false;
			for (int i=0; i<valid.length; i++)
			{
				if (valid[i].startsWith("*")&&!valid[i].equals("*"))
				{	
					correctAnswers.add(valid[i].substring(1));
					selList.add(valid[i].substring(1));
					//Can't have multiple corrects
					if (correctFound)
					{
						return this.messages.getString("only-one-correct");
					}
					correctFound = true;
				}
				else
				{
					selList.add(valid[i]);
				}
			}
			//Added to fix position,%page info for surveys with fill inlines
			if (question.getIsSurvey() && !correctFound) correctAnswers.add(valid[0]);
			//If asterisk is in the list but not placed correctly
			if (!question.getIsSurvey() && !correctFound) return this.messages.getString("correct-answer-missing");
			selectionLists.add(selList);

			// there are no more "}", exit loop
			if (alltextRightIndex == -1)
			{
				break;
			}
		}
		return null;
	}
	
	protected void parseCorrectAnswers(List<String> correctAnswers)
	{
		List<ArrayList<String>> selectionLists = new ArrayList<ArrayList<String>>();
		parseSelectionLists(selectionLists, correctAnswers);
		return;

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
