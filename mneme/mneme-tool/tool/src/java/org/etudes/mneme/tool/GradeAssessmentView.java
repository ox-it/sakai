/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.tool;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Paging;
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /grading view for the mneme tool.
 */
public class GradeAssessmentView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(GradeAssessmentView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

	/** Configuration: the page sizes for the view. */
	protected List<Integer> pageSizes = new ArrayList<Integer>();

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService serverConfigurationService = null;

	/** The SiteService. */
	protected SiteService siteService = null;

	/** Submission Service */
	protected SubmissionService submissionService = null;

	/** Dependency: ToolManager */
	protected ToolManager toolManager = null;

	/** UserDirectoryService */
	protected UserDirectoryService userDirectoryService = null;

	protected SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm aa");

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2]sort for /grades, [3]aid |optional->| [4]our sort, [5]our page, [6]our highest/all-for-uid, [7] section filter
		if ((params.length < 4) || params.length > 8) throw new IllegalArgumentException();

		// check for user permission to access the assessments for grading
		if (!this.submissionService.allowEvaluate(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// grades sort parameter
		String gradesSortCode = params[2];
		context.put("sort_grades", gradesSortCode);

		// get Assessment
		Assessment assessment = this.assessmentService.getAssessment(params[3]);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check that the assessment is not a formal course evaluation
		if (assessment.getFormalCourseEval())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// nor a survey
		if (assessment.getType() == AssessmentType.survey)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// validity check
		if (!assessment.getIsValid())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		context.put("assessment", assessment);

		// sort parameter
		String sortCode = null;
		if (params.length > 4) sortCode = params[4];
		SubmissionService.FindAssessmentSubmissionsSort sort = getSort(assessment, context, sortCode);
		context.put("sort", sort.toString());

		// paging parameter
		String pagingParameter = null;
		if (params.length > 5) pagingParameter = params[5];
		if (pagingParameter == null)
		{
			pagingParameter = "1-" + Integer.toString(this.pageSizes.get(0));
		}

		Boolean official = Boolean.FALSE;
		String allUid = null;
		if (params.length > 6)
		{
			if ("official".equals(params[6])) official = Boolean.TRUE;
		}

		// for anon, ignore official
		if (assessment.getAnonymous())
		{
			official = Boolean.FALSE;
			allUid = null;
		}

		// section filter
		String sectionFilter = null;
		if (params.length > 7)
		{
			sectionFilter = params[7];
			if ("-".equals(sectionFilter)) sectionFilter = null;
		}
		context.put("sectionFilter", sectionFilter == null ? "-" : sectionFilter);

		// view highest only decision (boolean string)
		Value highest = this.uiService.newValue();
		highest.setValue(Boolean.toString(official));
		context.put("highest", highest);

		// view option (official or all)
		if (official.booleanValue())
		{
			context.put("view", "official");
		}
		else
		{
			context.put("view", "all");
		}

		// get the size
		Integer maxSubmissions = this.submissionService.countAssessmentSubmissions(assessment, official, allUid, sectionFilter);

		// paging
		Paging paging = uiService.newPaging();
		paging.setMaxItems(maxSubmissions);
		paging.setCurrentAndSize(pagingParameter);
		context.put("paging", paging);

		// get all Assessment submissions
		List<Submission> submissions = this.submissionService.findAssessmentSubmissions(assessment, sort, official, allUid,
				paging.getSize() == 0 ? null : paging.getCurrent(), paging.getSize() == 0 ? null : paging.getSize(), null, sectionFilter);
		context.put("submissions", submissions);

		// pages sizes
		if (this.pageSizes.size() > 1)
		{
			context.put("pageSizes", this.pageSizes);
		}
		new CKSetup().setCKCollectionAttrib(getDocsPath(), toolManager.getCurrentPlacement().getContext());

		// the sections for the site
		List<String> sections = siteSections(assessment.getContext());
		context.put("sections", sections);

		// for the selected section (the destination string at page 0)
		// [2]sort for /grades, [3]aid |optional->| [4]our sort, [5]our page, [6]our highest/all-for-uid, [7] section filter
		String[] dest = new String[8];
		for (int i = 0; i < params.length; i++)
		{
			dest[i] = params[i];
		}
		// fill in missing sort
		if (dest[4] == null)
		{
			dest[4] = "0A";
		}
		// fill in missing paging
		if (dest[5] == null)
		{
			dest[5] = "1-" + paging.getSize().toString();
		}
		// set the official / all
		if ("true".equals(highest.getValue()))
		{
			dest[6] = "official";
		}
		else
		{
			dest[6] = "all";
		}
		if (dest[7] == null)
		{
			dest[7] = "-";
		}
		String newDestination = StringUtil.unsplit(dest, "/");
		Value value = this.uiService.newValue();
		value.setValue(newDestination);
		context.put("selectedSection", value);

		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		String pageSize = StringUtil.trimToNull(this.serverConfigurationService.getString("pageSize@org.etudes.mneme.tool.GradeAssessmentView"));
		if (pageSize != null) setPageSize(pageSize);

		if (this.pageSizes.isEmpty())
		{
			this.pageSizes.add(Integer.valueOf(50));
			this.pageSizes.add(Integer.valueOf(100));
			this.pageSizes.add(Integer.valueOf(0));
		}

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2]sort for /grades, [3]aid |optional->| [4]our sort, [5]our page, [6]our all/highest, [7] section filter
		if ((params.length < 4) || params.length > 8) throw new IllegalArgumentException();

		String siteId = toolManager.getCurrentPlacement().getContext();

		// check for user permission to access the assessments for grading
		if (!this.submissionService.allowEvaluate(siteId))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		Site site = null;
		try
		{
			site = this.siteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		//String iidCode = this.rosterService.findInstitutionCode(site.getTitle());
		String iidCode = null;

		// for Adjust every student's test submission by
		Value submissionAdjustValue = this.uiService.newValue();
		context.put("submissionAdjust", submissionAdjustValue);

		// for Adjust every student's test submission by
		Value deductLateValue = this.uiService.newValue();
		context.put("lateSubmissionDeduct", deductLateValue);


		// for "Adjust every student's test submission by" comments
		Value submissionAdjustCommentsValue = this.uiService.newValue();
		context.put("submissionAdjustComments", submissionAdjustCommentsValue);

		// setup the model: the assessment
		// get Assessment - assessment id is in params at index 3
		Assessment assessment = this.assessmentService.getAssessment(params[3]);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// for the final scores
		PopulatingSet submissions = null;
		final SubmissionService submissionService = this.submissionService;
		submissions = uiService.newPopulatingSet(new Factory()
		{
			public Object get(String id)
			{
				Submission submission = submissionService.getSubmission(id);
				return submission;
			}
		}, new Id()
		{
			public String getId(Object o)
			{
				return ((Submission) o).getId();
			}
		});
		context.put("submissions", submissions);

		// view highest boolean holder
		Value highest = this.uiService.newValue();
		context.put("highest", highest);

		// a CSV uploader for the CSV file
		UploadCsv upload = new UploadCsv();
		context.put("upload", upload);

		// read form
		String destination = this.uiService.decode(req, context);

		// save any final scores
		for (Iterator i = submissions.getSet().iterator(); i.hasNext();)
		{
			try
			{
				this.submissionService.evaluateSubmission((Submission) i.next());
			}
			catch (AssessmentPermissionException e)
			{
				M_log.warn("post: " + e);
			}
		}

		// get a valid global score adjustment (must not be more than 2x the assessment's points)
		Float score = null;
		String adjustScore = StringUtil.trimToNull(submissionAdjustValue.getValue());
		if (adjustScore != null)
		{
			Float totalPoints = assessment.getPoints();
			if (totalPoints != null)
			{
				try
				{
					score = Float.parseFloat(adjustScore);
					if (Math.abs(score) > (totalPoints.floatValue() * 2f))
					{
						M_log.warn("post: adjustScore out of range: score: " + score + " assessment points: " + totalPoints + " assessment id: "
								+ assessment.getId());
						score = null;
					}
				}
				catch (NumberFormatException e)
				{
					score = null;
				}
			}
			else
			{
				M_log.warn("post: adjustScore against an assessment with no points: assessment id: " + assessment.getId());
			}
		}

		// the global comment adjustment
		String adjustComments = StringUtil.trimToNull(submissionAdjustCommentsValue.getValue());

		// apply the global adjustments
		if ((score != null) || (adjustComments != null))
		{
			try
			{
				this.submissionService.evaluateSubmissions(assessment, adjustComments, score);
			}
			catch (AssessmentPermissionException e)
			{
				M_log.warn("post: " + e);
			}
		}

		// get a valid global penalty adjustment )
		Float deductValue = null;
		String deductScore = StringUtil.trimToNull(deductLateValue.getValue());
		if (deductScore != null)
		{
			Float totalPoints = assessment.getPoints();
			if (totalPoints != null)
			{
				try
				{
					deductValue = Float.parseFloat(deductScore);
				}
				catch (NumberFormatException e)
				{
					deductValue = null;
				}
			}
			else
			{
				M_log.warn("post: deductValue against an assessment with no points: assessment id: " + assessment.getId());
			}
		}
		
		// apply the global adjustments
		if (deductValue != null)
		{
			try
			{
				this.submissionService.deductLateSubmissions(assessment, deductValue);
			}
			catch (AssessmentPermissionException e)
			{
				M_log.warn("post: " + e);
			}
		}

		// release all evaluated
		if (destination.equals("RELEASEEVALUATED"))
		{
			try
			{
				this.submissionService.releaseSubmissions(assessment, Boolean.TRUE);
			}
			catch (AssessmentPermissionException e)
			{
				M_log.warn("post: " + e);
			}

			destination = context.getDestination();
		}

		else if (destination.equals("RELEASEALL"))
		{
			try
			{
				this.submissionService.releaseSubmissions(assessment, Boolean.FALSE);
			}
			catch (AssessmentPermissionException e)
			{
				M_log.warn("post: " + e);
			}

			destination = context.getDestination();
		}

		else if (destination.equals("ZEROCLOSED"))
		{
			List<Submission> submissionsToZero = this.submissionService.findAssessmentSubmissions(assessment, null, false, null, null, null, null,
					null);
			for (Submission s : submissionsToZero)
			{
				if (s.getIsPhantom() && !s.getAssessment().getDates().getIsOpen(Boolean.FALSE))
				{
					s.setTotalScore(0f);
					s.getEvaluation().setEvaluated(Boolean.TRUE);
					s.setIsReleased(Boolean.TRUE);
					try
					{
						this.submissionService.evaluateSubmission(s);
					}
					catch (AssessmentPermissionException e)
					{
					}
				}
			}

			destination = context.getDestination();
		}

		else if (destination.equals("SAVE"))
		{
			destination = context.getDestination();
		}

		else if (destination.equals("UPLOAD"))
		{
			GradeImportSet importSet = new GradeImportSet();
			importSet.assessment = assessment;

			List<GradeImportSet> importSets = new ArrayList<GradeImportSet>(1);
			importSets.add(importSet);

			// get the Tool session, where we will be storing this until confirmed
			ToolSession toolSession = m_sessionManager.getCurrentToolSession();
			toolSession.setAttribute(GradeImportSet.ATTR_NAME, importSets);

			List<CSVRecord> records = upload.getRecords();
			if ((records != null) && (records.size() > 1))
			{
				// get the header record
				CSVRecord header = records.get(0);

				// find the "Student ID" column
				int idCol = -1;
				for (int c = 0; c < header.size(); c++)
				{
					// get the assessment title, extras
					String title = StringUtil.trimToZero(header.get(c));
					if ("Student ID".equalsIgnoreCase(title))
					{
						idCol = c;
						break;
					}
				}

				// find the scores column - the one with a title matching the assessment title
				int col = -1;
				for (int c = 0; c < header.size(); c++)
				{
					// get the assessment title, extras
					String title = StringUtil.trimToZero(header.get(c));
					int extraPos = title.indexOf("{{");
					if (extraPos != -1)
					{
						title = StringUtil.trimToZero(title.substring(0, extraPos));
					}

					if (title.equalsIgnoreCase(assessment.getTitle()))
					{
						col = c;
						break;
					}
				}

				if ((col != -1) && (idCol != -1) && (col != idCol))
				{
					// get the assesssment's submissions
					List<Submission> subs = this.submissionService.findAssessmentSubmissions(assessment, null, true, null, null, null, null);

					for (CSVRecord r : records)
					{
						// skip the header
						if (r.getRecordNumber() == 1) continue;

						GradeImport x = new GradeImport();
						importSet.rows.add(x);

						if (r.size() > 0)
						{
							x.studentId = r.get(idCol);
						}
						if (r.size() > col)
						{
							x.scoreGiven = r.get(col);
						}

						if (x.scoreGiven != null)
						{
							try
							{
								x.score = Float.parseFloat(x.scoreGiven);
							}
							catch (NumberFormatException e)
							{
							}
						}

						if ((x.score != null) & (x.studentId != null))
						{
							// use value in paren, if there
							String id = x.studentId;
							int parenPos = id.indexOf("(");
							if (parenPos != -1)
							{
								id = StringUtil.trimToZero(id.substring(parenPos + 1, id.length() - 1));
							}

							User user = findIdentifiedStudentInSite(id, siteId, iidCode);

							if (user != null)
							{
								// check for duplicate records
								boolean duplicate = false;
								for (GradeImport gi : importSet.rows)
								{
									if (gi == x) continue;
									if (gi.userId == null) continue;
									if (gi.userId.equals(user.getId()))
									{
										duplicate = true;
										x.duplicate = Boolean.TRUE;
										break;
									}
								}

								if (!duplicate)
								{
									// find the submission
									Submission forUser = null;
									for (Submission submission : subs)
									{
										if (submission.getUserId().equals(user.getId()))
										{
											forUser = submission;
											break;
										}
									}

									if (forUser != null)
									{
										x.userId = user.getId();
										x.name = user.getDisplayName();
									}
								}
							}
						}
					}
				}
			}

			destination = "/confirm_grades_import" + context.getDestination();
		}

		else if (destination.equals("VIEW"))
		{
			// anon always gets view all
			if (assessment.getAnonymous())
			{
				// just ignore this
				destination = context.getDestination();
			}
			else
			{
				// build the new dest parameters
				String[] dest = new String[8];
				for (int i = 0; i < params.length; i++)
				{
					dest[i] = params[i];
				}

				// fill in missing sort
				if (dest[4] == null)
				{
					dest[4] = "0A";
				}

				// fill in missing paging
				if (dest[5] == null)
				{
					dest[5] = "1-" + Integer.toString(this.pageSizes.get(0));
				}

				// set the official / all
				if ("true".equals(highest.getValue()))
				{
					dest[6] = "official";
				}
				else
				{
					dest[6] = "all";
				}
				if (dest[7] == null)
				{
					dest[7] = "-";
				}

				destination = StringUtil.unsplit(dest, "/");
			}
		}
		
		if (destination.equals("EXPORT")) 
		{
			String fileName = assessment.getTitle().replaceAll(" ", "_")+".csv";
			StringBuffer sb = new StringBuffer();
			
			// Temporarily hardcode this parameter so tries are not displayed
			// there is currently no reliable method to get the number of tries
			boolean showTries = false;
			
			sb.append(this.csvTitles(showTries));
			
			ArrayList<String> csvLines = new ArrayList<String>();
			Iterator iter = submissions.getSet().iterator();
			while (iter.hasNext()) {
				Object object = iter.next();
				if (object instanceof Submission) {
					Submission submission = (Submission)object;
					csvLines.add(toCSV(submission, showTries));
				}
			}
			
			this.sortLines(csvLines);
			this.joinLines(sb, csvLines);
			
			String csvString = sb.toString();
			
			res.setContentType("text/comma-separated-values");
			String disposition = "attachment; fileName="+fileName;
			res.setHeader("Content-Disposition", disposition);
			res.setHeader("Cache-Control", "max-age=0");
			res.setContentLength(csvString.length());
			OutputStream out = null;
			try {
				out = res.getOutputStream();
				out.write(csvString.getBytes());
				out.flush();
				
			} catch(IOException e) {
				e.printStackTrace();
				
			} finally {
				
				try {
					if(out != null) out.close();
					
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			
			destination = context.getDestination();
			
		} else {
			
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
		}
	}

	/**
	 * @param assessmentService
	 *        the assessmentService to set
	 */
	public void setAssessmentService(AssessmentService assessmentService)
	{
		this.assessmentService = assessmentService;
	}

	/**
	 * Set the the page size for the view.
	 * 
	 * @param sizes
	 *        The the page sizes for the view - integers, comma separated.
	 */
	public void setPageSize(String sizes)
	{
		this.pageSizes.clear();
		String[] parts = StringUtil.split(sizes, ",");
		for (String part : parts)
		{
			this.pageSizes.add(Integer.valueOf(part));
		}
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		securityService = service;
	}

	/**
	 * Set the ServerConfigurationService.
	 * 
	 * @param service
	 *        the ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		this.serverConfigurationService = service;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		m_sessionManager = service;
	}

	/**
	 * @param siteService
	 *        the siteService to set
	 */
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	/**
	 * @param submissionService
	 *        the submissionService to set
	 */
	public void setSubmissionService(SubmissionService submissionService)
	{
		this.submissionService = submissionService;
	}

	/**
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	/**
	 * @param userDirectoryService
	 *        the userDirectoryService to set
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	/**
	 * Pick a user uniquely identified by the id in the site.
	 * 
	 * @param id
	 *        The id - can be IID or EID.
	 * @param siteId
	 *        The site id.
	 * @param iidCode
	 *        The site's institution code.
	 * @return The user, or null if not found.
	 */
	protected User findIdentifiedStudentInSite(String id, String siteId, String iidCode)
	{
		User rv = null;

		// first, try by IID
		/*try
		{
			rv = this.userDirectoryService.getUserByIid(iidCode, id);
		}
		catch (UserNotDefinedException e)
		{
		}

		// next, by EID
		if (rv == null)
		{
			List<User> candidates = this.userDirectoryService.getUsersByEid(id);

			// filter down by who is in the site
			List<User> filtered = new ArrayList<User>();
			for (User u : candidates)
			{
				if (this.securityService.checkSecurity(u.getId(), MnemeService.SUBMIT_PERMISSION, siteId))
				{
					filtered.add(u);
				}
			}

			// if just one
			if (filtered.size() == 1)
			{
				rv = filtered.get(0);
			}
		}*/
		
		try
		{
			rv = this.userDirectoryService.getUserByEid(id);
		}
		catch (UserNotDefinedException e)
		{
		}

		// assure in site
		if (rv != null)
		{
			if (!this.securityService.checkSecurity(rv.getId(), MnemeService.SUBMIT_PERMISSION, siteId))
			{
				rv = null;
			}
		}

		return rv;
	}

	/**
	 * get the sort based on sort code
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param context
	 * @param sortCode
	 *        sort code
	 * @return SubmissionService.FindAssessmentSubmissionsSort
	 */
	protected SubmissionService.FindAssessmentSubmissionsSort getSort(Assessment assessment, Context context, String sortCode)
	{
		// default sort is user name ascending
		SubmissionService.FindAssessmentSubmissionsSort sort;
		if (sortCode != null)
		{
			if (sortCode.trim().length() == 2)
			{
				context.put("sort_column", sortCode.charAt(0));
				context.put("sort_direction", sortCode.charAt(1));

				if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'A'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.userName_a;
				else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'D'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.userName_d;
				else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'A'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.status_a;
				else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'D'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.status_d;
				else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'A'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.final_a;
				else if ((sortCode.charAt(0) == '2') && (sortCode.charAt(1) == 'D'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.final_d;
				else if ((sortCode.charAt(0) == '3') && (sortCode.charAt(1) == 'A'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.evaluated_a;
				else if ((sortCode.charAt(0) == '3') && (sortCode.charAt(1) == 'D'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.evaluated_d;
				else if ((sortCode.charAt(0) == '4') && (sortCode.charAt(1) == 'A'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.released_a;
				else if ((sortCode.charAt(0) == '4') && (sortCode.charAt(1) == 'D'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.released_d;
				else if ((sortCode.charAt(0) == '5') && (sortCode.charAt(1) == 'A'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.sdate_a;
				else if ((sortCode.charAt(0) == '5') && (sortCode.charAt(1) == 'D'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.sdate_d;
				else if ((sortCode.charAt(0) == '6') && (sortCode.charAt(1) == 'A'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.section_a;
				else if ((sortCode.charAt(0) == '6') && (sortCode.charAt(1) == 'D'))
					sort = SubmissionService.FindAssessmentSubmissionsSort.section_d;
				else
				{
					throw new IllegalArgumentException();
				}
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}
		else
		{
			// default sort: user name ascending for non anon, sdate asc for anon
			if (assessment.getAnonymous())
			{
				sort = SubmissionService.FindAssessmentSubmissionsSort.sdate_a;
				context.put("sort_column", '5');
				context.put("sort_direction", 'A');
			}
			else
			{
				sort = SubmissionService.FindAssessmentSubmissionsSort.userName_a;
				context.put("sort_column", '0');
				context.put("sort_direction", 'A');
			}
		}

		return sort;
	}

	/**
	 * Create a sorted list of the sections for this site.
	 * 
	 * @param context
	 *        The context (site id).
	 * @return The list of sections.
	 */
	protected List<String> siteSections(String context)
	{
		List<String> rv = new ArrayList<String>();

		try
		{
			Site site = this.siteService.getSite(context);
			Collection groups = site.getGroups();
			for (Object groupO : groups)
			{
				Group g = (Group) groupO;

				// skip non-section groups
				if (g.getProperties().getProperty("sections_category") == null) continue;

				rv.add(g.getTitle());
			}
		}
		catch (IdUnusedException e)
		{
		}

		Collections.sort(rv);

		return rv;
	}

	private void sortLines(ArrayList<String> lines) {
		java.util.Collections.sort(lines, new Comparator<String>() {
			public int compare(String s, String s2) {
				return s.compareToIgnoreCase(s2);
			}
		});
	}

	private void joinLines(StringBuffer sb, ArrayList<String> lines) {
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}
	}

	private String csvTitles(boolean showTries) {
		String titles = "";
		titles += "\"Name\",\"User Name\",";
		titles += showTries ? "\"Tries\"," : "";
		titles += "\"Finished\",\"Auto Score\",\"Final\",\"Evaluated\",\"Released\"\n";
		return titles;
	}

	private String toCSV(Submission submission, boolean showTries) {
		StringBuffer sb = new StringBuffer();
		
		try {
			User user;
			user = userDirectoryService.getUser(submission.getUserId());
			sb.append("\""+user.getSortName()+"\"");
			sb.append(",");
			sb.append("\""+user.getDisplayId()+"\"");
		} catch (UserNotDefinedException e) {
			sb.append("\"not known\",\"not known\"");
		}
			
		sb.append(",");
		
		if (showTries) {
			sb.append("\""+submission.getSiblingCount()+"/");
			if (null != submission.getAssessment()) {
				Integer tries = submission.getAssessment().getTries();
				if (tries != null) {
					sb.append(tries +"\"");
				} else {
					sb.append("unlimited\"");
				}
			}
			sb.append(",");
		}
		
		if (null != submission.getSubmittedDate()) {
			sb.append("\""+sdf.format(submission.getSubmittedDate())+"\"");
		} else {
			sb.append("\"not started\"");
		}
		
		sb.append(",");
		if (null != submission.getAnswersAutoScore()) {
			sb.append("\""+submission.getAnswersAutoScore()+"\"");
		}
		
		sb.append(",");
		if (null != submission.getTotalScore()) {
			sb.append("\""+submission.getTotalScore()+"\"");
		}
		
		sb.append(",");
		if (null != submission.getEvaluation() && 
				null != submission.getEvaluation().getEvaluated()) {
			sb.append("\""+submission.getEvaluation().getEvaluated()+"\"");
		}
		
		sb.append(",");
		if (null != submission.getIsReleased()) {
			sb.append("\""+submission.getIsReleased()+"\"");
		}
		
		return sb.toString();
	}
}
