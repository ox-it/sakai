/******************************************************************************
 * ChownEvaluationTemplateProducer.java - created by fengr@vt.edu on Nov 16, 2006
 *
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 *
 * A copy of the Educational Community License has been included in this
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import java.util.ArrayList;
import java.util.List;

/**
 * This page allows the user to chnage owner of evaluations
 *
 * @author Nick Wilson
 */
public class ChownEvaluationProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    public static final String VIEW_ID = "chown_evaluation";
    public String getViewID(){
        return VIEW_ID;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
        this.navBarRenderer = navBarRenderer;
    }
    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        String actionBean = "setupEvalBean.";

        UIMessage.make(tofill, "chown-evaluation-title", "chownevaluation.page.title");

        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;

        if (evalViewParams.evaluationId != null) {
            EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);
            UIBranchContainer chownDiv = UIBranchContainer.make(tofill,"chownDiv:");
            UIMessage.make(chownDiv, "chown-evaluation-confirm-text", "chownevaluation.confirm.text", new Object[] {evaluation.getTitle()});
            UIMessage.make(tofill, "cancel-command-link", "general.cancel.button");

            UIForm form = UIForm.make(chownDiv, "chown-evaluation-form");
            UIMessage.make(form, "chown-evaluation-newownerlabel", "chownevaluation.chown.label");
            UIMessage.make(form, "chown-evaluation-newownertext", "chownevaluation.chown.text");
            UIInput.make(form, "chown-evaluation-newowner", actionBean + "evaluationOwner");
            UICommand chownCmd = UICommand.make(form, "chown-evaluation-button",
                    UIMessage.make("chownevaluation.chown.button"), actionBean + "chownEvalAction");
            chownCmd.parameters.add(new UIELBinding(actionBean + "evaluationId", evaluation.getId().toString()));
        } else {
            throw new IllegalArgumentException("evaluationId must be set for this view");
        }
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> i = new ArrayList<NavigationCase>();
        i.add(new NavigationCase("success", new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID)));
        return i;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalViewParameters();
    }

}