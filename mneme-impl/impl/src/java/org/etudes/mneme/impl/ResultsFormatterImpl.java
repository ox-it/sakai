/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 Etudes, Inc.
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.TypeSpecificAnswer;
import org.etudes.mneme.impl.MatchQuestionImpl.MatchQuestionPair;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.util.FormattedText;

/**
 * ResultsFormatterImpl ...
 */
public class ResultsFormatterImpl
{
	/** Matches \r */
	private static Pattern M_patternCR = Pattern.compile("\\r");

	private static Pattern M_patternNBSP = Pattern.compile("&nbsp;");

	/** Messages. */
	protected transient InternationalizedMessages messages = null;

	public ResultsFormatterImpl(InternationalizedMessages messages)
	{
		this.messages = messages;
	}

	/**
	 * Format and send a results email for this assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 */
	public String formatResults(Assessment assessment, List<Submission> submissions)
	{
		StringBuilder content = new StringBuilder();

		// the assessment title, points
		content.append("<p>");
		content.append(format("results-assessment-title", assessment.getTitle()));
		if (assessment.getHasPoints())
		{
			content.append("<br />");
			content.append(format("results-assessment-total-points", assessment.getParts().getTotalPoints()));
		}
		content.append("<br />\n");

		// date prepared
		content.append(format("results-date", formatDate(new Date())));
		content.append("</p>\n");

		// for each part
		for (Part part : assessment.getParts().getParts())
		{
			// part title, count, instructions
			content.append("<p>");
			if (part.getTitle() != null)
			{
				content.append(format("results-part-title", part.getOrdering().getPosition(), assessment.getParts().getSize(), part.getTitle()));
			}
			else
			{
				content.append(format("results-part-title-no-title", part.getOrdering().getPosition(), assessment.getParts().getSize()));
			}

			if (assessment.getParts().getShowPresentation())
			{
				content.append("<br />");
				content.append(stripHtml(part.getPresentation().getText()));
			}
			content.append("</p>\n");

			// for each question
			for (Question question : part.getQuestionsUsed())
			{
				Integer count = countQuestionSeen(question, submissions);

				// just skip questions not seen at all
				if (count == 0) continue;

				String answersText = (count == 1) ? format("results-answer") : format("results-answers", count);
				String pointsText = (question.getPoints() > 0) ? format("results-worth", question.getPoints()) : "";

				content
						.append("<div style=\"background: #EEEEEE;border: 1px solid #bbb;padding-left: 0.5em;font-weight: bold;line-height: 1.5em;\">");
				content.append(format("results-question-header", pointsText, answersText));

				content.append("</div>\n");

				// question text
				content.append("<p>");
				if (question.getTypeSpecificQuestion().getUseQuestionPresentation())
				{
					content.append(stripHtml(question.getPresentation().getText()));
				}
				else
				{
					content.append(question.getDescription());
				}
				content.append("</p>");

				// summary of submissions for this question

				// for t/f, likert, mc - for each option, show correct markings, text, # of submissions picking this one, and %
				if (question.getTypeSpecificQuestion() instanceof TrueFalseQuestionImpl)
				{
					formatTrueFalse(content, (TrueFalseQuestionImpl) question.getTypeSpecificQuestion(), question, submissions);
				}

				else if (question.getTypeSpecificQuestion() instanceof MultipleChoiceQuestionImpl)
				{
					formatMultipleChoice(content, (MultipleChoiceQuestionImpl) question.getTypeSpecificQuestion(), question, submissions);
				}

				else if (question.getTypeSpecificQuestion() instanceof LikertScaleQuestionImpl)
				{
					formatLikert(content, (LikertScaleQuestionImpl) question.getTypeSpecificQuestion(), question, submissions);
				}

				// for essay, task - list each submission's answer inline (attachments?)
				else if ((question.getTypeSpecificQuestion() instanceof EssayQuestionImpl)
						|| (question.getTypeSpecificQuestion() instanceof TaskQuestionImpl))
				{
					formatTextResponses(content, question, submissions);
				}

				// for matching... ???
				else if (question.getTypeSpecificQuestion() instanceof MatchQuestionImpl)
				{
					formatMatch(content, (MatchQuestionImpl) question.getTypeSpecificQuestion(), question, submissions);
				}

				// for fill-in... ???
				else if (question.getTypeSpecificQuestion() instanceof FillBlanksQuestionImpl)
				{
					formatFillBlanks(content, (FillBlanksQuestionImpl) question.getTypeSpecificQuestion(), question, submissions);
				}

				content.append("</p>\n");
			}

			if (!part.getOrdering().getIsLast())
			{
				content.append("<hr />\n");
			}
		}

		return content.toString();
	}

	/**
	 * Count the # submissions that saw this question.
	 * 
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions to the assessment.
	 * @return The # submissions that saw this question.
	 */
	protected Integer countQuestionSeen(Question question, List<Submission> submissions)
	{
		int count = 0;
		for (Submission s : submissions)
		{
			if (s.getIsPhantom()) continue;
			if (!s.getIsComplete()) continue;

			Answer a = s.getAnswer(question);
			if (a != null)
			{
				count++;
			}
		}

		return Integer.valueOf(count);
	}

	/**
	 * Format a message with the list of arguments.
	 * 
	 * @param key
	 *        The message bundle key.
	 * @param args
	 *        Any number of arguments for the formatted message.
	 * @return The formatted message.
	 */
	protected String format(String key, Object... args)
	{
		return this.messages.getFormattedMessage(key, args);
	}

	/**
	 * Count the answers to this question that match this target value, formatting count and percent for display.
	 * 
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions to the assessment that has this question.
	 * @param target
	 *        The target answer value.
	 * @return The formatted count and percent.
	 */
	protected String formatCountPercent(Question question, List<Submission> submissions, String target)
	{
		int count = 0;
		int total = 0;
		for (Submission s : submissions)
		{
			if (s.getIsPhantom()) continue;
			if (!s.getIsComplete()) continue;

			Answer a = s.getAnswer(question);
			if (a != null)
			{
				total++;

				if (a.getIsAnswered())
				{
					// does the answer's value match our target answer?
					// Note: assume that the answer is one of the getData() strings
					String[] answers = a.getTypeSpecificAnswer().getData();
					if ((answers != null) && (answers.length > 0))
					{
						for (int i = 0; i < answers.length; i++)
						{
							if (answers[i].equals(target))
							{
								count++;
							}
						}
					}
				}
			}
		}

		if (total > 0)
		{
			int pct = (count * 100) / total;

			return format("results-format-count", Integer.valueOf(pct), Integer.valueOf(count));
		}

		return "";
	}

	/**
	 * Prepare a display string for the date.
	 * 
	 * @param date
	 *        The date.
	 * @return The display string for the date.
	 */
	protected String formatDate(Date date)
	{
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
		String display = format.format(date);

		// remove seconds
		int i = display.lastIndexOf(":");
		if ((i == -1) || ((i + 3) >= display.length())) return display;

		String rv = display.substring(0, i) + display.substring(i + 3);
		return rv;
	}

	/**
	 * Format the fill-in question.
	 * 
	 * @param content
	 *        The building response.
	 * @param tsq
	 *        The type-specific question.
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions.
	 */
	protected void formatFillBlanks(StringBuilder content, FillBlanksQuestionImpl tsq, Question question, List<Submission> submissions)
	{
		boolean caseSensitive = Boolean.valueOf(tsq.getCaseSensitive());
		List<String> corrects = tsq.getCorrectAnswers();

		// for each position
		int pos = 0;
		for (String correct : corrects)
		{
			if (pos > 0) content.append("<br />\n");

			// position pos+1, answer is 'correct'
			content.append(format("results-position", Integer.valueOf(pos + 1), correct));
			content.append("<br />\n");

			// collect the given answers
			List<String> given = new ArrayList<String>();
			for (Submission s : submissions)
			{
				if (s.getIsPhantom()) continue;
				if (!s.getIsComplete()) continue;

				Answer a = s.getAnswer(question);
				if (a != null)
				{
					if (a.getIsAnswered())
					{
						String[] answers = a.getTypeSpecificAnswer().getData();
						if ((answers != null) && (answers.length > pos))
						{
							String answer = answers[pos];
							if (answer != null)
							{
								if (!caseSensitive) answer = answer.toLowerCase();
								if (!given.contains(answer)) given.add(answer);
							}
						}
					}
				}
			}

			Collections.sort(given);

			content.append("<table>\n");

			// show each given and the # times it shows up
			for (String target : given)
			{
				int hits = 0;
				int total = 0;
				for (Submission s : submissions)
				{
					if (s.getIsPhantom()) continue;
					if (!s.getIsComplete()) continue;

					Answer a = s.getAnswer(question);
					if (a != null)
					{
						total++;

						if (a.getIsAnswered())
						{
							// does the answer's value match our target answer?
							// Note: assume that the answer for this position is the nth data element
							String[] answers = a.getTypeSpecificAnswer().getData();
							if ((answers != null) && (answers.length > pos) && (answers[pos] != null))
							{
								if (caseSensitive)
								{
									if (answers[pos].equals(target))
									{
										hits++;
									}
								}
								else
								{
									if (answers[pos].equalsIgnoreCase(target))
									{
										hits++;
									}
								}
							}
						}
					}
				}

				int pct = total > 0 ? (hits * 100) / total : 0;

				// target hits percent
				content.append("<tr><td>");
				content.append(target);
				content.append("</td><td>");
				content.append(format("results-format-count", Integer.valueOf(pct), Integer.valueOf(hits)));
				content.append("</td></tr>\n");
			}

			content.append("</table>\n");

			pos++;
		}

		// unanswered
		content.append("<table>\n");
		content.append("<tr><td colspan=\"3\">&nbsp;</td></tr><tr><td>");
		content.append(format("results-unanswered"));
		content.append("</td>");
		content.append(formatUnanswered(question, submissions));
		content.append("</tr>\n");

		content.append("</table>\n");
	}

	/**
	 * Format the likert question.
	 * 
	 * @param content
	 *        The building response.
	 * @param tsq
	 *        The type-specific question.
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions.
	 */
	protected void formatLikert(StringBuilder content, LikertScaleQuestionImpl tsq, Question question, List<Submission> submissions)
	{
		content.append("<table>\n");
		for (LikertScaleQuestionImpl.LikertScaleQuestionChoice choice : tsq.getChoices())
		{
			content.append("<tr><td>");
			content.append(stripHtml(choice.getText()));
			content.append("</td>");
			content.append(formatCountPercent(question, submissions, choice.getId()));
			content.append("</tr>\n");
		}

		// unanswered
		content.append("<tr><td colspan=\"3\">&nbsp;</td></tr><tr><td>");
		content.append(format("results-unanswered"));
		content.append("</td>");
		content.append(formatUnanswered(question, submissions));
		content.append("</tr>\n");

		content.append("</table>\n");
	}

	/**
	 * Format the match question.
	 * 
	 * @param content
	 *        The building response.
	 * @param tsq
	 *        The type-specific question.
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions.
	 */
	protected void formatMatch(StringBuilder content, MatchQuestionImpl tsq, Question question, List<Submission> submissions)
	{
		List<MatchQuestionPair> pairs = tsq.getPairs();

		int pos = 0;
		for (MatchQuestionPair pair : pairs)
		{
			if (pos > 0) content.append("<br />\n");

			content.append(format("results-match", stripHtml(pair.getMatch())));
			content.append("<br />\n");

			content.append("<table>\n");

			// count for each possible choice
			for (MatchQuestionPair choicePair : pairs)
			{
				formatMatchChoice(content, question, pair.getId(), choicePair.getChoiceId(), choicePair.getChoice(), submissions);
			}

			// add the distractor id
			if (tsq.getDistractor() != null)
			{
				formatMatchChoice(content, question, pair.getId(), tsq.distractor.getChoiceId(), tsq.distractor.getChoice(), submissions);
			}

			content.append("</table>\n");

			pos++;
		}

		// unanswered
		content.append("<table>\n");
		content.append("<tr><td colspan=\"3\">&nbsp;</td></tr><tr><td>");
		content.append(format("results-unanswered"));
		content.append("</td>");
		content.append(formatUnanswered(question, submissions));
		content.append("</tr>\n");

		content.append("</table>\n");
	}

	/**
	 * Format one match's choice statistics.
	 * 
	 * @param content
	 *        The building response.
	 * @param question
	 *        The question.
	 * @param matchId
	 *        The match id.
	 * @param choiceId
	 *        The choice id.
	 * @param choice
	 *        The choice text.
	 * @param submissions
	 *        The submissions.
	 */
	protected void formatMatchChoice(StringBuilder content, Question question, String matchId, String choiceId, String choice,
			List<Submission> submissions)
	{
		int count = 0;
		int total = 0;
		for (Submission s : submissions)
		{
			if (s.getIsPhantom()) continue;
			if (!s.getIsComplete()) continue;

			Answer a = s.getAnswer(question);
			if (a != null)
			{
				total++;

				if (a.getIsAnswered())
				{
					// does this answer's entry for this match id = the choice id?
					// Note: assume that the answer data is match id, choice id, etc...
					String[] answers = a.getTypeSpecificAnswer().getData();
					if (answers != null)
					{
						for (int i = 0; i < answers.length; i++)
						{
							String answerMatchId = answers[i++];
							String answerChoiceId = answers[i];
							if ((answerMatchId != null) && (answerMatchId.equals(matchId)))
							{
								if ((answerChoiceId != null) && (answerChoiceId.equals(choiceId)))
								{
									count++;
								}
							}
						}
					}
				}
			}
		}

		int pct = total > 0 ? (count * 100) / total : 0;

		// 'choice pair' % count
		content.append("<tr><td>");
		content.append(stripHtml(choice));
		content.append("</td><td>");
		content.append(format("results-format-count", Integer.valueOf(pct), Integer.valueOf(count)));
		content.append("</td></tr>\n");
	}

	/**
	 * Format the multiple choice question.
	 * 
	 * @param content
	 *        The building response.
	 * @param tsq
	 *        The type-specific question.
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions.
	 */
	protected void formatMultipleChoice(StringBuilder content, MultipleChoiceQuestionImpl tsq, Question question, List<Submission> submissions)
	{
		content.append("<table>\n");
		for (MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice choice : tsq.getChoices())
		{
			content.append("<tr><td>");
			content.append(stripHtml(choice.getText()));
			content.append("</td>");
			content.append(formatCountPercent(question, submissions, choice.getId()));
			content.append("</tr>\n");
		}

		// unanswered
		content.append("<tr><td colspan=\"3\">&nbsp;</td></tr><tr><td>");
		content.append(format("results-unanswered"));
		content.append("</td>");
		content.append(formatUnanswered(question, submissions));
		content.append("</tr>\n");

		content.append("</table>\n");
	}

	/**
	 * Format the inline answers to this question from the submissions, followed by a count of unanswered.
	 * 
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions to the assessment that has this question.
	 * @param target
	 *        The target answer value.
	 * @return The formatted count and percent.
	 */
	protected void formatTextResponses(StringBuilder content, Question question, List<Submission> submissions)
	{
		int count = 0;
		int total = 0;
		for (Submission s : submissions)
		{
			if (s.getIsPhantom()) continue;
			if (!s.getIsComplete()) continue;

			Answer a = s.getAnswer(question);
			if (a != null)
			{
				total++;

				if (a.getIsAnswered())
				{
					count++;

					TypeSpecificAnswer tsa = a.getTypeSpecificAnswer();
					if (tsa instanceof EssayAnswerImpl)
					{
						String text = ((EssayAnswerImpl) tsa).getAnswerData();
						content.append("<hr>\n");
						content.append(stripHtml(text));
						content.append("\n");
					}
				}
			}
		}

		if (total == count)
		{
			content.append("<hr>\n");
			content.append(format("results-none-unanswered"));
			content.append("\n");
		}

		else
		{
			int pct = ((total - count) * 100) / total;

			content.append("<hr>\n");
			content.append(format("results-count-unanswered", Integer.valueOf(pct), Integer.valueOf(total - count)));
			content.append("\n");
		}
	}

	/**
	 * Format the true false question.
	 * 
	 * @param content
	 *        The building response.
	 * @param tsq
	 *        The type-specific question.
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions.
	 */
	protected void formatTrueFalse(StringBuilder content, TrueFalseQuestionImpl tsq, Question question, List<Submission> submissions)
	{
		content.append("<table>\n");
		for (TrueFalseQuestionImpl.TrueFalseQuestionChoice choice : tsq.getChoices())
		{
			content.append("<tr><td>");
			content.append(stripHtml(choice.getText()));
			content.append("</td>");
			content.append(formatCountPercent(question, submissions, choice.getId()));
			content.append("</tr>\n");
		}

		// unanswered
		content.append("<tr><td colspan=\"3\">&nbsp;</td></tr><tr><td>");
		content.append(format("results-unanswered"));
		content.append("</td>");
		content.append(formatUnanswered(question, submissions));
		content.append("</tr>\n");

		content.append("</table>\n");
	}

	/**
	 * Count the # submissions that saw this question but did not answer.
	 * 
	 * @param question
	 *        The question.
	 * @param submissions
	 *        The submissions to the assessment.
	 * @return The # submissions that saw this question but did not answer.
	 */
	protected String formatUnanswered(Question question, List<Submission> submissions)
	{
		int count = 0;
		int total = 0;
		for (Submission s : submissions)
		{
			if (s.getIsPhantom()) continue;
			if (!s.getIsComplete()) continue;

			Answer a = s.getAnswer(question);
			if (a != null)
			{
				total++;

				if (!a.getIsAnswered())
				{
					count++;
				}
			}
		}

		if (total > 0)
		{
			int pct = (count * 100) / total;

			return format("results-format-count", Integer.valueOf(pct), Integer.valueOf(count));
		}

		return "";
	}

	/**
	 * Remove the html from the source.
	 * 
	 * @param source
	 *        The source string.
	 * @return The source with html removed.
	 */
	protected String stripHtml(String source)
	{
		if (source == null) return "";

		// remove \r - convertFormattedTextToPlaintext won't do this.
		source = M_patternCR.matcher(source).replaceAll("");

		// &nbsp; is common, and convertFormattedTextToPlaintext() will replace it with a strange character
		source = M_patternNBSP.matcher(source).replaceAll(" ");

		source = FormattedText.convertFormattedTextToPlaintext(source);
		return source;
	}
}
