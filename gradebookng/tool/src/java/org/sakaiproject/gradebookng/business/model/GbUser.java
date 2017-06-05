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

	/**
	 * If displaying an eid, this is the one to display
	 */
	@Getter
	private final String displayId;

	@Getter
	private final String displayName;

	@Getter
	private final String studentNumber;

	public GbUser(final User u) {
		this.userUuid = u.getId();
		this.displayId = u.getDisplayId();
		this.displayName = u.getDisplayName();
		this.studentNumber = "";
	}

	public GbUser(final User u, GradebookNgBusinessService businessService) {
		this.userUuid = u.getId();
		this.displayId = u.getDisplayId();
		this.displayName = u.getDisplayName();
		this.studentNumber = businessService.getStudentNumber(u, businessService.getCurrentSite().orElse(null));
	}

	public GbUser(final String displayID, final String displayName) {
		this.userUuid = "";
		this.displayId = displayID;
		this.displayName = displayName;
		this.studentNumber = "";
	}

	public boolean isValid() {
		return StringUtils.isNotBlank(userUuid);
	}

	@Override
	public int compareTo(GbUser user) {
		String str1 = displayId + " (" + displayName + ")";
		String str2 = user.displayId + " (" + user.displayName + ")";
		return str1.compareTo(str2);
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
