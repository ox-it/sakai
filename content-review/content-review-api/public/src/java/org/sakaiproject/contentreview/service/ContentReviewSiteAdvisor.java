package org.sakaiproject.contentreview.service;

import java.util.Date;
import org.sakaiproject.site.api.Site;

public interface ContentReviewSiteAdvisor {

	public boolean siteCanUseReviewService(Site site);
	
	public boolean siteCanUseLTIReviewService(Site site);

	/**
	 * Returns true if the TII LTI review service should be used, given the
	 * assignment creation date. This is a transitional method that should be removed
	 * once TII legacy api support ends.
	 * @param site
	 * @param assignmentCreationDate
	 * @return 
	 */
	@Deprecated
	public boolean siteCanUseLTIReviewServiceForAssignment(Site site, Date assignmentCreationDate);

	public boolean siteCanUseLTIDirectSubmission(Site site);
}
