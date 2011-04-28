package uk.ac.ox.oucs.vle;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;

public class ModuleImpl implements Module {

	private static final Log log = LogFactory.getLog(ModuleImpl.class);
	
	private final static ResourceLoader rb = new ResourceLoader("messages");
	
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
	
	public void update() {
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date lastMidnight = (Date)calendar.getTime();
		System.out.println("Last Midnight using calendar object ["+DateFormat.getInstance().format(lastMidnight)+"]");
		
		calendar.add(Calendar.DATE, 1);
		Date nextMidnight = (Date) calendar.getTime();
		System.out.println("Next Midnight using calendar object ["+DateFormat.getInstance().format(nextMidnight)+"]");
		
		String[] words = new String[0];
		final List<CourseGroupDAO> groups = dao.findCourseGroupByWords(words, Range.ALL, new Date());
		
		for (CourseGroupDAO group : groups) {
			final Set<CourseComponentDAO> components = group.getComponents();
			final Set<CourseComponentDAO> closings = new HashSet<CourseComponentDAO>();
			for (CourseComponentDAO component : components) {
				
				if (component.getCloses().after(lastMidnight) && component.getCloses().before(nextMidnight)) {
					// Component is about to close
					System.out.println("Component is about to close ["+component.getId()+":"+DateFormat.getInstance().format(component.getCloses())+":"+component.getSubject()+"]");
					closings.add(component);
				}
			}
			
			if (!closings.isEmpty()) {
				
				if (validString(group.getAdministrator())) {
					sendModuleClosingEmail(group, closings, 
							"signup.closing.subject", 
							"signup.closing.body");
				}
			}
		}
	}
	
	private void sendModuleClosingEmail(CourseGroupDAO group, 
			Collection<CourseComponentDAO> components, 
			String subjectKey, 
			String bodyKey) {
		
		UserProxy recepient = proxy.findUserById(group.getAdministrator());
		if (recepient == null) {
			log.warn("Failed to find user for sending email: "+ group.getAdministrator());
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

	
	public boolean validString(String string) {
		if (null != string && string.trim().length() > 0) {
			return true;
		}
		return false;
	}
}
