package uk.ac.ox.oucs.vle;

/**
 * Send overnight emails
 * 	email course administrator if course component is about to close
 * 	SESii 16.1 Automated process for reminder emails to be sent to students
 * 	SESii 16.2 Automated email to remind Supervisor and Administrator of any pending approvals
 */

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Calendar;
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
	private Date tomorrowMidnight;
	private Date lastWeekMidnight;
	
	private final int todayInt = 1;
	private final int tomorrowInt = 3;
	private final int lastWeekInt = -7;
	private final int unActionedInt = 7;
	
	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	
	/**
	 * The proxy for getting users.
	 */
	private SakaiProxy proxy;
	
	public void initialise() {
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		lastMidnight = (Date)calendar.getTime();
		
		Calendar lastWeek = (Calendar)calendar.clone();
		lastWeek.add(Calendar.DATE, lastWeekInt);
		lastWeekMidnight = (Date) lastWeek.getTime();
		
		Calendar today = (Calendar)calendar.clone();
		today.add(Calendar.DATE, todayInt);
		todayMidnight = (Date) today.getTime();
		
		Calendar tomorrow = (Calendar)calendar.clone();
		tomorrow.add(Calendar.DATE, tomorrowInt);
		tomorrowMidnight = (Date) tomorrow.getTime();
		
		System.out.println("ModuleImpl.initialise");
		System.out.println("Last Midnight ["+
				DateFormat.getInstance().format(lastMidnight)+"]");
		System.out.println("Today Midnight ["+
				DateFormat.getInstance().format(todayMidnight)+"]");
		System.out.println("Tomorrow Midnight ["+
				DateFormat.getInstance().format(tomorrowMidnight)+"]");
		System.out.println("Last Week Midnight ["+
				DateFormat.getInstance().format(lastWeekMidnight)+"]");
	}
	
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}
	
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	public void update() {
		
		initialise();
		String[] words = new String[0];
		final List<CourseGroupDAO> groups = dao.findCourseGroupByWords(words, Range.ALL, new Date(), false);
		
		modulesClosing(groups);
		modulesStarting(groups);
		attentionSignups();
		/*
		for (CourseGroupDAO group : groups) {
			
			final Set<CourseComponentDAO> components = group.getComponents();
			final Set<CourseComponentDAO> componentsClosing = new HashSet<CourseComponentDAO>();
			final Set<CourseComponentDAO> componentsStarting = new HashSet<CourseComponentDAO>();
			
			for (CourseComponentDAO component : components) {
				
				if (isToday(component.getCloses())) {
					// Component is about to close
					System.out.println("Component is about to close ["+
							component.getId()+":"+
							DateFormat.getInstance().format(component.getCloses())+":"+
							component.getSubject()+"]");
					componentsClosing.add(component);
				}
				
				if (isTomorrow(component.getStarts())) {
					// Component is about to start
					System.out.println("Component is about to start ["+
							component.getId()+":"+
							DateFormat.getInstance().format(component.getCloses())+":"+
							component.getSubject()+"]");
					componentsStarting.add(component);
				}
			}
			
			if (!componentsClosing.isEmpty()) {
				for (String administrator : group.getAdministrators()) {
					sendModuleClosingEmail(administrator, group, componentsClosing, 
							"signup.closing.subject", 
							"signup.closing.body");
				}
			}
			
			for (CourseComponentDAO component : componentsStarting) {
				for (CourseSignupDAO signup : component.getSignups()) {
					if (Status.CONFIRMED == signup.getStatus()) {
						sendModuleStartingEmail(signup, component, 
							"signup.starting.subject", 
							"signup.starting.body");
					}
				}
			}
		}
		*/
	}
	
	/**
	 * email course administrator if course component is about to close
	 * @param groups
	 */
	private void modulesClosing(final List<CourseGroupDAO> groups) {
		
		for (CourseGroupDAO group : groups) {
			
			final Set<CourseComponentDAO> components = group.getComponents();
			final Set<CourseComponentDAO> componentsClosing = new HashSet<CourseComponentDAO>();
			
			for (CourseComponentDAO component : components) {
				
				if (isToday(component.getCloses())) {
					// Component is about to close
					System.out.println("Component is about to close ["+
							component.getId()+":"+
							DateFormat.getInstance().format(component.getCloses())+":"+
							component.getSubject()+"]");
					componentsClosing.add(component);
				}
			}
			
			if (!componentsClosing.isEmpty()) {
				for (String administrator : group.getAdministrators()) {
					sendModuleClosingEmail(administrator, group, componentsClosing, 
							"signup.closing.subject", 
							"signup.closing.body");
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
				
				if (isTomorrow(component.getStarts())) {
					// Component is about to start
					System.out.println("Component is about to start ["+
							component.getId()+":"+
							DateFormat.getInstance().format(component.getStarts())+":"+
							component.getSubject()+"]");
					componentsStarting.add(component);
				}
			}
			
			for (CourseComponentDAO component : componentsStarting) {
				for (CourseSignupDAO signup : component.getSignups()) {
					if (Status.CONFIRMED == signup.getStatus()) {
						sendModuleStartingEmail(signup, component, 
							"signup.starting.subject", 
							"signup.starting.body");
					}
				}
			}
		}
	}
	
	/**
	 * SESii 16.2 Automated email to remind Supervisor and Administrator of any pending approvals
	 */
	private void attentionSignups() {
		
		List<CourseSignupDAO> signups = dao.findSignupStillPendingOrAccepted(unActionedInt);
		System.out.println("ModuleImpl.attentionSignups ["+signups.size()+"]");
		
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
				
				//sendBumpAdministratorEmail(signup, 
				//		"bump.admin.subject", 
				//		"bump.admin.body");
			}
		}
		
		for (Map.Entry<String, Collection<CourseSignupDAO>> entry : supervisors.entrySet()) {
			sendBumpSupervisorEmail(entry.getKey(), 
									entry.getValue(), 
									"bump.supervisor.subject", 
									"bump.supervisor.body");
		}
		
		for (Map.Entry<String, Collection<CourseSignupDAO>> entry : administrators.entrySet()) {
			sendBumpAdministratorEmail(entry.getKey(), 
									entry.getValue(), 
									"bump.admin.subject", 
									"bump.admin.body");
		}
		//sendBumpSupervisorEmail(signup, 
		//		"bump.supervisor.subject", 
		//		"bump.supervisor.body");
	}
	
	private boolean isToday(Date date) {
		if (null != date) {
			if (date.after(lastMidnight) && date.before(todayMidnight)) {
				return true;
			}
		}
		return false;
	}
	private boolean isTomorrow(Date date) {
		if (null != date) {
			if (date.after(todayMidnight) && date.before(tomorrowMidnight)) {
				return true;
			}
		}
		return false;
	}
	
	private void sendModuleClosingEmail(String administrator, CourseGroupDAO group, 
			Collection<CourseComponentDAO> components, 
			String subjectKey, 
			String bodyKey) {
		
		UserProxy recepient = proxy.findUserById(administrator);
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ administrator);
			return;
		}
		String to = recepient.getEmail();
		String subject = MessageFormat.format(rb.getString(subjectKey), new Object[]{group.getTitle()});
		
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
		String body = MessageFormat.format(rb.getString(bodyKey), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	
	private void sendModuleStartingEmail(
			CourseSignupDAO signup, 
			CourseComponentDAO component, 
			String subjectKey, 
			String bodyKey) {
		
		UserProxy recepient = proxy.findUserById(signup.getUserId());
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ signup.getUserId());
			return;
		}
		String to = recepient.getEmail();
		String subject = MessageFormat.format(rb.getString(subjectKey), 
				new Object[]{signup.getGroup().getTitle()});
		
		Object[] baseBodyData = new Object[] {
				signup.getGroup().getTitle(),
				formatComponent(component)
		};
		Object[] bodyData = baseBodyData;
		String body = MessageFormat.format(rb.getString(bodyKey), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	/*
	private void sendBumpAdministratorEmail(CourseSignupDAO signup, 
			String subjectKey, 
			String bodyKey) {
		
		CourseGroupDAO group = signup.getGroup();
		UserProxy student = proxy.findUserById(signup.getUserId());
		Set<String> administrators = group.getAdministrators(); 
		
		for (String administrator : administrators) {
			UserProxy recepient = proxy.findUserById(administrator);
			if (recepient == null) {
				log.warn("Failed to find user for sending email: "+ administrator);
				return;
			}
			String to = recepient.getEmail();
			String subject = MessageFormat.format(rb.getString(subjectKey), new Object[]{group.getTitle()});
		
			StringBuffer componentDetails = new StringBuffer();
			for (CourseComponentDAO component : signup.getComponents()) {
				componentDetails.append("\n");
				componentDetails.append(formatComponent(component));
			}
			
			String url = null;//proxy.getConfirmUrl(signup.getId());
			String advanceUrl = null;//proxy.getAdvanceUrl(signup.getId(), "accept", null);
			
			Object[] bodyData = new Object[] {
				student.getDisplayName(),
				group.getTitle(),
				componentDetails.toString(),
				url,
				advanceUrl
			};
			
			String body = MessageFormat.format(rb.getString(bodyKey), bodyData);
			proxy.sendEmail(to, subject, body);
		}
	}
	*/
	
	/**
	 * 
	 * @param administrator
	 * @param signups
	 * @param subjectKey
	 * @param bodyKey
	 */
	private void sendBumpAdministratorEmail(
			String administratorId,
			Collection <CourseSignupDAO> signups, 
			String subjectKey, 
			String bodyKey) {
		
		UserProxy recepient = proxy.findUserById(administratorId);
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ administratorId);
			return;
		}
		String to = recepient.getEmail();
		String subject = rb.getString(subjectKey);
		StringBuffer signupsDetails = new StringBuffer();
		
		for (CourseSignupDAO signup : signups) {
			signupsDetails.append(formatSignup(signup));
			signupsDetails.append("\n");
		}
		
		String url = null;//proxy.getConfirmUrl(signup.getId());
		String advanceUrl = null;//proxy.getAdvanceUrl(signup.getId(), "accept", null);
		
		Object[] bodyData = new Object[] {
				signupsDetails.toString(),
				url,
				advanceUrl
		};
		String body = MessageFormat.format(rb.getString(bodyKey), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	
	/*
	private void sendBumpSupervisorEmail(CourseSignupDAO signup, 
			String subjectKey, 
			String bodyKey) {
		
		CourseGroupDAO group = signup.getGroup();
		UserProxy recepient = proxy.findUserById(signup.getSupervisorId());
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ signup.getSupervisorId());
			return;
		}
		String to = recepient.getEmail();
		String subject = MessageFormat.format(rb.getString(subjectKey), new Object[]{group.getTitle()});
		
		StringBuffer componentDetails = new StringBuffer();
		for (CourseComponentDAO component : signup.getComponents()) {
			componentDetails.append("\n");
			componentDetails.append(formatComponent(component));
		}
		
		String url = null;//proxy.getConfirmUrl(signup.getId());
		String advanceUrl = null;//proxy.getAdvanceUrl(signup.getId(), "approve", null);
		
		Object[] bodyData = new Object[] {
			group.getTitle(),
			componentDetails.toString(),
			url,
			advanceUrl
		};
		
		String body = MessageFormat.format(rb.getString(bodyKey), bodyData);
		proxy.sendEmail(to, subject, body);
	}
	*/
	/**
	 * 
	 * @param supervisorId
	 * @param signups
	 * @param subjectKey
	 * @param bodyKey
	 */
	private void sendBumpSupervisorEmail(
			String supervisorId,
			Collection<CourseSignupDAO> signups, 
			String subjectKey, 
			String bodyKey) {
		
		UserProxy recepient = proxy.findUserById(supervisorId);
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ supervisorId);
			return;
		}
		String to = recepient.getEmail();
		String subject = rb.getString(subjectKey);
		StringBuffer signupsDetails = new StringBuffer();
		
		for (CourseSignupDAO signup : signups) {
			signupsDetails.append(formatSignup(signup));
			signupsDetails.append("\n");
		}
		
		String url = null;//proxy.getConfirmUrl(signup.getId());
		String advanceUrl = null;//proxy.getAdvanceUrl(signup.getId(), "accept", null);
		
		Object[] bodyData = new Object[] {
				signupsDetails.toString(),
				url,
				advanceUrl
		};
		String body = MessageFormat.format(rb.getString(bodyKey), bodyData);
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
		output.append(component.getSlot());
		output.append(" for ");
		output.append(component.getSessions());
		output.append(" starts in ");
		output.append(component.getWhen());
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
	
	
	public boolean validString(String string) {
		if (null != string && string.trim().length() > 0) {
			return true;
		}
		return false;
	}
}
