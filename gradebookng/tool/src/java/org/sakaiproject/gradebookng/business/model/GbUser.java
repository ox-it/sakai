package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.gradebookng.business.exception.AnonymousConstraintViolationException;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
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

	/** 
	 * Maps sections to anonIds for this user. This *should* have up to one entry, but it handles rare situations where a student appears in two crosslisted sections with separate anonIds
	 */
	@Getter
	private final Map<String, Integer> sectionAnonIdMap;
	
	private GbUser()
	{
		this("", "", "", "", "", "", "", new LinkedHashMap<>());
	}

	private GbUser(String userUuid, String eid, String displayId, String displayName, String firstName, String lastName, String studentNumber, Map<String, Integer> sectionAnonIdMap)
	{
		this.userUuid = userUuid;
		this.eid = eid;
		this.displayId = displayId;
		this.displayName = displayName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.studentNumber = studentNumber;
		this.sectionAnonIdMap = sectionAnonIdMap;
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

	public static GbUser fromUserAcquiringStudentNumberAndAnonIdMap(final User u, GradebookNgBusinessService businessService)
	{
		String num = businessService.getStudentNumber(u, businessService.getCurrentSite().orElse(null));
		Map<String, Integer> sectionAnonIdMap = businessService.getSectionAnonIdMapForUser(u.getEid());
		return fromUserWithStudentNumberAndAnonIdMap(u, num, sectionAnonIdMap);
	}
	
	public static GbUser fromUserWithStudentNumber(final User u, final String studentNumber)
	{
		return new GbUser(u.getId(), u.getEid(), u.getDisplayId(), u.getDisplayName(), u.getFirstName(), u.getLastName(), studentNumber, new LinkedHashMap<>());
	}

	public static GbUser fromUserWithStudentNumberAndAnonIdMap(final User u, final String studentNumber, final Map<String, Integer> sectionAnonIdMap)
	{
		return new GbUser(u.getId(), u.getEid(), u.getDisplayId(), u.getDisplayName(), u.getFirstName(), u.getLastName(), studentNumber, sectionAnonIdMap);
	}
	
	public static GbUser forDisplayOnly(final String displayId, final String displayName)
	{
		return new GbUser("", "", displayId, displayName, "", "", "", Collections.emptyMap());
	}

	public boolean isValid() {
		return StringUtils.isNotBlank(userUuid);
	}

	/**
	 * Gets an anonId for this user (site-wide, Ie. not specific to any particular section). An AnonymousConstraintViolationException will be thrown if an anonId is not found (users should be filtered before invoking this).
	 * @return
	 */
	public String getAnonId()
	{
		return getAnonId((String)null);
	}

	/**
	 * Gets an anonId for this user. Throws an AnonymousConstraintViolationException if an anonId is not found (users should be filtered before invoking this)
	 * @param sectionId the user's anonId will be retrieved for this section. If null / blank, will retrieve an anonId for any section in the site.
	 * An AnonymousConstraintViolationException will be thrown if:
	 *     1) A non-blank sectionId is specified, but no anonId is found for that section or
	 *     2) The sectionId is null / blank, and no anonId is found in the site
	 * To avoid these, ensure that your student lists are filtered to include only students with anonymousIDs before attempting to do any anonId retrievals
	 * @return
	 */
	public String getAnonId(String sectionId)
	{
		Integer anonId = null;
		if (StringUtils.isNotBlank(sectionId))
		{
			anonId = sectionAnonIdMap.get(sectionId);
			if (anonId == null)
			{
				throw new AnonymousConstraintViolationException("Requested an anonId from a GbUser for a specific section; none found. This GbUser should have been filtered");
			}
		}
		else
		{
			// Grab the first anonId available
			Iterator<Integer> itAnonIds = sectionAnonIdMap.values().iterator();
			if (itAnonIds.hasNext())
			{
				anonId = itAnonIds.next();
			}

			if (anonId == null)
			{
				throw new AnonymousConstraintViolationException("Requested an anonId from a GbUser, but no anonIds exist for this user. Either anonIds weren't populated in this context, or this GbUser should have been filtered");
			}
		}

		return String.valueOf(anonId);
	}

	/**
	 * Uses settings.getGroupFilter to determine which section to look up an anonId for, then invokes getAnonId(String sectionId)
	 * @param settings
	 * @return
	 */
	public String getAnonId(GradebookUiSettings settings)
	{
		GbGroup group = settings.getGroupFilter();
		String section = group == null ? null : group.getProviderId();
		return getAnonId(section);
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
