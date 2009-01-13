package uk.ac.ox.oucs.vle;

import java.util.Iterator;
import java.util.List;

import org.sakaiproject.user.api.User;

public class ExternalGroupImpl implements ExternalGroup {

	private final String id;
	private final String name;
	
	private List<User> cachedMembers;
	
	private final ExternalGroupManagerImpl groupManagerImpl;
	
	ExternalGroupImpl(String id, String name, ExternalGroupManagerImpl groupManagerImpl ) {
		this.id = id;
		this.name = name;
		this.groupManagerImpl = groupManagerImpl;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Iterator<User> getMembers() {
		// TODO Should cache this call here.
		return groupManagerImpl.findMembers(id);
	}
	
	public String toString() {
		return ExternalGroupImpl.class.getName()+ "( ID: "+ id+ ", Name: "+ name+ ", Cached Members: "+ ((cachedMembers==null)?null:cachedMembers.size()+ ")");
	}

}
