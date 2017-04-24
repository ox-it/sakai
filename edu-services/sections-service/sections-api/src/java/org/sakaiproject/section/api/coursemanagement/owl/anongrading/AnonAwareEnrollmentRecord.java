package org.sakaiproject.section.api.coursemanagement.owl.anongrading;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;

/**
 *
 * @author plukasew
 */
public interface AnonAwareEnrollmentRecord extends EnrollmentRecord
{
	public String getAnonId(String optionalSectionEid);
}
