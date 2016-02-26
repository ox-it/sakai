/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2015 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ox.oucs.vle.*;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Centralise all our event based email sending.
 */
public class EmailSendingService {

    private Log log = LogFactory.getLog(EmailSendingService.class);

    private SakaiProxy proxy;
    private UserPlacementDAO placementDAO;
    private Set<EmailRule> rules;

    public void setProxy(SakaiProxy proxy) {
        this.proxy = proxy;
    }

    public void setPlacementDAO(UserPlacementDAO placementDAO) {
        this.placementDAO = placementDAO;
    }

    public void setRules(Set<EmailRule> rules) {
        this.rules = rules;
    }

    public void init() {
        // This sets up all the rules so they can be used.
        for (EmailRule rule: rules) {
            rule.setProxy(proxy);
            rule.setService(this);
        }
    }

    /**
     * This is the main entry point and runs all the email sending rules
     * against the state sendMails.
     * @param stateChange
     */
    public void applyRules(StateChange stateChange) {
        Set<EmailRule> actions = new HashSet<>();
        // See which rules match
        for (EmailRule rule: rules) {
            if (rule.matches(stateChange)) {
                actions.add(rule);
            }
        }

        // Perform the actions for the matched rules
        for (EmailRule action: actions) {
            action.perform(stateChange);
        }
    }

    /**
     * Generic method for sending out a signup email.
     *
     * @param userId The ID of the user who the message should be sent to.
     * @param signupDao The signup the message is about.
     * @param subjectKey The resource bundle key for the subject
     * @param bodyKey The resource bundle key for the body.
     * @param additionalBodyData Additional objects used to format the email body. Typically used for the confirm URL.
     */
    public void sendSignupEmail(String userId, CourseSignup signup, String subjectKey,
                                String bodyKey,
                                Object[] additionalBodyData) {

        UserProxy recepient = proxy.findUserById(userId);
        if (recepient == null) {
            log.warn("Failed to find user for sending email: "+ userId);
            return;
        }
        Person person = signup.getUser();
        if (person == null) {
            log.warn("Failed to find the user who made the signup: " + signup.getId());
            return;
        }

        String to = recepient.getEmail();
        String componentDetails = formatSignup(signup);
        Object[] baseBodyData = new Object[] {
                proxy.getCurrentUser().getDisplayName(), // {0}
                componentDetails, // {1}
                signup.getGroup().getTitle(), // {2}
                person.getName(), // {3}
                (null == person.getDegreeProgram()) ? "unknown" : person.getDegreeProgram() // {4}
        };
        Object[] data = baseBodyData;
        if (additionalBodyData != null) {
            data = new Object[data.length + additionalBodyData.length];
            System.arraycopy(baseBodyData, 0, data, 0, baseBodyData.length);
            System.arraycopy(additionalBodyData, 0, data, baseBodyData.length, additionalBodyData.length);
        }

        String subject = MessageFormat.format(proxy.getMessage(subjectKey), data);
        String body = MessageFormat.format(proxy.getMessage(bodyKey), data);
        proxy.sendEmail(to, subject, body);
    }

    /**
     *  @param signupDao
     * @param subjectKey
     * @param bodyKey
     * @param additionalBodyData
     * @param courseSignupService
     */
    public void sendStudentSignupEmail(CourseSignup signup, String subjectKey,
                                       String bodyKey,
                                       Object[] additionalBodyData) {

        Person signupUser = signup.getUser();
        if (signupUser == null) {
            log.warn("Failed to find the user who made the signup: "+ signup.getId());
            return;
        }

        String to = signupUser.getEmail();
        String componentDetails = formatSignup(signup);
        Object[] baseBodyData = new Object[] {
                signupUser.getName(),
                componentDetails,
                signup.getGroup().getTitle(),
        };

        Object[] data = baseBodyData;
        if (additionalBodyData != null) {
            data = new Object[data.length + additionalBodyData.length];
            System.arraycopy(baseBodyData, 0, data, 0, baseBodyData.length);
            System.arraycopy(additionalBodyData, 0, data, baseBodyData.length, additionalBodyData.length);
        }
        String subject = MessageFormat.format(proxy.getMessage(subjectKey), data);
        String body = MessageFormat.format(proxy.getMessage(bodyKey), data);
        proxy.sendEmail(to, subject, body);
    }

    /**
     * This sends a signup waiting email to an administrator
     *
     * @param userId The ID of the administrator.
     * @param signup The waiting signup.
     * @param subjectKey The resource bundle subject key.
     * @param bodyKey  The resource bundle body key.
     * @param additionalBodyData Additional data for the body resource bundle.
     */
    public void sendSignupWaitingEmail(String userId, CourseSignup signup, String subjectKey, String bodyKey, Object[] additionalBodyData) {

        UserProxy recepient = proxy.findUserById(userId);
        if (recepient == null) {
            log.warn("Failed to find user for sending email: "+ userId);
            return;
        }

        Person signupUser = signup.getUser();
        if (signupUser == null) {
            log.warn("Failed to find the user who made the signup: " + signup.getUser().getId());
            return;
        }

        String to = recepient.getEmail();
        String componentDetails = formatSignup(signup);
        Object[] baseBodyData = new Object[] {
                proxy.getCurrentUser().getDisplayName(),
                componentDetails,
                signup.getGroup().getTitle(),
                signupUser.getName(),
                (null == signupUser.getDegreeProgram()) ? "unknown" : signupUser.getDegreeProgram()
        };
        Object[] bodyData = baseBodyData;
        if (additionalBodyData != null) {
            bodyData = new Object[bodyData.length + additionalBodyData.length];
            System.arraycopy(baseBodyData, 0, bodyData, 0, baseBodyData.length);
            System.arraycopy(additionalBodyData, 0, bodyData, baseBodyData.length, additionalBodyData.length);
        }
        String subject = MessageFormat.format(proxy.getMessage(subjectKey), bodyData);
        String body = MessageFormat.format(proxy.getMessage(bodyKey), bodyData);
        proxy.sendEmail(to, subject, body);
    }


    // Computer-Aided Formal Verification (Computing Laboratory)
    // - Lectures: 16 lectures for 16 sessions starts in Michaelmas 2010 with Daniel Kroening

    /**
     * This formats the details of a signup into plain text.
     *
     * @param signupDao
     * @return
     */
    public String formatSignup(CourseSignup signup) {
        StringBuilder output = new StringBuilder(); // TODO Maybe should use resource bundle.
        output.append(signup.getGroup().getTitle());
        output.append(" (");
        output.append(signup.getGroup().getDepartment());
        output.append(" )\n");
        for(CourseComponent component: signup.getComponents()) {
            output.append("  - ");
            output.append(component.getTitle());
            output.append(" for ");
            output.append(component.getSessions());
            if (component.getWhen() != null) {
                output.append(" starts in ");
                output.append(component.getWhen());
            }
            Person presenter = component.getPresenter();
            if(presenter != null) {
                output.append(" with ");
                output.append(presenter.getName());
            }
            output.append("\n");
        }
        return output.toString();
    }


    public CourseUserPlacementDAO savePlacement(String userId, String placementId) {
        CourseUserPlacementDAO placementDao = placementDAO.findUserPlacement(userId);
        if (null == placementDao) {
            placementDao = new CourseUserPlacementDAO(userId);
        }
        placementDao.setPlacementId(placementId);
        placementDAO.save(placementDao);
        return placementDao;
    }
}
