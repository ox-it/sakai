package org.sakaiproject.component.gradebook.owl.anongrading;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.LearningContext;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.coursemanagement.owl.anongrading.AnonAwareEnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;

/**
 *
 * @author plukasew
 */
public class AnonAwareEnrollmentRecordImpl implements AnonAwareEnrollmentRecord, Serializable
{
	private EnrollmentRecord rec;
	private Map<String, String> anonIds;

	public AnonAwareEnrollmentRecordImpl(Map<String, String> sectionAnonIds, EnrollmentRecord er)
	{
		if (sectionAnonIds == null || er == null)
		{
			throw new IllegalArgumentException("Constructor params cannot be null");
		}
		anonIds = sectionAnonIds;
		rec = er;
	}

	@Override
	public String getAnonId(String optionalSectionEid)
	{
		String id = "";
		if (StringUtils.isEmpty(optionalSectionEid))
		{
			// no section, so what to do?
			// return the first value, for now
			// OWLTODO: consider only returning a value if they are all the same
			for (String anonId : anonIds.values())
			{
				id = anonId;
				break;
			}
		}
		else
		{
			String aid = anonIds.get(optionalSectionEid);
			if (aid != null)
			{
				id = aid;
			}
		}

		return id;
	}

	@Override
	public String getStatus()
	{
		return rec.getStatus();
	}

	@Override
	public User getUser()
	{
		return rec.getUser();
	}

	@Override
	public Role getRole()
	{
		return rec.getRole();
	}

	@Override
	public LearningContext getLearningContext()
	{
		return rec.getLearningContext();
	}
}
