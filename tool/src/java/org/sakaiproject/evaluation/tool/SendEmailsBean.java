/**
 * $Id$
 * $URL$
 * SetupEvalBean.java - evaluation - Mar 18, 2008 4:38:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.util.HashMap;

import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalEmailMessage;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


/**
 * This action bean helps with the sending evaluation emails manually
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SendEmailsBean {

    /**
     * This should be set to the evalId we are currently working with
     */
    public Long evaluationId;
    public String evalGroupId;
    public String subject;
    public String message;
    public String sendToConstant;
    public String sendTo;

    private EvalEmailsLogic emailsLogic;
    public void setEmailsLogic(EvalEmailsLogic emailsLogic) {
        this.emailsLogic = emailsLogic;
    }
    
    private EvalEvaluationService evalEvaluationService;
    public void setEvalEvaluationService(EvalEvaluationService evalEvaluationService) {
    	this.evalEvaluationService = evalEvaluationService;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }

    /**
     * Handles the email sending action
     */
    public String sendEmailAction() {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }

        if (EvalUtils.isBlank(subject)
                || EvalUtils.isBlank(message)
                //|| EvalUtils.isBlank(sendToConstant)) {
        		|| EvalUtils.isBlank(sendTo)) {
            messages.addMessage( new TargettedMessage("evalnotify.all.required",
                    new Object[] {}, TargettedMessage.SEVERITY_ERROR));
            return "failure";
        }

        String[] evalGroupIds = null;
        if (evalGroupId != null) {
            evalGroupIds = new String[] {evalGroupId};
        }
        EvalEvaluation evaluation = evalEvaluationService.getEvaluationById(evaluationId);
        
        EvalEmailMessage emailMessage = emailsLogic.makeEmailMessage(message, subject, evaluation, null, new HashMap<String, String>());
        String sent[] = emailsLogic.sendEmailMessages(emailMessage.message, emailMessage.subject, evaluationId, evalGroupIds, sendTo);
        
        messages.addMessage( new TargettedMessage("evalnotify.sent.mails",
                new Object[] { sent.length }, TargettedMessage.SEVERITY_INFO));
        return "success";
    }

}
