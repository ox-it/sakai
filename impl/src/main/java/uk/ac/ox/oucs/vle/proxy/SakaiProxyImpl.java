package uk.ac.ox.oucs.vle.proxy;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.ac.ox.oucs.vle.Email;
import uk.ac.ox.oucs.vle.SakaiProxy;
import uk.ac.ox.oucs.vle.UserProxy;

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
	
	private SiteService siteService;
	
	private PortalService portalService;
	
	private String fromAddress;
	
	public List<Email> emailLog = new ArrayList<Email>();
	
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

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setPortalService(PortalService portalService) {
		this.portalService = portalService;
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
		emailLog.add(new Email(to, subject, body));
	}
	
	public void logEvent(String resourceId, String eventType) {
		Placement placement = getPlacement();
		String context = placement.getContext();
		String resource = "/coursesignup/group/"+ resourceId;
		Event event = eventService.newEvent(eventType, resource, context, false, NotificationService.NOTI_OPTIONAL);
		eventService.post(event);
	}

	/**
	 * Just get the current placement.
	 * @return The current placement.
	 * @throws RunTimeException If there isn't a current placement, this happens
	 * when a request comes through that isn't processed by the portal.
	 */
	private Placement getPlacement() {
		Placement placement = toolManager.getCurrentPlacement();
		if (placement == null) {
			throw new RuntimeException("No current tool placement set.");
		}
		return placement;
	}

	private UserProxy wrapUserProxy(User sakaiUser) {
		if(sakaiUser == null) {
			return null;
		}
		List<String> units = sakaiUser.getProperties().getPropertyList("units");
		return new UserProxy(sakaiUser.getId(), sakaiUser.getEid(), sakaiUser.getDisplayName(), sakaiUser.getEmail(), (units == null)?Collections.EMPTY_LIST:units);
	}

	public String getConfirmUrl(String signupId) {
		return getUrl("/static/pending.jsp#"+ signupId);
	}

	public String getMyUrl() {
		return getUrl("/static/my.jsp");
	}

	private String getUrl(String toolState) {
		Placement currentPlacement = getPlacement();
		String siteId = currentPlacement.getContext();
		ToolConfiguration toolConfiguration = siteService.findTool(currentPlacement.getId());
		String pageUrl = toolConfiguration.getContainingPage().getUrl();
		Map<String, String[]> encodedToolState = portalService.encodeToolState(currentPlacement.getId(), toolState);
		StringBuilder params = new StringBuilder();
		for (Entry<String, String[]> entry : encodedToolState.entrySet()) {
			for(String value: entry.getValue()) {
				params.append("&");
				params.append(entry.getKey());
				params.append("=");
				params.append(URLEncoder.encode(value));
			}
		}
		if (params.length() > 0) {
			pageUrl += "?"+ params.substring(1); // Trim the leading &
		}
		return pageUrl;
	}

	public List<Email> getEmails() {
		return Collections.unmodifiableList(emailLog);
	}

}
