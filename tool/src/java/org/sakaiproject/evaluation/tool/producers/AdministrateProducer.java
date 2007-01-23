/******************************************************************************
 * AdministrateProducer.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvaluationConstant;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles administration of the evaluation system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AdministrateProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "administrate"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
    }
    
    private String ADMIN_WBL = "settingsBean";
    
    private void makeBoolean(UIContainer parent, String ID, String adminkey) {
      // Must use "composePath" here since admin keys currently contain periods
      UIBoundBoolean.make(parent, ID, adminkey == null? null : PathUtil.composePath(ADMIN_WBL, adminkey)); 
    }
    
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);

		if (! userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this page");
		}

		/*
		 * top links here
		 */
		UIOutput.make(tofill, "administrate-title", messageLocator.getMessage("administrate.page.title") ); //$NON-NLS-1$ //$NON-NLS-2$
		if (userAdmin) {
			UIInternalLink.make(tofill, "control-panel-toplink", messageLocator.getMessage("controlpanel.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleViewParameters(ControlPanelProducer.VIEW_ID));
		}
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "control-scales-toplink", messageLocator.getMessage("administrate.top.control.scales"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(ScaleControlProducer.VIEW_ID));			
	
		//System Settings
		UIForm form = UIForm.make(tofill, "basic-form"); //$NON-NLS-1$		
		UIOutput.make(form, "system-settings-header", messageLocator.getMessage("administrate.system.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "system-settings-instructions", messageLocator.getMessage("administrate.system.settings.instructions")); //$NON-NLS-1$ //$NON-NLS-2$

		//Instructor Settings
		UIOutput.make(form, "instructor-settings-header", messageLocator.getMessage("administrate.instructor.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
        makeBoolean(form, "instructors-eval-create", EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS); //$NON-NLS-1$ 
		UIOutput.make(form, "instructors-eval-create-note", messageLocator.getMessage("administrate.instructors.eval.create.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether instructors can view results or not
		UISelect instViewResults = UISelect.make(form, "instructors-view-results"); //$NON-NLS-1$
		instViewResults.selection = new UIInput();
		instViewResults.selection.valuebinding = new ELReference("#{administrativeBean.instViewResults}"); //$NON-NLS-1$
		UIBoundList instViewResultsValues = new UIBoundList();
		instViewResultsValues.setValue(EvaluationConstant.ADMINSTRATIVE_SETTING_OPTIONS);
		instViewResults.optionlist = instViewResultsValues;
		instViewResults.optionnames = instViewResultsValues;	
		UIOutput.make(form, "instructors-view-results-note", messageLocator.getMessage("administrate.instructors.view.results.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		makeBoolean(form, "instructors-email-students", EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS); //$NON-NLS-1$ 
		UIOutput.make(form, "instructors-email-students-note", messageLocator.getMessage("administrate.instructors.email.students.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether instructors must use evaluations from above
		UISelect instMustUse = UISelect.make(form, "instructors-hierarchy"); //$NON-NLS-1$
		instMustUse.selection = new UIInput();
		instMustUse.selection.valuebinding = new ELReference("#{administrativeBean.instUseEvalFromAbove}"); //$NON-NLS-1$
		UIBoundList instMustUseValues = new UIBoundList();
		instMustUseValues.setValue(EvaluationConstant.ADMINSTRATIVE_SETTING_OPTIONS);
		instMustUse.optionlist = instMustUseValues;
		instMustUse.optionnames = instMustUseValues;	
		UIOutput.make(form, "instructors-hierarchy-note", messageLocator.getMessage("administrate.instructors.hierarchy.email.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for number of questions intructors can add
		UISelect numQuestionsInst = UISelect.make(form, 
										"instructors-num-questions",				//$NON-NLS-1$
										EvaluationConstant.NUM_QUESTIONS_INST_ADMINS,
										"#{administrativeBean.instQuestionAdd}", 	//$NON-NLS-1$
										((Integer)settings.get(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER)).toString()); 
		UIOutput.make(form, "instructors-num-questions-note", messageLocator.getMessage("administrate.instructors.num.questions.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Student Settings
		UIOutput.make(form, "student-settings-header", messageLocator.getMessage("administrate.student.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$

		//Select for whether students can leave questions unanswered or not
		UISelect stuLeaveUnans = UISelect.make(form, "students-unanswered"); //$NON-NLS-1$
		stuLeaveUnans.selection = new UIInput();
		stuLeaveUnans.selection.valuebinding = new ELReference("#{administrativeBean.stuQuesUnanswered}"); //$NON-NLS-1$
		UIBoundList stuLeaveUnansValues = new UIBoundList();
		stuLeaveUnansValues.setValue(EvaluationConstant.ADMINSTRATIVE_SETTING_OPTIONS);
		stuLeaveUnans.optionlist = stuLeaveUnansValues;
		stuLeaveUnans.optionnames = stuLeaveUnansValues;	
		UIOutput.make(form, "students-unanswered-note", messageLocator.getMessage("administrate.students.unanswered.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether student can modify responses upto due date
		UISelect stuModifyAns = UISelect.make(form, "students-modify-responses"); //$NON-NLS-1$
		stuModifyAns.selection = new UIInput();
		stuModifyAns.selection.valuebinding = new ELReference("#{administrativeBean.stuModifyResponses}"); //$NON-NLS-1$
		UIBoundList stuModifyAnsValues = new UIBoundList();
		stuModifyAnsValues.setValue(EvaluationConstant.ADMINSTRATIVE_SETTING_OPTIONS);
		stuModifyAns.optionlist = stuModifyAnsValues;
		stuModifyAns.optionnames = stuModifyAnsValues;	
		UIOutput.make(form, "students-modify-responses-note", messageLocator.getMessage("administrate.students.modify.responses.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether students can view results
		UISelect stuViewResults = UISelect.make(form, "students-view-results"); //$NON-NLS-1$
		stuViewResults.selection = new UIInput();
		stuViewResults.selection.valuebinding = new ELReference("#{administrativeBean.stuViewResults}"); //$NON-NLS-1$
		UIBoundList stuViewResultsValues = new UIBoundList();
		stuViewResultsValues.setValue(EvaluationConstant.ADMINSTRATIVE_SETTING_OPTIONS);
		stuViewResults.optionlist = stuViewResultsValues;
		stuViewResults.optionnames = stuViewResultsValues;	
		UIOutput.make(form, "students-view-results-note", messageLocator.getMessage("administrate.students.view.results.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Administrator Settings
		UIOutput.make(form, "administrator-settings-header", messageLocator.getMessage("administrate.admin.settings.header"));		 //$NON-NLS-1$ //$NON-NLS-2$

		//Select for number of questions admins can add
		UISelect numQuestionsAdmin = UISelect.make(form, 
										"admin-hierarchy-num-questions",						//$NON-NLS-1$
										EvaluationConstant.NUM_QUESTIONS_INST_ADMINS,
										"#{administrativeBean.adminsBelowAddQuestions}",		//$NON-NLS-1$
										((Integer)settings.get(EvalSettings.ADMIN_ADD_ITEMS_NUMBER)).toString()); 
		UIOutput.make(form, "admin-hierarchy-num-questions-note", messageLocator.getMessage("administrate.admin.hierarchy.num.questions.note")); //$NON-NLS-1$ //$NON-NLS-2$

		makeBoolean(form, "admin-view-instructor-questions", null); //$NON-NLS-1$ 
		UIOutput.make(form, "admin-view-instructor-questions-note", messageLocator.getMessage("administrate.admin.view.instructor.questions.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "admin-super-modify-question", null); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "admin-super-modify-question-note", messageLocator.getMessage("administrate.admin.super.modify.question.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form, "general-settings-header", messageLocator.getMessage("administrate.general.settings.header"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form, "general-helpdesk-email", "#{administrativeBean.genHelpdeskEmail}", (String)(settings.get(EvalSettings.FROM_EMAIL_ADDRESS))); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "general-helpdesk-email-note", messageLocator.getMessage("administrate.general.helpdesk.email.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Select for number of responses before results could be viewed
		UISelect numResponsesBeforeView= UISelect.make(form, 
												"general-responses-before-view",						//$NON-NLS-1$
												EvaluationConstant.NUM_RESPONSES_BEFORE_VIEW_RESULTS,
												"#{administrativeBean.genResponsesBeforeViewResult}",	//$NON-NLS-1$
												((Integer)settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS)).toString());
		UIOutput.make(form, "general-responses-before-view-note", messageLocator.getMessage("administrate.general.responses.before.view.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		makeBoolean(form, "general-na-allowed", EvalSettings.NOT_AVAILABLE_ALLOWED); //$NON-NLS-1$ 
		UIOutput.make(form, "general-na-allowed-note", messageLocator.getMessage("administrate.general.na.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		
		// Select for maximum number of questions in a block
		UISelect maxQuestionsInBlock = UISelect.make(form, 
										"general-max-questions-block",						//$NON-NLS-1$
										EvaluationConstant.MAX_QUESTIONS_IN_BLOCK,
										"#{administrativeBean.genMaxQuestionsInBlock}", 	//$NON-NLS-1$
										((Integer)settings.get(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK)).toString());
		UIOutput.make(form, "general-max-questions-block-note", messageLocator.getMessage("administrate.general.max.questions.block.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		// Select for template sharing and visibility settings
		UISelect templateSharingVisibility = UISelect.make(form, "general-template-sharing"); //$NON-NLS-1$
		templateSharingVisibility.selection = new UIInput();
		templateSharingVisibility.selection.valuebinding = new ELReference("#{administrativeBean.genTemplateSharing}"); //$NON-NLS-1$
		UIBoundList templateSharingVisibilityValues = new UIBoundList();
		String[] sharingList = 
		{
			messageLocator.getMessage("modifytemplatetitledesc.sharing.private"),
			messageLocator.getMessage("modifytemplatetitledesc.sharing.visible"),
			messageLocator.getMessage("modifytemplatetitledesc.sharing.shared"),
			messageLocator.getMessage("modifytemplatetitledesc.sharing.public")
		};		
		templateSharingVisibilityValues.setValue(sharingList);
		templateSharingVisibility.optionlist = templateSharingVisibilityValues;
		templateSharingVisibility.optionnames = templateSharingVisibilityValues;	
		UIOutput.make(form, "general-template-sharing-note", messageLocator.getMessage("administrate.general.template.sharing.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether question category would be course or instructor
		UISelect defaultQuesCategory = UISelect.make(form, "general-default-question"); //$NON-NLS-1$
		defaultQuesCategory.selection = new UIInput();
		defaultQuesCategory.selection.valuebinding = new ELReference("#{administrativeBean.genDefaultQuestionCategory}"); //$NON-NLS-1$
		UIBoundList defaultQuesCategoryValues = new UIBoundList();
		defaultQuesCategoryValues.setValue(EvaluationConstant.ADMINSTRATIVE_SETTING_OPTIONS);
		defaultQuesCategory.optionlist = defaultQuesCategoryValues;
		defaultQuesCategory.optionnames = defaultQuesCategoryValues;	
		UIOutput.make(form, "general-default-question-category", messageLocator.getMessage("administrate.general.default.question.category.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		
		makeBoolean(form, "general-use-stop-date", null); //$NON-NLS-1$ 
		UIOutput.make(form, "general-use-stop-date-note", messageLocator.getMessage("administrate.general.use.stop.date.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "general-expert-templates", EvalSettings.USE_EXPERT_TEMPLATES); //$NON-NLS-1$ 
		UIOutput.make(form, "general-expert-templates-note", messageLocator.getMessage("administrate.general.expert.templates.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "general-expert-questions", EvalSettings.USE_EXPERT_ITEMS); 
		UIOutput.make(form, "general-expert-questions-note", messageLocator.getMessage("administrate.general.expert.questions.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "general-same-view-date",  null); //$NON-NLS-1$ 
		UIOutput.make(form, "general-same-view-date-note", messageLocator.getMessage("administrate.general.same.view.date.note"));	 //$NON-NLS-1$ //$NON-NLS-2$

		// Save settings button
		UICommand.make(form, "saveSettings", messageLocator.getMessage("administrate.save.settings.button"), "#{administrativeBean.saveAdminSettingsAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$		
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		//TODO
		return i;
	}
}
