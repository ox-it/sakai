/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 Etudes, Inc.
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
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.Submission;

/**
 * The "AccessSubmissionsQuestionAnswers" format delegate for the mneme tool.
 */
public class AccessSubmissionsQuestionScoresDelegate extends FormatDelegateImpl
{
	protected class Distribution
	{
		protected String count = null;
		protected int countNum = 1;
		protected String percent = null;
		protected Float score = 0f;
		protected String text = null;

		public Distribution(Float score, String text)
		{
			this.score = score;
			this.text = text;
		}

		public String getCount()
		{
			return this.count;
		}

		public String getPercent()
		{
			return this.percent;
		}

		public String getText()
		{
			return this.text;
		}
	}

	/** Our log. */
	private static Log M_log = LogFactory.getLog(AccessSubmissionsQuestionScoresDelegate.class);

	/**
	 * Format a score to 2 decimal places, trimming ".0" if present.
	 * 
	 * @param score
	 *        The score to format.
	 * @return The formatted score
	 */
	protected static String formatScore(Context context, Float score)
	{
		String earnedStr = context.getMessages().getFormattedMessage("format-score-earned", null);
		if (score == null) return "- "+earnedStr;

		// round to two places
		String rv = Float.toString(Math.round(score * 100.0f) / 100.0f);

		// get rid of ".00"
		if (rv.endsWith(".00"))
		{
			rv = rv.substring(0, rv.length() - 3);
		}

		// get rid of ".0"
		if (rv.endsWith(".0"))
		{
			rv = rv.substring(0, rv.length() - 2);
		}

		return rv+earnedStr;
	}

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public String format(Context context, Object value)
	{
		return value.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object formatObject(Context context, Object value)
	{
		// value is the submissions list
		if (value == null) return null;
		if (!(value instanceof List)) return value;
		List<Submission> submissions = (List<Submission>) value;

		// "question" is the Question
		Object o = context.get("question");
		if (o == null) return value;
		if (!(o instanceof Question)) return value;
		Question question = (Question) o;

		List<Answer> answers = new ArrayList<Answer>();
		for (Submission s : submissions)
		{
			if (s.getIsPhantom()) continue;
			if (!s.getIsComplete()) continue;

			Answer a = s.getAnswer(question);
			if (a != null)
			{
				answers.add(a);
			}
		}

		// find the possible scores and their distribution (count)
		List<Distribution> dist = new ArrayList<Distribution>();
		for (Answer a : answers)
		{
			Float score = a.getEvaluation().getScore();
			String scoreDisplay = formatScore(context,score);

			boolean found = false;
			for (Distribution d : dist)
			{
				if (d.text.equals(scoreDisplay))
				{
					d.countNum++;
					found = true;
					break;
				}
			}

			if (!found)
			{
				Distribution d = new Distribution(score, scoreDisplay);
				dist.add(d);
			}
		}

		// figure percent based on # answers
		int total = answers.size();
		for (Distribution d : dist)
		{
			// format the count
			Object[] args = new Object[1];
			args[0] = Integer.valueOf(d.countNum);
			d.count = context.getMessages().getFormattedMessage("format-count", args);

			// percent
			int pct = (d.countNum * 100) / total;
			args[0] = Integer.valueOf(pct);
			d.percent = context.getMessages().getFormattedMessage("format-percent-earned", args);
		}

		// sort by score asc.
		Collections.sort(dist, new Comparator<Distribution>()
		{
			public int compare(Distribution arg0, Distribution arg1)
			{
				// null (ungraded) is lower than everyone
				if (arg0.score == null) return -1;

				// everyone is higher than null
				if (arg1.score == null) return 1;

				if (arg0.score == null && arg1.score == null) return 0;

				return arg0.score.compareTo(arg1.score);
			}
		});

		return dist;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}
}
