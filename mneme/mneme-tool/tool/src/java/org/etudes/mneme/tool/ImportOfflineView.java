/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/ImportOfflineView.java $
 * $Id: ImportOfflineView.java 9671 2014-12-24 23:29:14Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.SubmissionService;
import org.etudes.roster.api.RosterService;
import org.sakaiproject.exception.IdUnusedException;
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
 * The /import_offline view for the mneme tool.
 */
public class ImportOfflineView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ImportOfflineView.class);

	/** Assessment Service */
	protected AssessmentService assessmentService = null;

	/** The RosterService. */
	protected RosterService rosterService = null;

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** The SiteService. */
	protected SiteService siteService = null;

	/** Submission Service */
	protected SubmissionService submissionService = null;

	/** tool manager reference. */
	protected ToolManager toolManager = null;

	/** UserDirectoryService */
	protected UserDirectoryService userDirectoryService = null;

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
		if (!this.assessmentService.allowManageAssessments(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		String returnUrl = (params.length > 3) ? params[2] : "";
		String sort = (params.length > 3) ? params[3] : "0A";

		context.put("returnUrl", returnUrl);
		context.put("sort", sort);

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		String returnUrl = (params.length > 3) ? params[2] : "";
		String sort = (params.length > 3) ? params[3] : "0A";

		String siteId = toolManager.getCurrentPlacement().getContext();
		if (!this.assessmentService.allowManageAssessments(siteId))
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

		String iidCode = this.rosterService.findInstitutionCode(site.getTitle());

		// a CSV uploader for the CSV file
		UploadCsv upload = new UploadCsv();
		context.put("upload", upload);

		// read the form
		String destination = uiService.decode(req, context);

		// import the offlines
		if ("UPLOAD".equals(destination))
		{
			List<GradeImportSet> importSets = new ArrayList<GradeImportSet>(1);

			// get the Tool session, where we will be storing this until confirmed
			ToolSession toolSession = sessionManager.getCurrentToolSession();
			toolSession.setAttribute(GradeImportSet.ATTR_NAME, importSets);

			// col 0, the student id col 1 .. n, the title of the offline assessment, in the first record, and the score, in the rest
			List<CSVRecord> records = upload.getRecords();

			if ((records != null) && (records.size() > 1))
			{
				// the header record
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

				if (idCol != -1)
				{
					// try all other columns
					for (int i = 0; i < header.size(); i++)
					{
						// we use this column for the Student ID
						if (i == idCol) continue;

						// we will keep this ONLY if we see at least one valid grade entry
						GradeImportSet set = new GradeImportSet();
						boolean hasValidEntries = false;

						// get the assessment title, points
						String title = StringUtil.trimToZero(header.get(i));
						Float points = null;
						int extraPos = title.indexOf("{{");
						if (extraPos != -1)
						{
							String extra = StringUtil.trimToZero(title.substring(extraPos + 2, title.length() - 2));
							title = StringUtil.trimToZero(title.substring(0, extraPos));
							try
							{
								points = Float.valueOf(extra);
							}
							catch (NumberFormatException e)
							{
							}
						}

						// skip blank header columns
						if (title.length() == 0) continue;

						// these two titles we also skip, as created by UCB Gradebook's export
						if ("Section ID".equals(title)) continue;
						if ("Cumulative".equals(title)) continue;

						// new assessment or existing
						Assessment existing = assessmentService.assessmentExists(siteId, title);
						if (existing != null)
						{
							// just ignore if it does not take points
							if (!existing.getHasPoints())
							{
								continue;
							}
							set.assessment = existing;
						}
						else
						{
							set.assessmentTitle = title;
							set.points = points;
						}

						for (CSVRecord r : records)
						{
							// skip the header
							if (r.getRecordNumber() == 1) continue;

							GradeImport x = new GradeImport();
							set.rows.add(x);

							if (r.size() >= 1)
							{
								x.studentId = r.get(idCol);
							}
							if (r.size() > i)
							{
								x.scoreGiven = r.get(i);
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
									for (GradeImport gi : set.rows)
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
										x.userId = user.getId();
										x.name = user.getDisplayName();
										hasValidEntries = true;
									}
								}
							}
						}

						// keep this ONLY if we see at least one valid grade entry
						if (hasValidEntries)
						{
							importSets.add(set);
						}
					}
				}
			}

			destination = "/confirm_grades_import/" + returnUrl + "/" + sort;
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * @param poolService
	 *        the poolService to set
	 */
	public void setAssessmentService(AssessmentService asssessmentService)
	{
		this.assessmentService = asssessmentService;
	}

	/**
	 * @param rosterService
	 *        the rosterService to set
	 */
	public void setRosterService(RosterService rosterService)
	{
		this.rosterService = rosterService;
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
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		sessionManager = service;
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
	 * Set the tool manager.
	 * 
	 * @param manager
	 *        The tool manager.
	 */
	public void setToolManager(ToolManager manager)
	{
		toolManager = manager;
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
		try
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
}
