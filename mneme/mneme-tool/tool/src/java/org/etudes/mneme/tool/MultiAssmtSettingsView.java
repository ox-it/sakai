/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1145/mneme-tool/tool/src/java/org/etudes/mneme/tool/MultiAssmtSettingsView.java $
 * $Id: MultiAssmtSettingsView.java 3635 2012-12-02 21:26:23Z ggolden $
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

package org.etudes.mneme.tool;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.ReviewShowCorrect;

import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /multi_assmt_settings view for the mneme tool.
 */
public class MultiAssmtSettingsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(MultiAssmtSettingsView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Dependency: GradesService */
	protected GradesService gradesService = null;

	/** tool manager reference. */
	protected ToolManager toolManager = null;

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
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}
		
        Assessment assessment = (Assessment) assessmentService.newEmptyAssessment(this.toolManager.getCurrentPlacement().getContext());
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		// collect information: the selected assessment
		context.put("assessment", assessment);

		// check if we have gradebook
		context.put("gradebookAvailable", this.gradesService.available(assessment.getContext()));

		// we carry sort and assmtIds for the /tests mode
		String sort = "";
		String assmtIds = "";
		String choiceSettings = "";
		if (params.length == 5)
		{
			sort = params[2];
			assmtIds = params[3];
			choiceSettings = params[4];
		}
		context.put("sort", sort);
		context.put("assmtIds", assmtIds);

		String chSets[] = StringUtil.split(choiceSettings, "+");
		//This sets the included values
		for (String chSet : chSets)
		{
			context.put(chSet, chSet);
		}

		// if we have a focus parameter
		String focus = req.getParameter("focus");
		if (focus != null) context.addFocusId(focus);
		new CKSetup().setCKCollectionAttrib(getDocsPath(), toolManager.getCurrentPlacement().getContext());

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}
		
		String sort = params[2];
		String assmtIds = params[3];
		String choiceSettings = params[4];

		// aid and return
		Assessment assessment = (Assessment) assessmentService.newEmptyAssessment(this.toolManager.getCurrentPlacement().getContext());
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// setup the model: the selected assessment
		context.put("assessment", assessment);
		context.put("assmtIds", assmtIds);

		// read the form
		String destination = uiService.decode(req, context);

		String chSets[] = StringUtil.split(choiceSettings, "+");

		String aIds[] = StringUtil.split(assmtIds, "+");
		for (String aId : aIds)
		{
			Assessment assmt = this.assessmentService.getAssessment(aId);

			// security check
			if (assessmentService.allowEditAssessment(assmt))
			{
				for (String chSet : chSets)
				{
					if (chSet.equals("ao") || chSet.equals("ts"))
					{
						assmt.setHasTriesLimit(assessment.getHasTriesLimit());
						assmt.setTries(assessment.getTries());
					}
					if (chSet.equals("ao") || chSet.equals("tls"))
					{
						assmt.setHasTimeLimit(assessment.getHasTimeLimit());
						assmt.setTimeLimit(assessment.getTimeLimit());
					}
					if (chSet.equals("ao") || chSet.equals("ro"))
					{
						assmt.getReview().setTiming(assessment.getReview().getTiming());
						assmt.getReview().setDate(assessment.getReview().getDate());
						assmt.getReview().setShowFeedback(assessment.getReview().getShowFeedback());
						assmt.getReview().setShowSummary(assessment.getReview().getShowSummary());
						
						if (assmt.getType() != AssessmentType.survey)
						{
							if (assessment.getReview().getShowSummary())
								assmt.getReview().setShowCorrectAnswer(ReviewShowCorrect.yes);
							else
								assmt.getReview().setShowCorrectAnswer(assessment.getReview().getShowCorrectAnswer());
						}
						
					}
					if (chSet.equals("ao") || chSet.equals("ac"))
					{
						if (assmt.getType() != AssessmentType.survey)
						{
							assmt.setMinScoreSet(assessment.getMinScoreSet());
							assmt.setMinScore(assessment.getMinScore());
						}
					}
					if (chSet.equals("ao") || chSet.equals("rs")) assmt.getGrading().setAutoRelease(assessment.getGrading().getAutoRelease());
					if (chSet.equals("ao") || chSet.equals("ma")) assmt.setShowModelAnswer(assessment.getShowModelAnswer());
					if (chSet.equals("ao") || chSet.equals("sg"))
					{
						if (assmt.getHasPoints()) assmt.getGrading().setGradebookIntegration(assessment.getGrading().getGradebookIntegration());
					}
					if (chSet.equals("ao") || chSet.equals("ag"))
					{
						if (assmt.getType() != AssessmentType.survey) assmt.getGrading().setAnonymous(assessment.getGrading().getAnonymous());
					}
					if (chSet.equals("ao") || chSet.equals("ae"))
					{
						assmt.setResultsEmail(assessment.getResultsEmail());
					}
					if (chSet.equals("ao") || chSet.equals("pw"))
					{
						assmt.getPassword().setPassword(assessment.getPassword().getPassword());
					}
					if (chSet.equals("ao") || chSet.equals("hp")) assmt.setRequireHonorPledge(assessment.getRequireHonorPledge());
					if (chSet.equals("ao") || chSet.equals("hns")) assmt.setShowHints(assessment.getShowHints());
					if (chSet.equals("ao") || chSet.equals("sc")) assmt.setShuffleChoicesOverride(assessment.getShuffleChoicesOverride());
					if (chSet.equals("ao") || chSet.equals("navlay"))
					{
						if (assmt.getType() != AssessmentType.assignment)
						{
							assmt.setRandomAccess(assessment.getRandomAccess());
							assmt.setQuestionGrouping(assessment.getQuestionGrouping());
						}
						else
						{
							assmt.setQuestionGrouping(assessment.getQuestionGrouping());
						}
					}
					if (chSet.equals("ao") || chSet.equals("pn"))
						assmt.getParts().setContinuousNumbering(assessment.getParts().getContinuousNumbering());
					if (chSet.equals("ao") || chSet.equals("fm"))
						assmt.getSubmitPresentation().setText(assessment.getSubmitPresentation().getText());
					// commit the save
					try
					{
						this.assessmentService.saveAssessment(assmt);
					}
					catch (AssessmentPermissionException e)
					{
						M_log.warn("Multiple assessments saving " + e);
					}
					catch (AssessmentPolicyException e)
					{
						M_log.warn("Multiple assessments saving " + e);
					}
				}
			}
		}

		if ("DONE".equals(destination))
		{
			destination = "/assessments/" + sort;
		}

		// if destination became null
		if (destination == null)
		{
			destination = context.getDestination();
		}

		// if destination is stay here
		else if (destination.startsWith("STAY:"))
		{
			String[] parts = StringUtil.splitFirst(destination, ":");
			destination = context.getDestination() + "?focus=" + parts[1];
		}
		// redirect to the next destination
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Set the GradesService.
	 * 
	 * @param service
	 *        The GradesService.
	 */
	public void setGradesService(GradesService service)
	{
		this.gradesService = service;
	}
	
	/**
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
}
}
