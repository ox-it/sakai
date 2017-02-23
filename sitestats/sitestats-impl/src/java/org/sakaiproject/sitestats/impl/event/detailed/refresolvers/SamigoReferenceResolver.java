package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

public class SamigoReferenceResolver
{
	private static final Log LOG = LogFactory.getLog(SamigoReferenceResolver.class);

	// Student events
	private static final String SAM_ASSESSMENT_TAKE = "sam.assessment.take";
	private static final String SAM_ASSESSMENT_TAKE_VIA_URL = "sam.assessment.take.via_url";
	private static final String SAM_ASSESSMENT_RESUME = "sam.assessment.resume";
	private static final String SAM_ASSESSMENT_SUBMIT = "sam.assessment.submit";
	private static final String SAM_ASSESSMENT_SUBMIT_CHECKED = "sam.assessment.submit.checked";
	private static final String SAM_ASSESSMENT_SUBMIT_CLICK_SUB = "sam.assessment.submit.click_sub";
	private static final String SAM_ASSESSMENT_SUBMIT_VIA_URL = "sam.assessment.submit.via_url";
	private static final String SAM_ASSESSMENT_REVIEW = "sam.assessment.review";
	private static final String SAM_ASSESSMENT_THREAD_SUBMIT = "sam.assessment.thread_submit";
	private static final String SAM_ASSESSMENT_TIMER_SUBMIT = "sam.assessment.timer_submit";
	private static final String SAM_ASSESSMENT_TIMER_SUBMIT_URL = "sam.assessment.timer_submit.url";
	private static final String SAM_ASSESSMENTSUBMITTED = "sam.assessmentSubmitted";
	private static final String SAM_ASSESSMENTTIMEDSUBMITTED = "sam.assessmentTimedSubmitted";
	private static final String SAM_SUBMIT_FROM_LAST_PAGE = "sam.submit.from_last_page";
	private static final String SAM_SUBMIT_FROM_TOC = "sam.submit.from_toc";

	// Instructor events
	private static final String SAM_ASESSMENT_REVISE = "sam.asessment.revise"; // double checking your string literals is key!
	//private static final String SAM_ASSESSMENT_REVISE = "sam.assessment.revise";
	private static final String SAM_ASSESSMENT_CREATE = "sam.assessment.create";
	//private static final String SAM_ASSESSMENT_ITEM_DELETE = "sam.assessment.item.delete";
	private static final String SAM_ASSESSMENT_PUBLISH = "sam.assessment.publish";
	private static final String SAM_ASSESSMENT_REMOVE = "sam.assessment.remove";
	//private static final String SAM_PUBASSESSMENT_REMOVE = "sam.pubAssessment.remove";
	//private static final String SAM_PUBSETTING_EDIT = "sam.pubSetting.edit";
	//private static final String SAM_PUBSETTING_EDIT2 = "sam.pubsetting.edit"; // consistency is key!
	//private static final String SAM_PUBASSESSMENT_CONFIRM_EDIT = "sam.pubassessment.confirm_edit";
	//private static final String SAM_PUBASSESSMENT_REPUBLISH = "sam.pubassessment.republish";
	//private static final String SAM_PUBASSESSMENT_REVISE = "sam.pubassessment.revise";
	//private static final String SAM_QUESTION_SCORE_UPDATE = "sam.question.score.update";
	//private static final String SAM_QUESTIONPOOL_DELETE = "sam.questionpool.delete";
	//private static final String SAM_QUESTIONPOOL_DELETEITEM = "sam.questionpool.deleteitem";
	//private static final String SAM_QUESTIONPOOL_QUESTIONMOVED = "sam.questionpool.questionmoved";
	//private static final String SAM_QUESTIONPOOL_TRANSFER = "sam.questionpool.transfer";
	//private static final String SAM_QUESTIONPOOL_UNSHARE = "sam.questionpool.unshare";
	//private static final String SAM_SETTING_EDIT = "sam.setting.edit";
	//private static final String SAM_STUDENT_SCORE_UPDATE = "sam.student.score.update";
	//private static final String SAM_TOTAL_SCORE_UPDATE = "sam.total.score.update";


	public static final List<String> SAMIGO_RESOLVABLE_EVENTS = Arrays.asList(
		SAM_ASSESSMENT_TAKE, SAM_ASSESSMENT_TAKE_VIA_URL, SAM_ASSESSMENT_RESUME,
		SAM_ASSESSMENT_SUBMIT, SAM_ASSESSMENT_SUBMIT_CHECKED, SAM_ASSESSMENT_SUBMIT_CLICK_SUB,
		SAM_ASSESSMENT_SUBMIT_VIA_URL, SAM_ASSESSMENT_REVIEW, SAM_ASSESSMENT_THREAD_SUBMIT,
		SAM_ASSESSMENT_TIMER_SUBMIT, SAM_ASSESSMENT_TIMER_SUBMIT_URL, SAM_ASSESSMENTSUBMITTED,
		SAM_ASSESSMENTTIMEDSUBMITTED, SAM_SUBMIT_FROM_LAST_PAGE, SAM_SUBMIT_FROM_TOC,

		SAM_ASESSMENT_REVISE, /*SAM_ASSESSMENT_REVISE,*/ SAM_ASSESSMENT_CREATE,
		/*SAM_ASSESSMENT_ITEM_DELETE,*/ SAM_ASSESSMENT_PUBLISH, SAM_ASSESSMENT_REMOVE /*, */
		/*SAM_PUBASSESSMENT_REMOVE, SAM_PUBSETTING_EDIT, SAM_PUBSETTING_EDIT2, */
		/*SAM_PUBASSESSMENT_CONFIRM_EDIT, SAM_PUBASSESSMENT_REPUBLISH, SAM_PUBASSESSMENT_REVISE, */
		/*SAM_QUESTION_SCORE_UPDATE, SAM_QUESTIONPOOL_DELETE, SAM_QUESTIONPOOL_DELETEITEM, */
		/*SAM_QUESTIONPOOL_QUESTIONMOVED, SAM_QUESTIONPOOL_TRANSFER, SAM_QUESTIONPOOL_UNSHARE, */
		/*SAM_SETTING_EDIT, SAM_STUDENT_SCORE_UPDATE, SAM_TOTAL_SCORE_UPDATE */
	);

	// Samigo permissions that we're requiring to resolve references
	private static final String GRADE_ANY_PERM = "assessment.gradeAssessment.any";
	public static final List<String> REQUIRED_PERMS = Arrays.asList(GRADE_ANY_PERM);

	private static final String REGEX_ASSESSMENT_ID = "assessmentId=(\\d+)";
	private static final String REGEX_ITEM_ID = "itemId=(\\d+)";
	private static final String REGEX_PUBLISHED_ASSESSMENT_ID_1 = "publishedAssessmentId=(\\d+)";
	private static final String REGEX_PUBLISHED_ASSESSMENT_ID_2 = "publishedAssessmentID=(\\d+)";
	private static final String REGEX_PUBLISHED_ASSESSMENT_ID_3 = "publishedAssessmentId(\\d+)";
	private static final String REGEX_SECTION_ID = "sectionId=(\\d+)";
	private static final String REGEX_SUBMISSION_ID = "submissionId=(\\d+)";

	public static List<ResolvedRef> resolveReference(String eventType, String ref)
	{
		List<ResolvedRef> eventDetails = Collections.emptyList();

		if (SAM_ASSESSMENT_TAKE.equals(eventType) || SAM_ASSESSMENT_TAKE_VIA_URL.equals(eventType) || SAM_ASSESSMENT_RESUME.equals(eventType) || SAM_ASSESSMENT_REVIEW.equals(eventType))
		{
			// Show quiz title, no need to entity link to quiz
			String assessmentId = matchRegex(ref, REGEX_PUBLISHED_ASSESSMENT_ID_1);
			if (assessmentId != null)
			{
				String title = getTitleForPubAssessmentId(assessmentId);
				if (title != null)
				{
					return Collections.singletonList(ResolvedRef.newText("Assessment", title));
				}
			}
			else
			{
				assessmentId = matchRegex(ref, REGEX_PUBLISHED_ASSESSMENT_ID_3);
				if (assessmentId != null)
				{
					String title = getTitleForPubAssessmentId(assessmentId);
					if (title != null)
					{
						return Collections.singletonList(ResolvedRef.newText("Assessment", title));
					}
				}
			}

		}
		else if (SAM_ASSESSMENT_SUBMIT.equals(eventType) || SAM_ASSESSMENT_SUBMIT_CHECKED.equals(eventType) || SAM_ASSESSMENT_SUBMIT_CLICK_SUB.equals(eventType) || SAM_ASSESSMENT_SUBMIT_VIA_URL.equals(eventType) || SAM_ASSESSMENT_THREAD_SUBMIT.equals(eventType) || SAM_ASSESSMENT_TIMER_SUBMIT.equals(eventType) || SAM_ASSESSMENT_TIMER_SUBMIT_URL.equals(eventType) || SAM_SUBMIT_FROM_LAST_PAGE.equals(eventType) || SAM_SUBMIT_FROM_TOC.equals(eventType))
		{
			String submissionId = matchRegex(ref, REGEX_SUBMISSION_ID);
			if (submissionId != null)
			{
				//submissionId is the assessmentGradingId
				AssessmentGradingData agd = getAssessmentGradingData(submissionId);
				if (agd != null)
				{
					Long assessmentId = agd.getPublishedAssessmentId();
					if (assessmentId != null)
					{
						String title = getTitleForPubAssessmentId(String.valueOf(assessmentId));
						if (title != null)
						{
							return Collections.singletonList(ResolvedRef.newText("Assessment", title));
						}
					}
				}
			}
		}
		else if (SAM_ASSESSMENTSUBMITTED.equals(eventType) || SAM_ASSESSMENTTIMEDSUBMITTED.equals(eventType))
		{
			String assessmentId = matchRegex(ref, REGEX_PUBLISHED_ASSESSMENT_ID_2);
			if (assessmentId != null)
			{
				String title = getTitleForPubAssessmentId(assessmentId);
				if (title != null)
				{
					return Collections.singletonList(ResolvedRef.newText("Assessment", title));
				}
			}
		}
		else if (SAM_ASESSMENT_REVISE.equals(eventType) || SAM_ASSESSMENT_CREATE.equals(eventType) || SAM_ASSESSMENT_PUBLISH.equals(eventType) || SAM_ASSESSMENT_REMOVE.equals(eventType))
		{
			String assessmentId = matchRegex(ref, REGEX_ASSESSMENT_ID);
			if (assessmentId != null)
			{
				String title = getTitleForAssessmentId(assessmentId);
				if (title != null)
				{
					return Collections.singletonList(ResolvedRef.newText("Assessment", title));
				}
			}
		}
		// OWLTODO: re-add these when we can test the question number appearance
		/*else if (SAM_ASSESSMENT_ITEM_DELETE.equals(eventType))
		{
			int capacity = 0;
			String assessmentTitle = null;
			String sectionTitle = null;
			Integer questionNumber = null;

			String assessmentId = matchRegex(ref, REGEX_ASSESSMENT_ID);
			if (assessmentId != null)
			{
				assessmentTitle = getTitleForAssessmentId(assessmentId);
			}

			String itemId = matchRegex(ref, REGEX_ITEM_ID);
			if (itemId != null)
			{
				ItemFacade item = getItemFacade(itemId);
				if (item != null)
				{
					SectionDataIfc section = item.getSection();
					if (section != null)
					{
						sectionTitle = section.getTitle();
					}

					questionNumber = item.getSequence();
				}
			}

			capacity = (assessmentTitle == null ? 0 : 1) + (sectionTitle == null ? 0 : 1) + (questionNumber == null ? 0 : 1);
			eventDetails = new ArrayList<ResolvedRef>(capacity);
			if (assessmentTitle != null)
			{
				eventDetails.add(ResolvedRef.newText("Assessment", assessmentTitle));
			}
			if (sectionTitle != null)
			{
				eventDetails.add(ResolvedRef.newText("Section", sectionTitle));
			}
			if (questionNumber != null)
			{
				// OWLTODO verify; I think the +1 is required, but not sure
				eventDetails.add(ResolvedRef.newText("Question", String.valueOf(questionNumber + 1)));
			}
		}
		else if (SAM_ASSESSMENT_REVISE.equals(eventType))
		{
			// look for itemId and work up; if itemId not present, look for sectionId and work up
			int capacity = 0;
			String assessmentTitle = null;
			String sectionTitle = null;
			Integer questionNumber = null;

			String itemId = matchRegex(ref, REGEX_ASSESSMENT_ID);
			if (itemId == null)
			{
				String sectionId = matchRegex(ref, REGEX_SECTION_ID);
				if (sectionId != null)
				{
					SectionFacade section = getSection(sectionId);
					if (section != null)
					{
						sectionTitle = section.getTitle();
						AssessmentIfc assessment = section.getAssessment();
						if (assessment != null)
						{
							assessmentTitle = assessment.getTitle();
						}
					}
				}
			}
			else
			{
				ItemFacade item = getItemFacade(itemId);
				if (item != null)
				{
					SectionDataIfc section = item.getSection();
					if (section != null)
					{
						sectionTitle = section.getTitle();
						AssessmentIfc assessment = section.getAssessment();
						if (assessment != null)
						{
							assessmentTitle = assessment.getTitle();
						}
					}

					questionNumber = item.getSequence();
				}
			}

			capacity = (assessmentTitle == null ? 0 : 1) + (sectionTitle == null ? 0 : 1) + (questionNumber == null ? 0 : 1);
			eventDetails = new ArrayList(capacity);
			if (assessmentTitle != null)
			{
				eventDetails.add(ResolvedRef.newText("Assessment", assessmentTitle));
			}
			if (sectionTitle != null)
			{
				eventDetails.add(ResolvedRef.newText("Section", sectionTitle));
			}
			if (questionNumber != null)
			{
				// OWLTODO: verify; I think the +1 is required, but not sure
				eventDetails.add(ResolvedRef.newText("Question", String.valueOf(questionNumber + 1)));
			}
		}*/
		return eventDetails;
	}

	private static String getTitleForPubAssessmentId(String assessmentId)
	{
		PublishedAssessmentFacade paf = getPublishedAssessmentFacade(assessmentId);
		if (paf != null)
		{
			EvaluationModelIfc evalModel = paf.getEvaluationModel();
			if (evalModel != null)
			{
				Integer anonGrading = evalModel.getAnonymousGrading();
				if (anonGrading == 1)
				{
					return "Anonymous Assessment";
				}
			}

			return paf.getTitle();
		}

		return null;
	}

	private static AssessmentGradingData getAssessmentGradingData(String assessmentGradingId)
	{
		try
		{
			GradingService gs = new GradingService();
			return gs.load(assessmentGradingId);
		}
		catch (RuntimeException e)
		{
			// Do nothing
		}

		return null;
	}

	private static String getTitleForAssessmentId(String assessmentId)
	{
		AssessmentFacade af = getAssessmentFacade(assessmentId);
		if (af != null)
		{
			return af.getTitle();
		}

		return null;
	}

	private static AssessmentFacade getAssessmentFacade(String assessmentId)
	{
		try
		{
			AssessmentService as = new AssessmentService();
			return as.getAssessment(assessmentId);
		}
		catch (RuntimeException e)
		{
			// Do nothing
		}

		return null;
	}

	private static SectionFacade getSection(String sectionId)
	{
		try
		{
			AssessmentService as = new AssessmentService();
			return as.getSection(sectionId);
		}
		catch (RuntimeException e)
		{
			// Do nothing
		}

		return null;
	}

	private static ItemFacade getItemFacade(String itemId)
	{
		try
		{
			ItemService is = new ItemService();
			return is.getItem(itemId);
		}
		catch (RuntimeException e)
		{
			// Do nothing
		}

		return null;
	}

	private static PublishedAssessmentFacade getPublishedAssessmentFacade(String assessmentId)
	{
		try
		{
			PublishedAssessmentService pas = new PublishedAssessmentService();
			return pas.getPublishedAssessment(assessmentId);
		}
		catch (RuntimeException e)
		{
			// Do nothing
		}

		return null;
	}

	private static String matchRegex(String text, String regex)
	{
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		if (m.find())
		{
			return m.group(1);
		}

		return null;
	}
}
