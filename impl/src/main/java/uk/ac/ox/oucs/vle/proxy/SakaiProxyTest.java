package uk.ac.ox.oucs.vle.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proxy through which all calls to Sakai are made.
 * This should allow us to run outside Sakai, if it starts dealing with more than just
 * users it should probably me removed.
 * @author buckett
 *
 */
public class SakaiProxyTest implements SakaiProxy {

	private List<User> users = new ArrayList<User>();
	
	public List<Email> emailLog = new ArrayList<Email>();
	
	private User current;
	
	public SakaiProxyTest() {
		
		users.add(new User("current", "user", "Current User", "current.user@coll.ox.ac.uk"));
		
		users.add(new User("1", "user0001", "Test User One", "test.user.1@dept.ox.ac.uk"));
		users.add(new User("2", "user0002", "Test User Two", "test.user.2@dept.ox.ac.uk"));
		
		users.add(new User("d86d9720-eba4-40eb-bda3-91b3145729da", "socs0001", "Course Admin 1", "course.admin.1@dept.ox.ac.uk"));
		users.add(new User("c10cdf4b-7c10-423c-8319-2d477051a94e", "socs0002", "Course Admin 2", "course.admin.2@dept.ox.ac.uk"));
		
		// Bulk load of users.
		for (int i = 1; i <=50; i++) {
			users.add(new User("id"+i, "eid"+i, "Full Name "+ i, "user"+i+"@dept.ox.ac.uk"));
		}
		current = users.get(3);
	}
		
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.vle.proxy.SakaiProxy#getCurrentUser()
	 */
	public User getCurrentUser() {
		return current;
	}
	
	public void setCurrentUser(User user) {
		this.current = user;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.vle.proxy.SakaiProxy#findUserById(java.lang.String)
	 */
	public User findUserById(String id) {
		for (User user : users) {
			if (id.equals(user.getId())) {
				return user;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.vle.proxy.SakaiProxy#findUserByEmail(java.lang.String)
	 */
	public User findUserByEmail(String email) {
		for (User user : users) {
			if (email.equals(user.getEmail())) {
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
}
