package uk.ac.ox.oucs.vle.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Proxy through which all calls to Sakai are made.
 * This should allow us to run outside Sakai, if it starts dealing with more than just
 * users it should probably me removed.
 * @author buckett
 *
 */
public class SakaiProxy {

	private List<User> users = new ArrayList<User>();
	
	public SakaiProxy() {
		
		users.add(new User("current", "user", "Current User", "current.user@coll.ox.ac.uk"));
		
		users.add(new User("1", "user0001", "Test User One", "test.user.1@dept.ox.ac.uk"));
		users.add(new User("2", "user0002", "Test User Two", "test.user.2@dept.ox.ac.uk"));
		
		users.add(new User("d86d9720-eba4-40eb-bda3-91b3145729da", "socs0001", "Course Admin 1", "course.admin.1@dept.ox.ac.uk"));
		users.add(new User("c10cdf4b-7c10-423c-8319-2d477051a94e", "socs0002", "Course Admin 2", "course.admin.2@dept.ox.ac.uk"));
	}
		
	public User getCurrentUser() {
		return users.get(0);
	}
	
	public User findUserById(String id) {
		for (User user : users) {
			if (id.equals(user.getId())) {
				return user;
			}
		}
		return null;
	}
	
	public User findUserByEmail(String email) {
		for (User user : users) {
			if (email.equals(user.getEmail())) {
				return user;
			}
		}
		return null;
	}
}
