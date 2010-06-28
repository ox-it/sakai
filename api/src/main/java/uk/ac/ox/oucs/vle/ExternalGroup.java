package uk.ac.ox.oucs.vle;

import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.user.api.User;

/**
 * Object to represent an external group which could then be used to form site membership.
 * @author buckett
 *
 */
public interface ExternalGroup {

	public String getId();
	
	/**
	 * Pretty name for the group.
	 * @return
	 */
	public String getName();
	
	/**
	 * Gets additional data about this group.
	 */
	public Map<String, String> getMetadata();
	
	/**
	 * Members of this group. Defined as an iterator so that large groups can easily to represented.
	 * @return
	 */
	public Iterator<User> getMembers();
	
	/**
	 * Get the EIDs of all the members of this group.
	 * @return
	 */
	public Iterator<String> getMemberEids();
	
}
