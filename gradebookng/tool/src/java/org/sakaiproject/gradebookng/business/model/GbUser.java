package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.user.api.User;

/**
 * DTO for a user. Enhance as required.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbUser implements Serializable, Comparable<GbUser> {

	@Getter
	private final String userUuid;
	
	@Getter
	private final String eid;

	/**
	 * If displaying an eid, this is the one to display
	 */
	@Getter
	private final String displayId;

	@Getter
	private final String displayName;
	
	@Getter
	private final String firstName;
	
	@Getter
	private final String lastName;

	@Getter
	private final String studentNumber;
	
	private GbUser()
	{
		this("", "", "", "", "", "", "");
	}

	private GbUser(String userUuid, String eid, String displayId, String displayName, String firstName, String lastName, String studentNumber)
	{
		this.userUuid = userUuid;
		this.eid = eid;
		this.displayId = displayId;
		this.displayName = displayName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.studentNumber = studentNumber;
	}
	
	public static GbUser fromUser(final User u)
	{
		return fromUserWithStudentNumber(u, "");
	}
	
	public static GbUser fromUserAcquiringStudentNumber(final User u, GradebookNgBusinessService businessService)
	{
		String num = businessService.getStudentNumber(u, businessService.getCurrentSite().orElse(null));
		return fromUserWithStudentNumber(u, num);
	}
	
	public static GbUser fromUserWithStudentNumber(final User u, final String studentNumber)
	{
		return new GbUser(u.getId(), u.getEid(), u.getDisplayId(), u.getDisplayName(), u.getFirstName(), u.getLastName(), studentNumber);
	}
	
	public static GbUser forDisplayOnly(final String displayId, final String displayName)
	{
		return new GbUser("", "", displayId, displayName, "", "", "");
	}

	public boolean isValid() {
		return StringUtils.isNotBlank(userUuid);
	}

	@Override
	public int compareTo(GbUser user)
	{
		int comp = displayId.compareToIgnoreCase(user.displayId);
		if (comp == 0)
		{
			comp = displayName.compareToIgnoreCase(user.displayName);
		}
		
		return comp;
	}

	@Override
	public String toString() {
		String retVal = displayId;
		if (StringUtils.isNotBlank(studentNumber)) {
			retVal += " (" + studentNumber + ")";
		}

		return retVal;
	}
}
