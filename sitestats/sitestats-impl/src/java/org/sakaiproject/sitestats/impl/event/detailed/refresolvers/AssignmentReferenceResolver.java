package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Resolves assignment references into meaningful details indicating what has changed
 *
 * @author bbailla2
 */
public class AssignmentReferenceResolver
{
	private static final Log LOG = LogFactory.getLog(AssignmentReferenceResolver.class);

	// Event types handled by this resolver
	private static final String ASN_SUBMIT_SUBMISSION = "asn.submit.submission";
	private static final String ASN_GRADE_SUBMISSION = "asn.grade.submission";
	private static final String ASN_NEW_ASSIGNMENT = "asn.new.assignment";
	private static final String ASN_NEW_ASSIGNMENTCONTENT = "asn.new.assignmentcontent";
	private static final String ASN_READ_ASSIGNMENT = "asn.read.assignment";
	private static final String ASN_READ_SUBMISSION = "asn.read.submission";
	private static final String ASN_REVISE_ACCESS = "asn.revise.access";
	private static final String ASN_REVISE_ASSIGNMENT = "asn.revise.assignment";
	private static final String ASN_REVISE_ASSIGNMENTCONTENT = "asn.revise.assignmentcontent";
	private static final String ASN_REVISE_CLOSEDATE = "asn.revise.closedate";
	private static final String ASN_REVISE_DUEDATE = "asn.revise.duedate";
	private static final String ASN_REVISE_OPENDATE = "asn.revise.opendate";
	private static final String ASN_REVISE_TITLE = "asn.revise.title";
	private static final String ASN_SAVE_SUBMISSION = "asn.save.submission";
	private static final String ASN_DELETE_SUBMISSION = "asn.delete.submission";

	// All of the above as a list
	public static final List<String> ASSIGNMENT_RESOLVABLE_EVENTS = Arrays.asList(ASN_SUBMIT_SUBMISSION, ASN_GRADE_SUBMISSION, ASN_NEW_ASSIGNMENT, ASN_NEW_ASSIGNMENTCONTENT, ASN_READ_ASSIGNMENT, ASN_READ_SUBMISSION, ASN_REVISE_ACCESS, ASN_REVISE_ASSIGNMENT, ASN_REVISE_ASSIGNMENTCONTENT, ASN_REVISE_CLOSEDATE, ASN_REVISE_DUEDATE, ASN_REVISE_OPENDATE, ASN_REVISE_TITLE, ASN_SAVE_SUBMISSION, ASN_SUBMIT_SUBMISSION, ASN_DELETE_SUBMISSION);

	// Assignments permissions that we're requiring to resolve references
	// OWLTODO: this is probably not ideal, as for some events having the read permission is probably good enough, etc
	// OWLTODO: I wonder what permissions the asn service actually checks? site.update perhaps?
	// OWLTODO: Revisit and see if we can have the service handle this because we really shouldn't be keeping permission checks for other tools here
	private static final String READ_PERM = "asn.read";
	private static final String GRADE_PERM = "asn.grade";
	public static final List<String> REQUIRED_PERMS = Arrays.asList(READ_PERM, GRADE_PERM);

	/**
	 * Resolves an event reference and returns a list of key-value pairs representing meaningful details about what has changed
	 * @param eventType
	 * @param ref
	 * @param asnServ
	 * @param siteServ
	 * @param userDirServ
	 * @param statsAuthz
	 * @param siteId
	 * @return
	 */
	public static List<ResolvedRef> resolveReference(String eventType, String ref, AssignmentService asnServ,
			SiteService siteServ, UserDirectoryService userDirServ, StatsAuthz statsAuthz, String siteId)
	{
		/*
		 * Our goal is to return a List with a capacity that matches exactly the amount of data we are returning.
		 * But firstly, we'll set our return value - eventDetails - to Collections.emptyList(), this ensures we're returning *something*.
		 * Collections.emptyList() is constant (doesn't insantiate anything), and it's immutable.
		 */
		List<ResolvedRef> eventDetails = Collections.emptyList();

		if (ASN_DELETE_SUBMISSION.equals(eventType))
		{
			// Takes an assignment submission ref, but submission doesn't exist
			String[] tokens = ref.split("/");
			if (tokens.length == 6)
			{
				// Could be looking at a student, anonymity is important
				return RefResolverUtils.getSingletonList("Assignment Title", getAssignmentTitleById(tokens[4], asnServ, true));
			}
		}
		else if (ASN_NEW_ASSIGNMENT.equals(eventType) || ASN_REVISE_ACCESS.equals(eventType) || ASN_REVISE_ASSIGNMENT.equals(eventType) || ASN_REVISE_CLOSEDATE.equals(eventType) || ASN_REVISE_DUEDATE.equals(eventType) || ASN_REVISE_OPENDATE.equals(eventType) || ASN_REVISE_TITLE.equals(eventType))
		{
			// These all take assignment refs
			if (ref != null)
			{
				String[] tokens = ref.split("/");
				if (tokens.length == 5)
				{
					// All instructor events / none relate to grading events; anonymity doesn't matter
					return RefResolverUtils.getSingletonList("Assignment Title", getAssignmentTitleById(tokens[4], asnServ, false));
				}
			}
			else if (ASN_REVISE_ACCESS.equals(eventType))
			{
				// ref is null
				return RefResolverUtils.getSingletonList("Revised For", "Entire site");
			}
			// asn.revise.title can have a null ref, but the "Error: Unable to find details for this event" message is appropriate
		}
		else if (ASN_NEW_ASSIGNMENTCONTENT.equals(eventType) || ASN_REVISE_ASSIGNMENTCONTENT.equals(eventType))
		{
			// Takes an assignmentcontent ref
			String[] tokens = ref.split("/");
			if (tokens.length == 5)
			{
				AssignmentContent ac = getAssignmentContentByRef(ref, asnServ);
				String title = null;
				if (ac != null)
				{
					String assignmentId = ac.getContext();
					if (assignmentId != null)
					{
						title = getAssignmentTitleById(assignmentId, asnServ, false);
					}

					if (title == null)
					{
						title = ac.getTitle();
					}
				}
				return RefResolverUtils.getSingletonList("Assignment Title", title);
			}
		}
		else if (ASN_GRADE_SUBMISSION.equals(eventType) || ASN_READ_SUBMISSION.equals(eventType) || ASN_SAVE_SUBMISSION.equals(eventType) || ASN_SUBMIT_SUBMISSION.equals(eventType))
		{
			String[] tokens = ref.split("/");
			if (tokens.length == 6)
			{
				// Capacity of eventDetails collection
				int capacity = 0;

				String refSiteId = tokens[3];
				String assignmentId = tokens[4];

				boolean isGroup = false;

				// possible return data
				String title = null;
				String submitterDetails = null;

				Assignment a = getAssignmentById(assignmentId, asnServ);
				if (a != null)
				{
					boolean anonymous = asnServ.assignmentUsesAnonymousGrading(a);
					isGroup = a.isGroup();
					title = anonymous ? "Anonymous Assignment" : a.getTitle();
					if (title != null)
					{
						capacity++;
					}

					if (!anonymous)
					{
						AssignmentSubmission s = getSubmissionByRef(ref, asnServ);
						if (s != null)
						{
							String submitterId = s.getSubmitterId();
							if (submitterId != null)
							{
								if (isGroup)
								{
									// get group title
									Site site = RefResolverUtils.getSiteByID(refSiteId, siteServ, LOG);
									if (site != null)
									{
										Group g = site.getGroup(submitterId);
										if (g != null)
										{
											submitterDetails = g.getTitle();
										}
									}
								}
								else
								{
									boolean displayStudent = false;
									if (ASN_GRADE_SUBMISSION.equals(eventType) || ASN_READ_SUBMISSION.equals(eventType))
									{
										displayStudent = true;
									}
									else
									{
										// save / submit; display the student if this event is an instructor doing something on a student's behalf
										ResourceProperties props = s.getProperties();
										if (props != null)
										{
											// The submitter_user_id property is set if it's to be submitted on a student's behalf.
											// This property represents the instructor (who generated the event), so we're detecting if it exists to determine if we should show the student
											String submitterUserId = props.getProperty((AssignmentSubmission.SUBMITTER_USER_ID));
											displayStudent = submitterUserId != null;
										}
									}
									if (displayStudent)
									{
										// get student display name
										User user = RefResolverUtils.getUserByID(submitterId, userDirServ, LOG);
										if (user != null)
										{
											submitterDetails = user.getDisplayName();
										}
									}
								}

								if (submitterDetails != null)
								{
									capacity++;
								}
							}
						}
					}
				}

				if (capacity > 0)
				{
					eventDetails = new ArrayList<>(capacity);
					RefResolverUtils.addEventDetailsText(eventDetails, "Assignment Title", title);
					RefResolverUtils.addEventDetailsText(eventDetails, isGroup ? "Group" : "Student", submitterDetails);
				}
			}
		}

		return eventDetails;
	}

	private static Assignment getAssignmentById(String assignmentId, AssignmentService asnServ)
	{
		try
		{
			return asnServ.getAssignment(assignmentId);
		}
		catch (IdUnusedException | PermissionException iue)
		{
			// OWLTODO: log something here
		}

		return null;
	}

	private static AssignmentSubmission getSubmissionByRef(String submissionRef, AssignmentService asnServ)
	{
		try
		{
			return asnServ.getSubmission(submissionRef);
		}
		catch (IdUnusedException | PermissionException iue)
		{
			// OWLTODO: log something here
		}

		return null;
	}

	private static String getAssignmentTitleById(String assignmentId, AssignmentService asnServ, boolean hideIfAnonymous)
	{
		Assignment a = getAssignmentById(assignmentId, asnServ);
		if (a == null)
		{
			return null;
		}
		if (hideIfAnonymous && asnServ.assignmentUsesAnonymousGrading(a))
		{
			return "Anonymous Assignment";
		}
		return a.getTitle();
	}

	private static AssignmentContent getAssignmentContentByRef(String assignmentContentRef, AssignmentService asnServ)
	{
		try
		{
			return asnServ.getAssignmentContent(assignmentContentRef);
		}
		catch (IdUnusedException | PermissionException iue)
		{
			// OWLTODO: log something here
		}

		return null;
	}
}
