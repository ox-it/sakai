/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2013, 2014 Etudes, Inc.
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

package org.etudes.mneme.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.AssessmentSubmissionStatus;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionCompletionStatus;
import org.etudes.util.api.AccessAdvisor;
import org.etudes.util.api.MasteryAdvisor;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * The "FormatListDecoration" format delegate for the mneme tool.
 */
public class FormatListDecorationDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatListDecorationDelegate.class);

	/** Dependency (optional, self-injected): AccessAdvisor. */
	protected transient AccessAdvisor accessAdvisor = null;

	/** Dependency (optional, self-injected): MasteryAdvisor. */
	protected transient MasteryAdvisor masteryAdvisor = null;

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
		Object o = context.get("submission");
		if (!(o instanceof Submission)) return value.toString();
		Submission submission = (Submission) o;

		String rv = null;

		// see if we are blocked by an access advisor
		boolean blocked = false;
		if (this.accessAdvisor != null)
		{
			blocked = this.accessAdvisor.denyAccess("sakai.mneme", submission.getAssessment().getContext(), submission.getAssessment().getId(),
					submission.getUserId());
		}

		// check mastery level
		boolean belowMastery = false;
		String masteryPoints = null;
		if (this.masteryAdvisor != null)
		{
			belowMastery = this.masteryAdvisor.failedToMaster("sakai.mneme", submission.getAssessment().getContext(), submission.getAssessment()
					.getId(), submission.getUserId());
			if (belowMastery)
			{
				Integer masteryPercent = this.masteryAdvisor.masteryLevelPercent("sakai.mneme", submission.getAssessment().getContext(), submission
						.getAssessment().getId(), submission.getUserId());
				if (masteryPercent != null)
				{
					masteryPoints = FormatScoreDelegate.formatScore(Float.valueOf(submission.getAssessment().getParts().getTotalPoints().floatValue()
							* (masteryPercent.floatValue() / 100.0f)));
				}
			}
		}

		// get the status
		AssessmentSubmissionStatus status = submission.getAssessmentSubmissionStatus();

		// non-user submission status, 0 points, not answered
		Boolean nonUserZeroEmpty = ((submission.getCompletionStatus() != SubmissionCompletionStatus.userFinished)
				&& ((!submission.getIsReleased()) || (submission.getTotalScore() == null) || (submission.getTotalScore() == 0.0)) && (submission
				.getIsUnanswered()));

		switch (status)
		{
			case future:
			{
				rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/future.gif\" alt=\""
						+ context.getMessages().getString("format-list-decoration-future") + "\" title=\""
						+ context.getMessages().getString("format-list-decoration-future") + "\" /><br /><span style=\"font-size:smaller\">"
						+ context.getMessages().getString("format-list-decoration-future") + "</span>";
				break;
			}

			case hiddenTillOpen:
			{
				rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/future.gif\" alt=\""
						+ context.getMessages().getString("format-list-decoration-future") + "\" title=\""
						+ context.getMessages().getString("format-list-decoration-future") + "\" /><br /><span style=\"font-size:smaller\">"
						+ context.getMessages().getString("format-list-decoration-future") + "</span>";
				break;
			}

			case ready:
			{
				if (!blocked)
				{
					rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/begin.gif\" alt=\""
							+ context.getMessages().getString("format-list-decoration-todo") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-todo") + "\" /><br /><span style=\"font-size:smaller\">"
							+ context.getMessages().getString("format-list-decoration-todo") + "</span>";
				}
				break;
			}

			case overdueReady:
			{
				if (!blocked)
				{
					rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/exit.gif\" alt=\""
							+ context.getMessages().getString("format-list-decoration-inprogress") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-inprogress") + "\" />"
							+ "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/warning.png\" alt=\""
							+ context.getMessages().getString("format-list-decoration-urgent") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-urgent") + "\" />" + "<br /><span style=\"font-size:smaller\">"
							+ context.getMessages().getString("format-list-decoration-overdue-ready") + "</span>";
				}
				break;
			}

			case inProgressAlert:
			{
				if (!blocked)
				{
					rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/exit.gif\" alt=\""
							+ context.getMessages().getString("format-list-decoration-inprogress") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-inprogress") + "\" />"
							+ "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/warning.png\" alt=\""
							+ context.getMessages().getString("format-list-decoration-urgent") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-urgent") + "\" />" + "<br /><span style=\"font-size:smaller\">"
							+ context.getMessages().getString("format-list-decoration-inprogress-urgent") + "</span>";
				}
				break;
			}

			case inProgress:
			{
				if (!blocked)
				{
					rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/exit.gif\" alt=\""
							+ context.getMessages().getString("format-list-decoration-inprogress") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-inprogress") + "\" /><br /><span style=\"font-size:smaller\">"
							+ context.getMessages().getString("format-list-decoration-inprogress") + "</span>";
				}
				break;
			}

			case completeReady:
			{
				String icon = "/icons/finish.gif";
				String msgSelector = "format-list-decoration-complete";
				String repeatMsgSelector = "format-list-decoration-complete-repeat";
				if (nonUserZeroEmpty)
				{
					icon = "/icons/missed-try-again.png";
					msgSelector = "format-list-decoration-missed";
					repeatMsgSelector = "format-list-decoration-missed-repeat";
				}

				String repeatMsg = context.getMessages().getString(repeatMsgSelector);
				if (belowMastery && (!nonUserZeroEmpty))
				{
					icon = "/icons/not-mastered.png";
					Object[] args = new Object[1];
					args[0] = masteryPoints;

					msgSelector = "format-list-decoration-submitted";
					String selector = "format-list-decoration-complete-repeat-mastery";
					if ((!submission.getIsReleased()) || submission.getHasUnscoredAnswers() || submission.getHasUngradedSiblings())
					{
						selector = "format-list-decoration-complete-repeat-mastery-pending";
					}
					repeatMsg = context.getMessages().getFormattedMessage(selector, args);
				}

				if (!blocked)
				{
					rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + icon + "\" alt=\""
							+ context.getMessages().getString(msgSelector) + "\" title=\"" + context.getMessages().getString(msgSelector) + "\" />"
							+ "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/begin.gif\" alt=\""
							+ context.getMessages().getString("format-list-decoration-repeat") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-repeat") + "\" />" + "<br /><span style=\"font-size:smaller\">"
							+ repeatMsg + "</span>";
				}
				break;
			}

			case overdueCompleteReady:
			{
				String icon = "/icons/finish.gif";
				String msgSelector = "format-list-decoration-complete";
				if (nonUserZeroEmpty)
				{
					icon = "/icons/missed-try-again.png";
					msgSelector = "format-list-decoration-missed";
				}

				String repeatMsg = context.getMessages().getString("format-list-decoration-complete-repeat-overdue");
				if (belowMastery && (!nonUserZeroEmpty))
				{
					icon = "/icons/not-mastered.png";
					Object[] args = new Object[1];
					args[0] = masteryPoints;

					msgSelector = "format-list-decoration-submitted";
					String selector = "format-list-decoration-complete-repeat-overdue-mastery";
					if ((!submission.getIsReleased()) || submission.getHasUnscoredAnswers() || submission.getHasUngradedSiblings())
					{
						selector = "format-list-decoration-complete-repeat-overdue-mastery-pending";
					}

					repeatMsg = context.getMessages().getFormattedMessage(selector, args);
				}

				if (!blocked)
				{
					rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + icon + "\" alt=\""
							+ context.getMessages().getString(msgSelector) + "\" title=\"" + context.getMessages().getString(msgSelector) + "\" />"
							+ "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/begin.gif\" alt=\""
							+ context.getMessages().getString("format-list-decoration-repeat") + "\" title=\""
							+ context.getMessages().getString("format-list-decoration-repeat") + "\" />" + "<br /><span style=\"font-size:smaller\">"
							+ repeatMsg + "</span>";
				}
				break;
			}

			case complete:
			{
				String icon = "/icons/finish.gif";
				String msgSelector = "format-list-decoration-complete";
				String detailSelector = "format-list-decoration-complete";

				Object[] args = new Object[1];
				args[0] = masteryPoints;

				if (nonUserZeroEmpty)
				{
					icon = "/icons/exclamation.png";
					msgSelector = "format-list-decoration-missed";
					detailSelector = "format-list-decoration-missed";
				}
				else if (belowMastery)
				{
					msgSelector = "format-list-decoration-submitted";
					icon = "/icons/not-mastered.png";
					if ((!submission.getIsReleased()) || submission.getHasUnscoredAnswers() || submission.getHasUngradedSiblings())
					{
						detailSelector = "format-list-decoration-complete-not-mastered-pending";
					}
					else
					{
						detailSelector = "format-list-decoration-complete-not-mastered";
					}
				}

				String msg = context.getMessages().getFormattedMessage(msgSelector, args);
				String detail = context.getMessages().getFormattedMessage(detailSelector, args);
				rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + icon + "\" alt=\"" + msg + "\" title=\"" + msg
						+ "\" /><br /><span style=\"font-size:smaller\">" + detail + "</span>";
				break;
			}
			case over:
			{
				rv = "<img style=\"border-style: none;\" src=\"" + context.get("sakai.return.url") + "/icons/cancel.gif\" alt=\""
						+ context.getMessages().getString("format-list-decoration-overdue") + "\" title=\""
						+ context.getMessages().getString("format-list-decoration-overdue") + "\" /><br /><span style=\"font-size:smaller\">"
						+ context.getMessages().getString("format-list-decoration-overdue") + "</span>";
				break;
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object formatObject(Context context, Object value)
	{
		return value;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		// check if there is an access advisor - if not, that's ok.
		this.accessAdvisor = (AccessAdvisor) ComponentManager.get(AccessAdvisor.class);

		// check if there is an access advisor - if not, that's ok.
		this.masteryAdvisor = (MasteryAdvisor) ComponentManager.get(MasteryAdvisor.class);

		M_log.info("init()");
	}
}
