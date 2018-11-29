package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class ExternalGroupImpl implements ExternalGroup {

	private static final Log log = LogFactory.getLog(ExternalGroupImpl.class);
	
	private final String id;
	private final String name;
	
	private final ExternalGroupManagerImpl groupManagerImpl;
	private final UserDirectoryService userDirectoryService;
	
	ExternalGroupImpl(String id, String name, ExternalGroupManagerImpl groupManagerImpl, UserDirectoryService userDirectoryService) {
		this.id = id;
		this.name = name;
		this.groupManagerImpl = groupManagerImpl;
		this.userDirectoryService = userDirectoryService;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return ExternalGroupImpl.class.getName()+ "( ID: "+ id+ ", Name: "+ name;
	}
	
	public Iterator<User> getMembers() {
		Collection<String> memberEids = groupManagerImpl.findMembers(id);
		List<User> members = new ArrayList<User>(memberEids.size());
		for (String eid: memberEids) {
			try {
				User user = userDirectoryService.getUserByEid(eid);
				members.add(user);
			} catch (UserNotDefinedException unde) {
				log.warn("Couldn't find member ("+ eid+ ") of group ("+ id+ ")");
			}
		}
		return members.iterator();
	}

	public Iterator<String> getMemberEids() {
		return groupManagerImpl.findMembers(id).iterator();
	}

}
