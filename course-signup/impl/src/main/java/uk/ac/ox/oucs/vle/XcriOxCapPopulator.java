/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.sakaiproject.util.FormattedText;
import org.springframework.orm.hibernate3.HibernateAccessor;
import org.xcri.Extension;
import org.xcri.common.ExtensionManager;
import org.xcri.common.OverrideManager;
import org.xcri.common.Title;
import org.xcri.core.Catalog;
import org.xcri.core.Course;
import org.xcri.core.Presentation;
import org.xcri.core.Provider;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.presentation.Venue;
import org.xcri.types.DescriptiveTextType;

import uk.ac.ox.oucs.vle.xcri.daisy.Bookable;
import uk.ac.ox.oucs.vle.xcri.daisy.CourseSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentThirdLevelApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentalSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.DivisionWideEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeName;
import uk.ac.ox.oucs.vle.xcri.daisy.Identifier;
import uk.ac.ox.oucs.vle.xcri.daisy.ModuleApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.OtherDepartment;
import uk.ac.ox.oucs.vle.xcri.daisy.Sessions;
import uk.ac.ox.oucs.vle.xcri.daisy.SupervisorApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.TeachingDetails;
import uk.ac.ox.oucs.vle.xcri.daisy.TermCode;
import uk.ac.ox.oucs.vle.xcri.daisy.TermLabel;
import uk.ac.ox.oucs.vle.xcri.daisy.WebAuthCode;
import uk.ac.ox.oucs.vle.xcri.oxcap.MemberApplyTo;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapCourse;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapPresentation;
import uk.ac.ox.oucs.vle.xcri.oxcap.Session;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject;

/**
 * This is a standard importer that parse Xcri files.
 */
public class XcriOxCapPopulator implements Populator {

	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}

	/**
	 * The proxy for getting users.
	 */
	private SakaiProxy proxy;
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	/**
	 * The class to get the inputStream.
	 */
	private PopulatorInput populatorInput;
	public void setPopulatorInput(PopulatorInput populatorInput) {
		this.populatorInput = populatorInput;
	}
	
	/**
	 * The search service
	 */
	private SearchService search;
	public void setSearchService(SearchService search) {
		this.search = search;
	}

	private static final Log log = LogFactory.getLog(XcriOxCapPopulator.class);

	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy  hh:mm");
	
	private static String TARGET_AUDIENCE = "cdp:targetAudience";

	static {
		ExtensionManager.registerExtension(new WebAuthCode());
		ExtensionManager.registerExtension(new DepartmentalSubUnit());
		ExtensionManager.registerExtension(new DepartmentThirdLevelApproval());
		ExtensionManager.registerExtension(new DivisionWideEmail());
		ExtensionManager.registerExtension(new CourseSubUnit());
		ExtensionManager.registerExtension(new ModuleApproval());
		ExtensionManager.registerExtension(new SupervisorApproval());
		ExtensionManager.registerExtension(new OtherDepartment());
		ExtensionManager.registerExtension(new Sessions());
		ExtensionManager.registerExtension(new Bookable());
		ExtensionManager.registerExtension(new TermCode());
		ExtensionManager.registerExtension(new TermLabel());
		ExtensionManager.registerExtension(new EmployeeName());
		ExtensionManager.registerExtension(new EmployeeEmail());
		ExtensionManager.registerExtension(new Identifier());
		ExtensionManager.registerExtension(new MemberApplyTo());
		ExtensionManager.registerExtension(new TeachingDetails());
		ExtensionManager.registerExtension(new Subject());
		ExtensionManager.registerExtension(new Session());

		OverrideManager.registerOverride(Course.class, new OxcapCourse());
		OverrideManager.registerOverride(Presentation.class, new OxcapPresentation());
	}

	/**
	 * @throws  
	 * @throws MalformedURLException 
	 * 
	 */
	public void update(PopulatorContext context) throws PopulatorException {

		InputStream input = null;
		
		try {
			// This is so that collections don't get lost, this is a hack but I couldn't find the
			// simple fix to get it working.
			dao.setFlushMode(HibernateAccessor.FLUSH_EAGER);
			input = populatorInput.getInput(context);

			if (null == input) {
				throw new PopulatorException("No Input for Importer");
			}
			process(context, input);

		} catch (MalformedURLException e) {
			log.warn("MalformedURLException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (IllegalStateException e) {
			log.warn("IllegalStateException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (IOException e) {
			log.warn("IOException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (JDOMException e) {
			log.warn("JDOMException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (InvalidElementException e) {
			log.warn("InvalidElementException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());
			
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					log.error("IOException ["+e+"]");
				}
			}
		}

	}

	/**
	 * Attempts to create a category mapper using config from the SakaiProxy, or
	 * if that one isn't found config from the classpath. If no mappings can be found anywhere
	 * it still returns a mapper, but that mapper doesn't alter anything.
	 *
	 * @return A CategoryMapper and never <code>null</code>.
	 */
	private CategoryMapper getCategoryMapper() {
		CategoryMapper mapper = new CategoryMapper();
		Properties props = proxy.getCategoryMapping();
		if (props == null) {
			// fallback to classpath.
			String classpathResource =
					proxy.getConfigParam("course-signup.default-category-mapping", "/default-category-mapping.properties");
			props = new Properties();
			try {
				props.load(getClass().getResourceAsStream(classpathResource));
			} catch (IOException e) {
				log.error("Failed to load default category mapping from classpath using: "+ classpathResource+
						" No mapping of categories will be done", e);
			}
		}
		if (props != null) {
			mapper.setMappings(props);
		}

		return mapper;
	}

	/**
	 * 
	 * @param inputStream
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws InvalidElementException 
	 */
	public void process(PopulatorContext context, InputStream inputStream) 
			throws JDOMException, IOException, InvalidElementException {

		Catalog catalog = new Catalog();
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(inputStream);
		catalog.fromXml(document);

		// Here we setup our category mapper, we don't have this as a singleton managed by spring because
		// the content hosting service isn't up when spring does init() on a singleton.
		// We could have followed that route and done lazy loading, but then you still need to manage
		// reload when someone changes the file. This way it's loaded for every import.
		CategoryMapper mapper = getCategoryMapper();
			
		PopulatorInstanceData data = new PopulatorInstanceData(mapper);
		
		if (null != context.getDeletedLogWriter()) {
			context.getDeletedLogWriter().heading(catalog.getGenerated());
		}
		if (null != context.getErrorLogWriter()) {
			context.getErrorLogWriter().heading(catalog.getGenerated());
		}
		if (null != context.getInfoLogWriter()) {
			context.getInfoLogWriter().heading(catalog.getGenerated());
		}
			
		Provider[] providers = catalog.getProviders();
		
		for (Provider provider : providers) {
			provider(provider, context, data);		
		}
			
		logMs(context, 
				"CourseDepartments (seen: "+ data.getDepartmentSeen() + " created: "+ data.getDepartmentCreated() + ", updated: "+ data.getDepartmentUpdated() +")");
		logMs(context,
				"CourseSubUnits (seen: "+ data.getSubunitSeen() + " created: "+ data.getSubunitCreated() + ", updated: "+ data.getSubunitUpdated() +")");
		logMs(context,
				"CourseGroups (seen: "+ data.getGroupSeen() + " created: "+ data.getGroupCreated() + ", updated: "+ data.getGroupUpdated() +")");
		logMs(context,
				"CourseComponents (seen: "+ data.getComponentSeen() + " created: "+ data.getComponentCreated() + ", updated: "+ data.getComponentUpdated() +")");

	}

	/**
	 * 
	 * @param provider
	 * @throws IOException 
	 */
	private void provider(Provider provider, PopulatorContext context, PopulatorInstanceData data) 
			throws IOException {

		String departmentName = null;
		if (provider.getTitles().length > 0) {
			departmentName = provider.getTitles()[0].getValue();
		}
		String departmentCode = null;
		String divisionEmail = null;
		boolean departmentApproval = false;
		String divisionCode = null;
		Set<String> departmentApprovers = new HashSet<String>();
		Set<String> divisionSuperUsers = new HashSet<String>();
		Map<String, String> subunits = new HashMap<String, String>();

		for (Extension extension : provider.getExtensions()) {

			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if (typeProviderId(identifier.getType())) {
					if (typeProviderFallbackId(identifier.getType()) &&
							null != departmentCode) {
						continue;
					}
					departmentCode = identifier.getValue();
					continue;
				}
				if (typeProviderDivision(identifier.getType())) {
					divisionCode = identifier.getValue();
					continue;
				}
			}

			if (extension instanceof DivisionWideEmail) {
				divisionEmail = extension.getValue();
				continue;
			}

			if (extension instanceof DepartmentThirdLevelApproval) {
				departmentApproval = parseBoolean(extension.getValue());
				continue;
			}

			if (extension instanceof ModuleApproval) {
				departmentApprovers.add(getUser(extension.getValue()));
				continue;
			}

			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;

				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.superUser) {
					divisionSuperUsers.add(getUser(webAuthCode.getValue()));
				}
				continue;
			}

			if (extension instanceof DepartmentalSubUnit) {
				DepartmentalSubUnit subUnit = (DepartmentalSubUnit) extension;
				subunits.put(subUnit.getCode(), subUnit.getValue());
				continue;
			}

		}

		if (null == departmentCode) {
			logMe(context, 
				"Log Failure Provider ["+departmentCode+":"+departmentName+"] No Provider Identifier");
			return;
		}

		data.incrDepartmentSeen();
		if (updateDepartment(departmentCode, departmentName, departmentApproval, 
				departmentApprovers)) {
			data.incrDepartmentCreated();;
		} else {
			data.incrDepartmentUpdated();
		}

		for (Map.Entry<String, String> entry : subunits.entrySet()) {
			data.incrSubunitSeen();
			if (updateSubUnit(entry.getKey(), entry.getValue(), departmentCode)) {
				data.incrSubunitCreated();;
			} else {
				data.incrSubunitUpdated();
			}
		}

		for (Course course : provider.getCourses()) {
			course(course, departmentCode, departmentName, divisionEmail, divisionSuperUsers, context, data);
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typeProviderId(String type) {
		if ("ns:department".equals(type) ||
				"ns:twoThree".equals(type)) {
			return true;
		}
		return false;
	}

	protected static boolean typeProviderFallbackId(String type) {
		if ("ns:department".equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typeProviderDivision(String type) {
		if ("ns:division".equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * Process <course> tag
	 * 
	 * @param course
	 * @param departmentCode
	 * @param departmentName
	 * @param divisionEmail
	 * @param divisionSuperUsers
	 * @throws IOException 
	 */
	private void course(Course course, 
			String departmentCode, String departmentName, 
			String divisionEmail, Set<String> divisionSuperUsers,
			PopulatorContext context,
			PopulatorInstanceData data) 
					throws IOException {

		CourseGroupDAO myCourse = new CourseGroupDAO();

		myCourse.setSource(context.getName());
		myCourse.setDept(departmentCode);
		myCourse.setDepartmentName(departmentName);
		myCourse.setContactEmail(divisionEmail);
		myCourse.setTitle(course.getTitles()[0].getValue());

		OxcapCourse oxCourse = (OxcapCourse)course;
		myCourse.setVisibility(oxCourse.getVisibility().toString());

		myCourse.setDescription(filterDescriptiveTextTypeArray(course.getDescriptions(), null));
		myCourse.setPrerequisite(filterDescriptiveTextTypeArray(course.getDescriptions(), TARGET_AUDIENCE));
		myCourse.setRegulations(filterDescriptiveTextTypeArray(course.getRegulations(), null));

		Set<Subject.SubjectIdentifier> categories = new HashSet<Subject.SubjectIdentifier>();


		String teachingComponentId = null;
		Set<String> administrators = new HashSet<String>();
		Set<String> otherDepartments = new HashSet<String>();

		for (Extension extension : course.getExtensions()) {

			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if (typeCourseId(identifier.getType())) {
					myCourse.setCourseId(identifier.getValue());
				}
				if ("teachingComponentId".equals(identifier.getType())) {
					teachingComponentId = identifier.getValue();
				}
				continue;
			}

			if (extension instanceof SupervisorApproval) {
				myCourse.setSupervisorApproval(parseBoolean(extension.getValue()));
				continue;
			}

			if (extension instanceof ModuleApproval) {
				myCourse.setAdministratorApproval(parseBoolean(extension.getValue()));
				continue;
			}

			if (extension instanceof CourseSubUnit) {
				CourseSubUnit subUnit = (CourseSubUnit)extension;
				myCourse.setSubunit(subUnit.getCode());
				myCourse.setSubunitName(subUnit.getValue());
				continue;
			}

			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.administrator) {
					administrators.add(getUser(webAuthCode.getValue()));
				}
				continue;
			}

			if (extension instanceof OtherDepartment) {
				if (!extension.getValue().isEmpty()) {
					otherDepartments.add(extension.getValue());
				}
				continue;
			}

			if (extension instanceof Subject) {
				Subject subject = (Subject) extension;

				// Check it's a in our constrained vocab
				Subject.SubjectIdentifier subjectIdentifier = subject.getSubjectIdentifier();
				if (subjectIdentifier != null && subject.isValid()) {
					categories.add(subjectIdentifier);
				} else {
					log.debug(String.format("Ignoring subject of %s", subject));
				}
			}
		}

		// Map existing course categories onto vitae course categories.
		data.getMapper().mapCategories(categories);

		myCourse.setAdministrators(administrators);
		myCourse.setOtherDepartments(otherDepartments);
		myCourse.setSuperusers(divisionSuperUsers);

		if (null == myCourse.getCourseId()) {
			logMe(context, 
					"Log Failure Course ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"] No Course Identifier");
			return;
		}
			
		if (myCourse.getDescription().isEmpty()) {
			logMe(context, 
					"Log Warning Course ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"] has no description");
		}

		if (!myCourse.getCourseId().equals(data.getLastGroup())) {

			data.incrGroupSeen();
			data.setLastGroup(myCourse.getCourseId());

			if (validCourse(context, data, myCourse)) {
				updateCourse(context, data, myCourse);
			}
		}

		Presentation[] presentations = course.getPresentations();
		for (int i=0; i<presentations.length; i++) {
			presentation(presentations[i], myCourse.getCourseId(), teachingComponentId, context, data);
		}

		for (Subject.SubjectIdentifier subjectIdentifier : categories) {
			CourseGroup.CategoryType type = null;
			if (subjectIdentifier instanceof Subject.RMSubjectIdentifier) {
				type = CourseGroup.CategoryType.RM;
			} else if (subjectIdentifier instanceof  Subject.VITAESubjectIdentifier) {
				type = CourseGroup.CategoryType.VITAE;
			} else if (subjectIdentifier instanceof  Subject.RDFSubjectIdentifier) {
				type = CourseGroup.CategoryType.RDF;
			} else {
				log.warn("Unknown subject identifier "+ subjectIdentifier);
				continue;
			}
			updateCategory(context, new CourseCategoryDAO(
					type, subjectIdentifier.name(), subjectIdentifier.getValue()),
					myCourse.getCourseId());
		}
		/**
		 * Update the search engine
		 */
		if (null != search) {
			CourseGroupDAO courseDao = dao.findCourseGroupById(myCourse.getCourseId());
			search.addCourseGroup(new CourseGroupImpl(courseDao, null));
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typeCourseId(String type) {
		return ("ns:daisy-course".equals(type) ||
				"ns:itlp-course".equals(type) ||
				"ns:careers-course".equals(type) ||
				"ns:language-centre-course".equals(type) ||
				"ns:medsci-course".equals(type) ||
				"ns:humanities-course".equals(type) ||
				"ns:oli-course".equals(type) ||
				"ns:sharepoint-course".equals(type)
		);
	}

	/**
	 * 
	 * @param presentation
	 * @param teachingcomponentId
	 * @throws IOException 
	 */
	private void presentation(Presentation presentation, 
			String assessmentunitCode, String teachingcomponentId, 
			PopulatorContext context,
			PopulatorInstanceData data) 
					throws IOException {

		CourseComponentDAO myPresentation = new CourseComponentDAO();

		myPresentation.setComponentId(teachingcomponentId);
		myPresentation.setSource(context.getName());
		myPresentation.setTitle(presentation.getTitles()[0].getValue());

		if (null != presentation.getAttendanceMode()) {
			myPresentation.setAttendanceMode(presentation.getAttendanceMode().getIdentifier());
			myPresentation.setAttendanceModeText(presentation.getAttendanceMode().getValue());
		}

		if (null != presentation.getAttendancePattern()) {
			myPresentation.setAttendancePattern(presentation.getAttendancePattern().getIdentifier());
			myPresentation.setAttendancePatternText(presentation.getAttendancePattern().getValue());
		}

		if (null != presentation.getApplyTo()) {
			myPresentation.setApplyTo(presentation.getApplyTo().getValue());
		}

		if (null != presentation.getStart()) {
			myPresentation.setStarts(presentation.getStart().getDtf());
			myPresentation.setStartsText(presentation.getStart().getValue());
		}

		if (null != presentation.getEnd()) {
			myPresentation.setEnds(presentation.getEnd().getDtf());
			myPresentation.setEndsText(presentation.getEnd().getValue());
		}

		if (null != presentation.getApplyFrom()) {
			myPresentation.setOpens(presentation.getApplyFrom().getDtf());
			myPresentation.setOpensText(presentation.getApplyFrom().getValue());
		}

		if (null != presentation.getApplyUntil()) {
			myPresentation.setCloses(presentation.getApplyUntil().getDtf());
			myPresentation.setClosesText(presentation.getApplyUntil().getValue());
		}

		if (0 != presentation.getVenues().length) {
			Venue venue = presentation.getVenues()[0];
			if (null != venue.getProvider() && venue.getProvider().getTitles().length > 0) {
				myPresentation.setLocation(venue.getProvider().getTitles()[0].getValue());
			}
		}

		Set<Session> sessions = new HashSet<Session>();

		for (Extension extension : presentation.getExtensions()) {

			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if ("presentationURI".equals(identifier.getType())) {
					//uri = identifier.getValue();
					continue;
				}
				if (typePresentationId(identifier.getType())) {
					myPresentation.setPresentationId(identifier.getValue());
					continue;
				}
			}

			if (extension instanceof Bookable) {
				myPresentation.setBookable(parseBoolean(extension.getValue()));
				continue;
			}

			if (extension instanceof EmployeeName) {
				myPresentation.setTeacherName(extension.getValue());
				continue;
			}

			if (extension instanceof EmployeeEmail) {
				myPresentation.setTeacherEmail(extension.getValue());
				continue;
			}

			if (extension instanceof MemberApplyTo) {
				myPresentation.setMemberApplyTo(extension.getValue());
				continue;
			}

			if (extension instanceof Sessions) {
				myPresentation.setSessions(extension.getValue());
				continue;
			}

			if (extension instanceof TermCode) {
				// TODO We should validate it here.
				// TT09, HT09, MT09, TT10
				myPresentation.setTermcode(extension.getValue());
				continue;
			}

			if (extension instanceof TeachingDetails) {
				myPresentation.setTeachingDetails(extension.getValue());
				continue;
			}

			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.presenter) {
					myPresentation.setTeacher(getUser(webAuthCode.getValue()));
				}
				continue;
			}

			if (extension instanceof Session) {
				Session session = (Session)extension;
				sessions.add(session);
				continue;
			}

		}

		if (null != presentation.getPlaces() &&
				!presentation.getPlaces().getValue().isEmpty()) {
			try {
				myPresentation.setSize(Integer.parseInt(presentation.getPlaces().getValue()));
				
			} catch (Exception e) {
				logMs(context,
						"Log Warning Presentation ["+
								myPresentation.getPresentationId()+":"+myPresentation.getTitle()+
								"] value in places tag is not a number ["+presentation.getPlaces().getValue()+"]");
			}
		}

		CourseGroupDAO courseDao = dao.findCourseGroupById(assessmentunitCode);

		data.incrComponentSeen();

		if (validComponent(context, data, myPresentation, courseDao)) {
			Iterator<Session> sessionIterator = sessions.iterator();
			while (sessionIterator.hasNext()) {
				Session session = sessionIterator.next();
				if (! (validSession(context, session, myPresentation))) {
					// Logging is done in the validate.
					sessionIterator.remove();
				}
			}
			updateComponent(context, data, myPresentation, sessions, courseDao);
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typePresentationId(String type) {
		return ("ns:daisy-presentation".equals(type) ||
				"ns:careers-presentation".equals(type) ||
				"ns:itlp-presentation".equals(type) ||
				"ns:language-centre-presentation".equals(type) ||
				"ns:medsci-presentation".equals(type) ||
				"ns:sharepoint-presentation".equals(type) ||
				"ns:oli-presentation".equals(type) ||
				"ns:humanities-presentation".equals(type)
			);
	}

	/**
	 * 
	 * @param code
	 * @param name
	 * @param approve
	 * @param approvers
	 * @return
	 */
	private boolean updateDepartment(String code, String name, boolean approve, Set<String> approvers) {

		log.debug("XcriPopulatorImpl.updateDepartment ["+code+":"+name+":"+
				approve+":"+approvers.size()+"]");

		boolean created = false;

		if (null != dao) {
			CourseDepartmentDAO departmentDao = dao.findDepartmentByCode(code);

			if (null == departmentDao) {
				departmentDao = new CourseDepartmentDAO(code);
				created = true;
			}
			departmentDao.setName(name);
			departmentDao.setApprove(approve);
			departmentDao.setApprovers(approvers);
			dao.save(departmentDao);
		}

		return created;
	}

	/**
	 * 
	 * @param code
	 * @param name
	 * @param departmentCode
	 * @return
	 */
	private boolean updateSubUnit(String code, String name, String departmentCode) {

		log.debug("XcriPopulatorImpl.updateSubUnit ["+
				code+":"+name+":"+departmentCode+"]");

		boolean created = false;

		if (null != dao) {
			CourseSubunitDAO subunitDao = dao.findSubunitByCode(code);
			if (null == subunitDao) {
				subunitDao = new CourseSubunitDAO(code);
				created = true;
			}
			subunitDao.setSubunitName(name);
			subunitDao.setDepartmentCode(departmentCode);
			dao.save(subunitDao);
		}

		return created;
	}

	/**
	 * 
	 * @param data
	 * @param myCourse
	 * @return
	 */
	protected boolean validCourse(PopulatorContext context, 
			PopulatorInstanceData data,
			CourseGroupDAO myCourse) {

		int i=0;

		try {

			if (null == myCourse.getCourseId()) {
				logMe(context,  "Log Failure Assessment Unit ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"] No AssessmentUnit code");
				i++;
			}

			if (i == 0) {
				return true;
			}

		} catch (IOException e) {

		}

		return false;
	}

	/**
	 * 
	 * @param data
	 * @param myCourse
	 * @return
	 * @throws IOException
	 */
	private boolean updateCourse(PopulatorContext context,
			PopulatorInstanceData data,
			CourseGroupDAO myCourse) throws IOException {

		boolean created = false;

		if (null != dao) {
			CourseGroupDAO groupDao = dao.findCourseGroupById(myCourse.getCourseId());

			if (groupDao == null) {
				groupDao = dao.newCourseGroup(myCourse.getCourseId(), myCourse.getTitle(), myCourse.getDept(), myCourse.getSubunit());
				created = true;
			} else {
				groupDao.setDept(myCourse.getDept());
				groupDao.setSubunit(myCourse.getSubunit());
				groupDao.setTitle(myCourse.getTitle());
			}
			groupDao.setDescription(myCourse.getDescription());
			groupDao.setDepartmentName(myCourse.getDepartmentName());
			groupDao.setSubunitName(myCourse.getSubunitName());
			groupDao.setVisibility(myCourse.getVisibility());
			groupDao.setSource(myCourse.getSource());
			groupDao.setSupervisorApproval(myCourse.getSupervisorApproval());
			groupDao.setAdministratorApproval(myCourse.getAdministratorApproval());
			groupDao.setContactEmail(myCourse.getContactEmail());
			groupDao.getAdministrators().clear();
			groupDao.getAdministrators().addAll(myCourse.getAdministrators());
			groupDao.setPrerequisite(myCourse.getPrerequisite());
			groupDao.setRegulations(myCourse.getRegulations());
			groupDao.setDeleted(false);
			groupDao.getSuperusers().clear();
			groupDao.getSuperusers().addAll(myCourse.getSuperusers());
			groupDao.getOtherDepartments().clear();
			groupDao.getOtherDepartments().addAll(myCourse.getOtherDepartments());

			dao.save(groupDao);
		}

		if (created) {
			logMs(context, "Log Success Course Group created ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"]");
			data.incrGroupCreated();
		} else {
			logMs(context, "Log Success Course Group updated ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"]");
			data.incrGroupUpdated();
		}
		return created;
	}


	/**
	 * This validates that a session is correctly populated.
	 *
	 * @param context The populator context (used for logging).
	 * @param session The session to validate.
	 * @param myPresentation The presentation this is linked to so we can log against it (presentation shoudl be valid).
	 * @return <code>true</code> if the session is good.
	 */
	private boolean validSession(PopulatorContext context, Session session, CourseComponentDAO myPresentation) {
		int i = 0;
		try {
			if (session.getIdentifiers().length == 0) {
				logMe(context, "Log Failure Session for Teaching Instance [" + myPresentation.getPresentationId() + "] No session identifier");
				i++;
			} else {
				String id = session.getIdentifiers()[0].getValue();
				if (id == null || id.length() == 0) {
					logMe(context, "Log Failure Session for Teaching Instance [" + myPresentation.getPresentationId() + "] Empty session identifier");
					i++;
				}
			}
			if (session.getStart() == null) {
				logMe(context, "Log Failure Session for Teaching Instance [" + myPresentation.getPresentationId() + "] No start date");
				i++;
			}
			if (session.getEnd() == null) {
				logMe(context, "Log Failure Session for Teaching Instance [" + myPresentation.getPresentationId() + "] No end date");
				i++;
			}
			return i == 0;
		} catch (IOException ioe) {
			log.warn("Failed to write log: "+ ioe.getMessage());
		}
		return false;
	}


	/**
	 * 
	 * @param data
	 * @param myPresentation
	 * @return
	 */
	protected boolean validComponent(PopulatorContext context,
									 PopulatorInstanceData data,
									 CourseComponentDAO myPresentation,
									 CourseGroupDAO group) {

		int i=0;

		try {
			if (myPresentation.getPresentationId() == null || myPresentation.getPresentationId().isEmpty()) {
				logMe(context,  "Log Failure Teaching Instance ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"] No presentation ID");
				i++;
			}

			if (null != myPresentation.getOpens() && null != myPresentation.getCloses()) {
				if (myPresentation.getOpens().after(myPresentation.getCloses())){
					logMe(context, "Log Failure Teaching Instance ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"] Open date is after close date");
					i++;
				}
			}

			if (myPresentation.getTitle() == null || myPresentation.getTitle().trim().length() == 0) {
				logMe(context, "Log Failure Teaching Instance ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"] Title isn't set");
				i++;
			}

			if (null == group) {
				logMe(context, "Log Failure Teaching Instance ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"] No Assessment Unit codes");
				i++;
			}

			if (i == 0) {
				return true;
			}

		} catch (IOException e) {
			log.warn("Failed to write log: "+ e.getMessage());
		}
		return false;
	}

	/**
	 * 
	 * @param data
	 * @param myPresentation
	 * @param sessions
	 * @return
	 * @throws IOException
	 */
	private boolean updateComponent(PopulatorContext context,
			PopulatorInstanceData data,
			CourseComponentDAO myPresentation,
			Set<Session> sessions, CourseGroupDAO group) throws IOException {

		boolean created = false;
		if (null != dao) {
			CourseComponentDAO componentDao = dao.findCourseComponent(myPresentation.getPresentationId());
			if (componentDao == null) {
				componentDao = dao.newCourseComponent(myPresentation.getPresentationId());
				created = true;
			}
			componentDao.setTitle(myPresentation.getTitle());
			componentDao.setOpens(myPresentation.getOpens());
			componentDao.setOpensText(myPresentation.getOpensText());
			componentDao.setCloses(myPresentation.getCloses());
			componentDao.setClosesText(myPresentation.getClosesText());
			componentDao.setStarts(myPresentation.getStarts());
			componentDao.setStartsText(myPresentation.getStartsText());
			componentDao.setEnds(myPresentation.getEnds());
			componentDao.setEndsText(myPresentation.getEndsText());
			componentDao.setBookable(myPresentation.isBookable());
			componentDao.setSize(myPresentation.getSize());
			componentDao.setTermcode(myPresentation.getTermcode());
			componentDao.setAttendanceMode(myPresentation.getAttendanceMode());
			componentDao.setAttendanceModeText(myPresentation.getAttendanceModeText());
			componentDao.setAttendancePattern(myPresentation.getAttendancePattern());
			componentDao.setAttendancePatternText(myPresentation.getAttendancePatternText());
			componentDao.setComponentId(myPresentation.getComponentId()+":"+myPresentation.getTermcode());
			componentDao.setTeacher(myPresentation.getTeacher());
			componentDao.setTeacherName(myPresentation.getTeacherName());
			componentDao.setTeacherEmail(myPresentation.getTeacherEmail());
			componentDao.setSessions(myPresentation.getSessions());
			componentDao.setLocation(myPresentation.getLocation());
			componentDao.setApplyTo(myPresentation.getApplyTo());
			componentDao.setMemberApplyTo(myPresentation.getMemberApplyTo());
			componentDao.setTeachingDetails(myPresentation.getTeachingDetails());

			componentDao.setBaseDate(baseDate(componentDao));
			componentDao.setSource(myPresentation.getSource());

			// Populate teacher details.
			// Look for details in WebLearn first then fallback to details in DAISY.
			if (myPresentation.getTeacher() != null && myPresentation.getTeacher().length() > 0) {
				UserProxy teacher = proxy.findUserByEid(myPresentation.getTeacher());
				if (teacher != null) {
					componentDao.setTeacherName(teacher.getDisplayName());
					componentDao.setTeacherEmail(teacher.getEmail());
				}
			}

			// Use of Set filters duplicates
			componentDao.getGroups().add(group);
			componentDao.setDeleted(false);
			
			componentDao.getComponentSessions().clear();
			for (Session session : sessions) {

				if (session.getIdentifiers().length < 1) {
					logMe(context, "Skipped session because no identifier found ["
							+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"]");
					continue;
				}

				String identifier = session.getIdentifiers()[0].getValue();
				List<String> locations = new ArrayList<String>(2);
				for (Venue venue: session.getVenues()) {
					if (venue.getProvider() != null) {
						for (Title title : venue.getProvider().getTitles()) {
							locations.add(title.getValue());
						}
					}

				}
				if (locations.size() > 1) {
					logMs(context, "More than one location found for session:"+ identifier + " ["
							+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"]");
				}
				String location = StringUtils.trimToNull(StringUtils.join(locations, " "));

				componentDao.getComponentSessions().add(
						new CourseComponentSessionDAO(identifier,
								session.getStart().getDtf(), session.getStart().getValue(), 
								session.getEnd().getDtf(), session.getEnd().getValue(), location));
			}
			if (!componentDao.getComponentSessions().isEmpty()) {
				componentDao.setSessions(Integer.toString(componentDao.getComponentSessions().size()));
			}
			
			dao.save(componentDao);
		}

		if (created) {
			logMs(context, "Log Success Course Component created ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"]");
			data.incrComponentCreated();
		} else {
			logMs(context, "Log Success Course Component updated ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"]");
			data.incrComponentUpdated();
		}
		return created;
	}

	/*
	 * 
	 */
	private boolean updateCategory(PopulatorContext context,CourseCategoryDAO category, String assessmentunitCode) throws IOException {

		boolean created = false;
		
		if (null == category.getCategoryId() || category.getCategoryId().isEmpty()) {
			logMe(context, "Category ["+category.getCategoryType()+":"+category.getCategoryName()+
					"] ignored on course ["+assessmentunitCode+"] - empty identifier");
			return created;
		}

		if (null != dao) {
			CourseCategoryDAO categoryDao = dao.findCourseCategory(category.getCategoryId());
			if (categoryDao == null) {
				// We have a race condition here when multiple threads attempt to create the same category
				categoryDao = category;
				created = true;
				dao.save(categoryDao);
			}

			CourseGroupDAO courseDao = dao.findCourseGroupById(assessmentunitCode);
			courseDao.getCategories().add(categoryDao);
			dao.save(courseDao);
		}

		return created;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void logMe(PopulatorContext context, String message) throws IOException {
		log.info(message);
		if (null != context.getErrorLogWriter()) {
			context.getErrorLogWriter().write(message+"\n");
		}
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void logMs(PopulatorContext context, String message) throws IOException {
		log.info(message);
		if (null != context.getInfoLogWriter()) {
			context.getInfoLogWriter().write(message+"\n");
		}
	}


	/**
	 * 
	 * @param userCode
	 * @return
	 */
	private String getUser (String userCode) {

		if (null == proxy) {
			return userCode;
		}
		UserProxy user = proxy.findUserByEid(userCode);
		if (null == user) {
			log.warn("Failed to find User [" + userCode +"]");
			return null;
		}
		return user.getId();
	}

	protected static String viewDate(Date date, String text) {
		if (null == date) {
			return text+"[null]";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return text+"["+sdf.format(date)+"]";
	}

	private static boolean parseBoolean(String data) {
		if ("1".equals(data)) {
			return true;
		}
		if ("0".equals(data)) {
			return false;
		}
		return Boolean.parseBoolean(data);
	}

	/**
	 * Find the  base date for a component.
	 * The base date is the point at which a course transitions from being a current course to a previous course.
	 * 
	 * @param component
	 * 			the component to assess.
	 * @return
	 * 			the base date for the component or <code>null</code> if it's not possible to find one.
	 */
	public static Date baseDate(CourseComponentDAO component) {
		
		if (null != component.getEnds()) {
			return component.getEnds();
		}
		if (null != component.getCloses()) {
			return component.getCloses();
		}
		return null;
	}

	/**
	 * Search the array for elements matching type and add concatenate their descriptions.
	 * 
	 * @param array
	 * 		The array of DescriptiveTextTypes
	 * @param type
	 * 		The type to filter on, if <code>null</code> look for text without a type (<code>null</code>).
	 * @return
	 * 		The concatenated matching descriptions.
	 */
	protected String filterDescriptiveTextTypeArray(DescriptiveTextType[] array, String type) {

		StringBuilder sb = new StringBuilder();
		
		for (DescriptiveTextType descriptiveTextType: array) {

			String text;
			if (!descriptiveTextType.isXhtml()) {
				text = parse(descriptiveTextType.getValue());
			} else {
				text = parseXHTML(descriptiveTextType.getValue());
			}
			
			if (null != type) {
				if (type.equals(descriptiveTextType.getType())) {
					sb.append(text).append(" ");
				}
			} else {
				if (null == descriptiveTextType.getType()) {
					sb.append(text).append(" ");
				}
			}
		}
		
		return sb.toString().trim();
	}

	/**
	 *  * Processing of descriptivetext fields where descriptiveTextType.isXhtml=false
	 * 
	 * @param data
	 * @return
	 */
	static String parse(String data) {

		data = data.replaceAll("<", "&lt;");
		data = data.replaceAll(">", "&gt;");
		data = FormattedText.convertPlaintextToFormattedText(data);

		Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(data);

		StringBuffer sb = new StringBuffer(data.length());
		while (matcher.find()) {
			String text = matcher.group(0);
			matcher.appendReplacement(sb, "<a class=\"email\" href=\"mailto:"+text+"\">"+text+"</a>" );
		}
		matcher.appendTail(sb);

		pattern = Pattern.compile("(https?|ftps?):\\/\\/[a-z_0-9\\\\\\-]+(\\.([\\w#!:?+=&%@!\\-\\/])+)+", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(sb.toString());

		sb = new StringBuffer(data.length());
		while (matcher.find()) {
			String text = matcher.group(0);
			matcher.appendReplacement(sb, "<a class=\"url\" href=\""+text+"\" target=\"_blank\">"+text+"</a>" );
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	/**
	 * Processing of descriptivetext fields where descriptiveTextType.isXhtml=true
	 * 
	 * @param data
	 * @return
	 */
	static String parseXHTML(String data) {
		if (null != data) {
			data = data.replaceAll("xhtml:", "");
		}
		return data;
	}

}
