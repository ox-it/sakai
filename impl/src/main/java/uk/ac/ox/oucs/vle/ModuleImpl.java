/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
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
package uk.ac.ox.oucs.vle;

/**
 * Module Signup Overnight Jobs
 * 
 * 	email course administrator if course component is about to close
 * 
 * 	SESii 16.1 (WL-2273) Automated process for reminder emails to be sent to students
 * 				
 * 	SESii 16.2 Automated email to remind Supervisor and Administrator of any pending approvals
 */

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public class ModuleImpl implements Module {

	private static final Log log = LogFactory.getLog(ModuleImpl.class);
	
	private final static ResourceLoader rb = new ResourceLoader("messages");
	
	private Date lastMidnight;
	private Date todayMidnight;
	private Date aboutToStart;
	
	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	
	/**
	 * The proxy for getting users.
	 */
	private SakaiProxy proxy;
	
	
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}
	
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	/**
	 * 
	 */
	public void initialise() {
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		lastMidnight = (Date)calendar.getTime();
		
		int aboutToCloseDays = getAboutToCloseDays();
		Calendar today = (Calendar)calendar.clone();
		today.add(Calendar.DATE, aboutToCloseDays);
		todayMidnight = (Date) today.getTime();
		
		int aboutToStartDays = getAboutToStartDays();
		Calendar tomorrow = (Calendar)calendar.clone();
		tomorrow.add(Calendar.DATE, aboutToStartDays);
		aboutToStart = (Date) tomorrow.getTime();
		
		log.info("ModuleImpl.initialise");
		log.info("Last Midnight ["+
				DateFormat.getInstance().format(lastMidnight)+"]");
		log.info("Today Midnight ["+
				DateFormat.getInstance().format(todayMidnight)+"]");
		log.info("About to Start ["+
				DateFormat.getInstance().format(aboutToStart)+"]");
		
		log.info("Courses about to Close [close date between "+
				DateFormat.getInstance().format(lastMidnight)+" and "+
				DateFormat.getInstance().format(todayMidnight)+"]");
		
		log.info("Courses about to Start [start date between "+
				DateFormat.getInstance().format(todayMidnight)+" and "+
				DateFormat.getInstance().format(aboutToStart)+"]");
	}
	
	/**
	 * 
	 */
	public void update() {
		
		initialise();
		String[] words = new String[0];
		// This gets all the groups there are as all the values are ignored.
		final List<CourseGroupDAO> groups = dao.findCourseGroupByWords(words, Range.ALL, new Date(), false);
		
		modulesClosing(groups);
		modulesStarting(groups);
		
		int reminderDays = getReminderDays();
		attentionSignups(reminderDays);
	}
	
	/**
	 * Email course administrator if course component is about to close
	 * @param groups A list of all groups that may have closing components.
	 */
	private void modulesClosing(final List<CourseGroupDAO> groups) {
		
		for (CourseGroupDAO group : groups) {
			
			final Set<CourseComponentDAO> components = group.getComponents();
			final Set<CourseComponentDAO> componentsClosing = new HashSet<CourseComponentDAO>();
			
			for (CourseComponentDAO component : components) {
				
				if (isToday(component.getCloses())) {
					// Component is about to close
					log.info("Component is about to close ["+
							component.getPresentationId()+":"+
							DateFormat.getInstance().format(component.getCloses())+":"+
							component.getSubject()+"]");
					componentsClosing.add(component);
				}
			}
			
			if (!componentsClosing.isEmpty()) {
				for (String administrator : group.getAdministrators()) {
					sendModuleClosingEmail(administrator, group, componentsClosing);
				}
			}
		}
	}
	
	/**
	 * SESii 16.1 Automated process for reminder emails to be sent to students
	 * @param groups
	 */
	private void modulesStarting(final List<CourseGroupDAO> groups) {
		
		for (CourseGroupDAO group : groups) {
			
			final Set<CourseComponentDAO> components = group.getComponents();
			final Set<CourseComponentDAO> componentsStarting = new HashSet<CourseComponentDAO>();
			
			for (CourseComponentDAO component : components) {
				
				if (isAboutToStart(component.getStarts())) {
					// Component is about to start
					log.info("Component is about to start ["+
							component.getPresentationId()+":"+
							DateFormat.getInstance().format(component.getStarts())+":"+
							component.getSubject()+"]");
					componentsStarting.add(component);
				}
			}
			
			for (CourseComponentDAO component : componentsStarting) {
				for (CourseSignupDAO signup : component.getSignups()) {
					if (Status.CONFIRMED == signup.getStatus()) {
						sendModuleStartingEmail(signup, component);
					}
				}
			}
		}
	}
	
	/**
	 * SESii 16.2 Automated email to remind Supervisor and Administrator of any pending approvals
	 */
	private void attentionSignups(int reminderDays) {
		
		List<CourseSignupDAO> signups = dao.findSignupStillPendingOrAccepted(reminderDays);
		log.info("ModuleImpl.attentionSignups ["+signups.size()+"]");
		
		Map<String, Collection<CourseSignupDAO>> supervisors = 
				new HashMap<String, Collection<CourseSignupDAO>>();
		Map<String, Collection<CourseSignupDAO>> administrators = 
				new HashMap<String, Collection<CourseSignupDAO>>();
		
		for (CourseSignupDAO signup : signups) {
			
			if (Status.ACCEPTED == signup.getStatus()) {
				
				Collection<CourseSignupDAO> set = 
						(Collection<CourseSignupDAO>)supervisors.get(signup.getSupervisorId());
				
				if (null == set) {
					set = new HashSet<CourseSignupDAO>();
					set.add(signup);
				} else {
					set.add(signup);
					supervisors.remove(signup.getSupervisorId());
				}
				supervisors.put(signup.getSupervisorId(), set);
				
			}
			
			if (Status.PENDING == signup.getStatus()) {
				
				CourseGroupDAO group = signup.getGroup();
				Collection <String> admins = group.getAdministrators();
				
				for (String admin : admins) {
					
					Collection<CourseSignupDAO> set = 
							(Collection<CourseSignupDAO>)administrators.get(admin);
					
					if (null == set) {
						set = new HashSet<CourseSignupDAO>();
						set.add(signup);
					} else {
						set.add(signup);
						administrators.remove(admin);
					}
					administrators.put(admin, set);
				}
				
			}
		}
		
		for (Map.Entry<String, Collection<CourseSignupDAO>> entry : supervisors.entrySet()) {
			sendBumpSupervisorEmail(entry.getKey(), entry.getValue());
		}
		
		for (Map.Entry<String, Collection<CourseSignupDAO>> entry : administrators.entrySet()) {
			sendBumpAdministratorEmail(entry.getKey(), entry.getValue());
		}
		
	}
	
	private boolean isToday(Date date) {
		if (null != date) {
			if (date.after(lastMidnight) && date.before(todayMidnight)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isAboutToStart(Date date) {
		if (null != date) {
			if (date.after(todayMidnight) && date.before(aboutToStart)) {
				return true;
			}
		}
		return false;
	}
	
	private void sendModuleClosingEmail(String administrator, CourseGroupDAO group, 
			Collection<CourseComponentDAO> components) {
		
		UserProxy recepient = proxy.findUserById(administrator);
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ administrator);
			return;
		}
		String to = recepient.getEmail();
		String subject = MessageFormat.format(rb.getString("signup.closing.subject"), 
				new Object[]{group.getTitle()});
		
		StringBuffer componentDetails = new StringBuffer();
		for (CourseComponentDAO component : components) {
			componentDetails.append("\n");
			componentDetails.append(formatComponent(component));
		}
		Object[] baseBodyData = new Object[] {
				group.getTitle(),
				componentDetails.toString()
		};
		Object[] bodyData = baseBodyData;
		String body = MessageFormat.format(rb.getString("signup.closing.body"), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	
	private void sendModuleStartingEmail(
			CourseSignupDAO signup, 
			CourseComponentDAO component) {
		
		UserProxy recepient = proxy.findUserById(signup.getUserId());
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ signup.getUserId());
			return;
		}
		CourseUserPlacementDAO placementDao = dao.findUserPlacement(signup.getUserId());
		if (null == placementDao) {
			log.warn("Failed to find placement for sending email: "+ signup.getUserId());
			return;
		}
		String placementId = placementDao.getPlacementId();
		
		Object[] data = new Object[] {
				signup.getGroup().getTitle(),
				new SimpleDateFormat("EEE d MMM yyyy").format(component.getStarts()),
				formatComponent(component),
				proxy.getMyUrl(placementId)
		};
		
		String to = recepient.getEmail();
		String subject = MessageFormat.format(rb.getString("signup.starting.subject"), data);
		String body = MessageFormat.format(rb.getString("signup.starting.body"), data);
		proxy.sendEmail(to, subject, body);
	}

	private void sendBumpAdministratorEmail(
			String administratorId,
			Collection <CourseSignupDAO> signups) {
		
		UserProxy recepient = proxy.findUserById(administratorId);
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ administratorId);
			return;
		}
		String to = recepient.getEmail();
		String subject = rb.getString("bump.admin.subject");
		StringBuffer signupsDetails = new StringBuffer();
		
		for (CourseSignupDAO signup : signups) {
			signupsDetails.append(formatSignup(signup));
			signupsDetails.append("\n");
		}
		
		CourseUserPlacementDAO placementDao = dao.findUserPlacement(administratorId);
		if (null == placementDao) {
			log.warn("Failed to find placement for sending email: "+ administratorId);
			return;
		}
		String placementId = placementDao.getPlacementId();
		String url = proxy.getConfirmUrl(null, placementId);
		String advanceUrl = null;//proxy.getAdvanceUrl(signup.getId(), "accept", null);
		
		Object[] bodyData = new Object[] {
				signupsDetails.toString(),
				url,
				advanceUrl
		};
		String body = MessageFormat.format(rb.getString("bump.admin.body"), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	
	private void sendBumpSupervisorEmail(
			String supervisorId,
			Collection<CourseSignupDAO> signups) {
		
		UserProxy recepient = proxy.findUserById(supervisorId);
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ supervisorId);
			return;
		}
		String to = recepient.getEmail();
		String subject = rb.getString("bump.supervisor.subject");
		StringBuffer signupsDetails = new StringBuffer();
		
		for (CourseSignupDAO signup : signups) {
			signupsDetails.append(formatSignup(signup));
			signupsDetails.append("\n");
		}
		
		CourseUserPlacementDAO placementDao = dao.findUserPlacement(supervisorId);
		if (null == placementDao) {
			log.warn("Failed to find placement for sending email: "+ supervisorId);
			return;
		}
		String placementId = placementDao.getPlacementId();
		String url = proxy.getConfirmUrl(null, placementId);
		String advanceUrl = null;//proxy.getAdvanceUrl(signup.getId(), "accept", null);
		
		Object[] bodyData = new Object[] {
				signupsDetails.toString(),
				url,
				advanceUrl
		};
		String body = MessageFormat.format(rb.getString("bump.supervisor.body"), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	
	/**
	 * 
	 * @param component
	 * @return
	 */
	public String formatComponent(CourseComponentDAO component) {
		
		StringBuilder output = new StringBuilder(); 
		output.append(component.getSubject());
		output.append(": ");
		output.append(component.getTitle());
		output.append(": ");
		// Slot held extra details of the teaching (time)
		if (null != component.getTeachingDetails()) {
			output.append(component.getTeachingDetails());
			output.append(" ");
		}
		// TODO - The sessions should really be an int and we should check > 1
		if (component.getSessions() != null && validString(component.getSessions())) {
			output.append(String.format(" for %s sessions ", component.getSessions()));
			output.append(component.getSessions());
		}
		output.append(" starts on ");
		if (component.getStarts() != null) {
			output.append(component.getStarts());
		} else {
			output.append(component.getStartsText());
		}

		// TODO this needs to mapped to a name
		output.append(component.getTermcode());
		if(validString(component.getTeacherName())) {
			output.append(" with ");
			output.append(component.getTeacherName());
		}
		
		return output.toString();
	}

	/**
	 * 
	 * @param signup
	 * @return
	 */
	public String formatSignup(CourseSignupDAO signup) {
		
		StringBuilder output = new StringBuilder(); 
		CourseGroupDAO group = signup.getGroup();
		UserProxy student = proxy.findUserById(signup.getUserId());
		
		output.append(student.getDisplayName());
		output.append(" for the course ");
		output.append(group.getTitle());
		output.append(".");
		
		return output.toString();
	}
	
	protected int getTodayInt() {
		return proxy.getConfigParam("course-signup.site-id", 1);
	}
	
	protected int getReminderDays() {
		return proxy.getConfigParam("reminder.days", 7);
	}
	
	protected int getAboutToStartDays() {
		return proxy.getConfigParam("abouttostart.days", 2);
	}
	
	protected int getAboutToCloseDays() {
		return proxy.getConfigParam("abouttoclose.days", 1);
	}
	
	public boolean validString(String string) {
		if (null != string && string.trim().length() > 0) {
			return true;
		}
		return false;
	}
}
