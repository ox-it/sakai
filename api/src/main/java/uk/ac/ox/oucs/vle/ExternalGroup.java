package uk.ac.ox.oucs.vle;

import java.util.Iterator;

import org.sakaiproject.user.api.User;

/**
 * Object to represent an external group which could then be used to form site membership.
 * This is an interface so the specifics of LDAP or some other service aren't exposed through.
 * @author buckett
 *
 */
public interface ExternalGroup {

	/**
	 * The external group ID.
	 * @return
	 */
	public String getId();
	
	/**
	 * Pretty name for the group.
	 * @return
	 */
	public String getName();
	
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
