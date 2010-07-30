package uk.ac.ox.oucs.vle.proxy;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * This is the actual Sakai proxy which talks to the Sakai services.
 * @author buckett
 *
 */
public class SakaiProxyImpl implements SakaiProxy {

	private final static Log log = LogFactory.getLog(SakaiProxyImpl.class);
	
	private UserDirectoryService userService;
	
	private EmailService emailService;
	
	private EventTrackingService eventService;
	
	private ToolManager toolManager;
	
	private ServerConfigurationService serverConfigurationService;
	
	private String fromAddress;
	
	public void setUserService(UserDirectoryService userService) {
		this.userService = userService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public void setEventService(EventTrackingService eventService) {
		this.eventService = eventService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void init() {
		if (fromAddress == null) {
			fromAddress = serverConfigurationService.getString("course-signup.from");
		}
	}
	
	public UserProxy getCurrentUser() {
		User sakaiUser = userService.getCurrentUser();
		UserProxy user = wrapUserProxy(sakaiUser);
		return user;
	}

	public UserProxy findUserById(String id) {
		try {
			return wrapUserProxy(userService.getUser(id));
		} catch (UserNotDefinedException unde) {
			return null;
		}
	}

	public UserProxy findUserByEmail(String email) {
		Collection<User> users = userService.findUsersByEmail(email);
		if (users.size() == 0) {
			return null;
		} else {
			if (users.size() > 1) {
				log.warn("More than one user found with email: "+ email);
			}
			return wrapUserProxy(users.iterator().next());
		}
		
	}

	public UserProxy findUserByEid(String eid) {
		try {
			return wrapUserProxy(userService.getUserByAid(eid));
		} catch (UserNotDefinedException unde) {
			return null;
		}
	}

	public void sendEmail(String to, String subject, String body) {
		String from = fromAddress;
		if (from == null) {
			from = getCurrentUser().getEmail();
		}
		emailService.send(
				from, // from address
				to, // to address
				subject, // subject
				body, // message body
				null, // header to string
				null, // Reply to string
				null // Additional headers
		);
	}
	
	// TODO needs more work.
	public void logEvent(String eventType) {
		// This won't work as we're not going through a normal tool. 
		Placement placement = toolManager.getCurrentPlacement();
		String context = (placement != null)? placement.getContext():null;
		// Need find out what the resource is.
		String resource = null;
		Event event = eventService.newEvent(eventType, resource, context, false, NotificationService.NOTI_OPTIONAL);
		eventService.post(event);
	}

	private UserProxy wrapUserProxy(User sakaiUser) {
		if(sakaiUser == null) {
			return null;
		}
		return new UserProxy(sakaiUser.getId(), sakaiUser.getEid(), sakaiUser.getDisplayName(), sakaiUser.getEmail());
	}

}
