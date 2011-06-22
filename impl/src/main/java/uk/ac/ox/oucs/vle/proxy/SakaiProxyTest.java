package uk.ac.ox.oucs.vle.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.vle.Email;
import uk.ac.ox.oucs.vle.SakaiProxy;
import uk.ac.ox.oucs.vle.UserProxy;

/**
 * Proxy through which all calls to Sakai are made.
 * This should allow us to run outside Sakai, if it starts dealing with more than just
 * users it should probably me removed.
 * @author buckett
 *
 */
public class SakaiProxyTest implements SakaiProxy {
	
	private final static Log log = LogFactory.getLog(SakaiProxyTest.class);

	private List<UserProxy> users = new ArrayList<UserProxy>();
	
	public List<Email> emailLog = new ArrayList<Email>();
	
	private UserProxy current;
	
	public SakaiProxyTest() {
		
		users.add(new UserProxy("current", "user", "Current User", "current.user@coll.ox.ac.uk", null, null, Collections.singletonList("Some College")));
		
		users.add(new UserProxy("1", "user0001", "Test User One", "test.user.1@dept.ox.ac.uk", null, null, Collections.EMPTY_LIST));
		users.add(new UserProxy("2", "user0002", "Test User Two", "test.user.2@dept.ox.ac.uk", null, null, Collections.EMPTY_LIST));

		// So some imports work.
		users.add(new UserProxy("3", "bras1760", "Katherine Allen", "katherine.allen@bnc.ox.ac.uk", null, null, Collections.EMPTY_LIST));
		
		users.add(new UserProxy("d86d9720-eba4-40eb-bda3-91b3145729da", "socs0001", "Course Admin 1", "course.admin.1@dept.ox.ac.uk", null, null, Collections.EMPTY_LIST));
		users.add(new UserProxy("c10cdf4b-7c10-423c-8319-2d477051a94e", "socs0002", "Course Admin 2", "course.admin.2@dept.ox.ac.uk", null, null, Collections.EMPTY_LIST));
		
		
		// Bulk load of users.
		for (int i = 1; i <=50; i++) {
			users.add(new UserProxy("id"+i, "eid"+i, "Full Name "+ i, "user"+i+"@dept.ox.ac.uk", null, null, Collections.EMPTY_LIST));
		}
		current = users.get(0);
	}
		
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.vle.proxy.SakaiProxy#getCurrentUser()
	 */
	public UserProxy getCurrentUser() {
		return current;
	}
	
	public void setCurrentUser(UserProxy user) {
		this.current = user;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.vle.proxy.SakaiProxy#findUserById(java.lang.String)
	 */
	public UserProxy findUserById(String id) {
		for (UserProxy user : users) {
			if (id.equals(user.getId())) {
				return user;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.vle.proxy.SakaiProxy#findUserByEmail(java.lang.String)
	 */
	public UserProxy findUserByEmail(String email) {
		for (UserProxy user : users) {
			if (email.equals(user.getEmail())) {
				return user;
			}
		}
		return null;
	}
	
	public UserProxy findUserByEid(String eid) {
		for (UserProxy user : users) {
			if (eid.equals(user.getEid())) {
				return user;
			}
		}
		return null;
	}
	
	public void sendEmail(String to, String subject, String body) {
		emailLog.add(new Email(to, subject, body));
	}
	
	public List<Email> getEmails() {
		return Collections.unmodifiableList(emailLog);
	}

	public String getConfirmUrl(String signupId) {
		return "/someurl/"+ signupId;
	}
	
	public String getDirectUrl(String signupId) {
		return "/someurl/"+ signupId;
	}

	public String getMyUrl() {
		return "/my/";
	}
	
	public void logEvent(String resource, String eventType) {
		log.info("Event - user: "+getCurrentUser().getId()+ " resource:"+
				resource+ " type "+ eventType);
	}
}
