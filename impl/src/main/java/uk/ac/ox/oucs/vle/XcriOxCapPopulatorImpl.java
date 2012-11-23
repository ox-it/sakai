package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.FormattedText;
import org.xcri.Extension;
import org.xcri.common.Description;
import org.xcri.common.ExtensionManager;
import org.xcri.common.OverrideManager;
import org.xcri.core.Catalog;
import org.xcri.core.Course;
import org.xcri.core.Presentation;
import org.xcri.core.Provider;
import org.xcri.exceptions.InvalidElementException;

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
import uk.ac.ox.oucs.vle.xcri.daisy.TermCode;
import uk.ac.ox.oucs.vle.xcri.daisy.TermLabel;
import uk.ac.ox.oucs.vle.xcri.daisy.WebAuthCode;
import uk.ac.ox.oucs.vle.xcri.oxcap.MemberApplyTo;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapCourse;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapPresentation;
import uk.ac.ox.oucs.vle.xcri.oxcap.Session;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject;

public class XcriOxCapPopulatorImpl implements Populator {
	
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
	 * 
	 */
	protected ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	/**
	 * 
	 */
	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	/**
	 * 
	 */
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private static final Log log = LogFactory.getLog(XcriOxCapPopulatorImpl.class);

	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy  hh:mm");
	
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
		ExtensionManager.registerExtension(new Subject());
		ExtensionManager.registerExtension(new Session());

		OverrideManager.registerOverride(Course.class, new OxcapCourse());
		OverrideManager.registerOverride(Presentation.class, new OxcapPresentation());
	}
	
	/**
	 * 
	 */
	public void update(PopulatorContext context) {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		try {
			URL xcri = new URL(context.getURI());
		
			HttpHost targetHost = new HttpHost(xcri.getHost(), xcri.getPort(), xcri.getProtocol());

	        httpclient.getCredentialsProvider().setCredentials(
	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
	                new UsernamePasswordCredentials(context.getUser(), context.getPassword()));

            HttpGet httpget = new HttpGet(xcri.toURI());

            HttpResponse response = httpclient.execute(targetHost, httpget);
            HttpEntity entity = response.getEntity();
             
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
            	throw new IllegalStateException(
            			"Invalid response ["+response.getStatusLine().getStatusCode()+"]");
            }
            
            process(context.getName(), entity.getContent());

		} catch (MalformedURLException e) {
			log.warn("MalformedURLException ["+context.getURI()+"]", e);
			
        } catch (IllegalStateException e) {
        	log.warn("IllegalStateException ["+context.getURI()+"]", e);
			
		} catch (IOException e) {
			log.warn("IOException ["+context.getURI()+"]", e);
			
		} catch (URISyntaxException e) {
			log.warn("URISyntaxException ["+context.getURI()+"]", e);
		} finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
       
	}
	
	/**
	 * 
	 * @param inputStream
	 */
	public void process(String name, InputStream inputStream) {
		
		switchUser();
		
		try {
			Catalog catalog = new Catalog();
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(inputStream);
			catalog.fromXml(document);
			
			XcriOxcapPopulatorInstanceData data = 
					new XcriOxcapPopulatorInstanceData(contentHostingService, getSiteId(), name, simpleDateFormat.format(catalog.getGenerated()));
			
			Provider[] providers = catalog.getProviders();
		
			// First pass to create course groups
			for (Provider provider : providers) {
				provider(provider, data, true);		
			}
		
			// Second pass to create course components
			for (Provider provider : providers) {
				provider(provider, data, false);
			}
			
			data.finalise();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (JDOMException e) {
			e.printStackTrace();
			
		} catch (InvalidElementException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * 
	 * @param provider
	 * @param createGroups
	 * @throws IOException 
	 */
	private void provider(Provider provider, XcriOxcapPopulatorInstanceData data, boolean createGroups) 
			throws IOException {
		
		String departmentName = null;
		if (provider.getTitles().length > 0) {
			departmentName = provider.getTitles()[0].getValue();
		}
		String departmentCode = null;
		String divisionEmail = null;
		boolean departmentApproval = false;
		String departmentApprover = null;
		String divisionCode = null;
		Collection<String> divisionSuperUsers = new HashSet<String>();
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
				departmentApprover = extension.getValue();
				continue;
			}
			
			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.superUser) {
					divisionSuperUsers.add(webAuthCode.getValue());
				}
				continue;
			}
			
			if (extension instanceof DepartmentalSubUnit) {
				DepartmentalSubUnit subUnit = (DepartmentalSubUnit) extension;
				subunits.put(subUnit.getCode(), subUnit.getValue());
				continue;
			}
			
		}
		
		Collection<String> superusers = getUsers(divisionSuperUsers);
		String approver = getUser(departmentApprover);
		
		if (null == departmentCode) {
			XcriOxcapPopulatorInstanceData.logMe(
					"Log Failure Provider ["+departmentCode+":"+departmentName+"] No Provider Identifier");
			return;
		}
		
		if (createGroups) {
			
			data.incrDepartmentSeen();
			if (updateDepartment(departmentCode, departmentName, departmentApproval, 
				(Set<String>)Collections.singleton(approver))) {
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
		}
			
		for (Course course : provider.getCourses()) {
			course(course, departmentCode, departmentName, divisionEmail, superusers, data, !createGroups);
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
	 * @param createComponents
	 * @throws IOException 
	 */
	private void course(Course course, 
			String departmentCode, String departmentName, 
			String divisionEmail, Collection<String> divisionSuperUsers,
			XcriOxcapPopulatorInstanceData data, 
			boolean createComponents) 
					throws IOException {
		
		String title = course.getTitles()[0].getValue();
		
		OxcapCourse oxCourse = (OxcapCourse)course;
		String visibility = oxCourse.getVisibility().toString();
		String regulations = course.getRegulations()[0].getValue();
		
		Collection<Subject> researchCategories = new HashSet<Subject>();
		Collection<Subject> skillsCategories = new HashSet<Subject>();
		Collection<Subject> jacsCategories = new HashSet<Subject>();
		
		String id = null;
		String teachingcomponentId = null;
		boolean supervisorApproval = true;
		boolean administratorApproval = true;
		String subunitCode = null;
		String subunitName = null;
		Collection<String> administratorCodes = new HashSet<String>();
		Collection<String> otherDepartments = new HashSet<String>();
		
		for (Extension extension : course.getExtensions()) {
			
			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if (typeCourseId(identifier.getType())) {
					id = identifier.getValue();
				}
				if ("teachingComponentId".equals(identifier.getType())) {
					teachingcomponentId = identifier.getValue();
				}
				continue;
			}
			
			if (extension instanceof SupervisorApproval) {
				supervisorApproval = parseBoolean(extension.getValue());
				continue;
			}
			
			if (extension instanceof ModuleApproval) {
				administratorApproval = parseBoolean(extension.getValue());
				continue;
			}
			
			if (extension instanceof CourseSubUnit) {
				CourseSubUnit subUnit = (CourseSubUnit)extension;
				subunitCode = subUnit.getCode();
				subunitName = subUnit.getValue();
				continue;
			}
			
			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.administrator) {
					administratorCodes.add(webAuthCode.getValue());
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
				
				if (subject.isRDFCategory()) {
					skillsCategories.add(subject);
				}
				if (subject.isRMCategory()) {
					researchCategories.add(subject);
				}
				if (subject.isJACSCategory()) {
					jacsCategories.add(subject);
				}
				continue;
			}
		}
		
		Collection<String> administrators = getUsers(administratorCodes);
		
		if (null == id) {
			XcriOxcapPopulatorInstanceData.logMe(
					"Log Failure Course ["+id+":"+title+"] No Course Identifier");
			return;
		}
		
		String description = null;
		if (course.getDescriptions().length > 0) {
			Description xDescription = course.getDescriptions()[0];
			if (!xDescription.isXhtml()) {
				description = parse(xDescription.getValue());
			} else {
				description = xDescription.getValue();
			}
		} else {
			XcriOxcapPopulatorInstanceData.logMe(
					"Log Warning Course ["+id+"] has no description");
		}
			
			
		if (createComponents) {
			
			Presentation[] presentations = course.getPresentations();
			for (int i=0; i<presentations.length; i++) {
				presentation(presentations[i], id, teachingcomponentId, data);
			}
			
		} else {
			
			if (!id.equals(data.getLastGroup())) {
				
				data.incrGroupSeen();
				data.setLastGroup(id);
			
				if (validCourse(id, title, departmentCode, subunitCode, description,
						departmentName, subunitName, visibility, 
						supervisorApproval, administratorApproval,
						divisionEmail, regulations,
						(Set<String>) administrators, 
						(Set<String>) divisionSuperUsers, 
						(Set<String>) otherDepartments,
						(Set<Subject>) researchCategories, 
						(Set<Subject>) skillsCategories, 
						(Set<Subject>) jacsCategories)) {
			
					if (updateCourse(id, title, departmentCode, subunitCode, description,
							departmentName, subunitName, visibility, 
							supervisorApproval, administratorApproval,
							divisionEmail, regulations, data.getFeed(),
							(Set<String>) administrators, 
							(Set<String>) divisionSuperUsers, 
							(Set<String>) otherDepartments,
							(Set<Subject>) researchCategories, 
							(Set<Subject>) skillsCategories, 
							(Set<Subject>) jacsCategories)) {
						data.incrGroupCreated();
					} else {
						data.incrGroupUpdated();
					}
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typeCourseId(String type) {
		if ("ns:daisy-course".equals(type) ||
			"ns:itlp-course".equals(type) ||
			"ns:careers-course".equals(type) ||
			"ns:language-centre-course".equals(type) ||
			"ns:medsci-course".equals(type) ||
			"ns:sharepoint-course".equals(type)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param presentation
	 * @param teachingcomponentId
	 * @param groups
	 * @throws IOException 
	 */
	private void presentation(Presentation presentation, 
			String assessmentunitCode, String teachingcomponentId, XcriOxcapPopulatorInstanceData data) 
					throws IOException {
		
		String title = presentation.getTitles()[0].getValue();
		String subject = null;
		String slot = null;
		String applyTo = null;
		Date startDate = null;
		String startText = null;
		Date endDate = null;
		String endText = null;
		Date openDate = null;
		String openText = null;
		Date closeDate = null;
		String closeText = null;
		int capacity = 0;
		String location = null;
		String attendanceMode = null;
		String attendanceModeText = null;
		String attendancePattern = null;
		String attendancePatternText = null;
		
		if (null != presentation.getAttendanceMode()) {
			attendanceMode = presentation.getAttendanceMode().getIdentifier();
			attendanceModeText = presentation.getAttendanceMode().getValue();
		}
		if (null != presentation.getAttendancePattern()) {
			attendancePattern = presentation.getAttendancePattern().getIdentifier();
			attendancePatternText = presentation.getAttendancePattern().getValue();
		}
		if (null != presentation.getApplyTo()) {
			applyTo = presentation.getApplyTo().getValue();
		}
		
		if (null != presentation.getStart()) {
			startDate = presentation.getStart().getDtf();
			startText = presentation.getStart().getValue();
		}
		if (null != presentation.getEnd()) {
			endDate = presentation.getEnd().getDtf();
			endText = presentation.getEnd().getValue();
		}
		if (null != presentation.getApplyFrom()) {
			openDate = presentation.getApplyFrom().getDtf();
			openText = presentation.getApplyFrom().getValue();
		}
		if (null != presentation.getApplyUntil()) {
			closeDate = presentation.getApplyUntil().getDtf();
			closeText = presentation.getApplyUntil().getValue();
		}
		if (0 != presentation.getVenues().length) {
			location = presentation.getVenues()[0].getProvider().getTitles()[0].getValue();
		}
		
		boolean bookable = false;
		String id = null;
		String uri = null;
		String teacherId = null;
		String teacherName = null;
		String teacherEmail = null;
		String sessionCount = null;
		String termCode = null;
		String sessionDates = null;
		String memberApplyTo = null;
		Collection<Session> sessions = new HashSet<Session>();
		
		for (Extension extension : presentation.getExtensions()) {
			
			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if ("presentationURI".equals(identifier.getType())) {
					uri = identifier.getValue();
					continue;
				}
				if (typePresentationId(identifier.getType())) {
					id = identifier.getValue();
					continue;
				}
			}
			
			if (extension instanceof Bookable) {
				bookable = parseBoolean(extension.getValue());
				continue;
			}
			
			if (extension instanceof EmployeeName) {
				teacherName = extension.getValue();
				continue;
			}
			
			if (extension instanceof EmployeeEmail) {
				teacherEmail = extension.getValue();
				continue;
			}
			
			if (extension instanceof MemberApplyTo) {
				memberApplyTo = extension.getValue();
				continue;
			}
			
			if (extension instanceof Sessions) {
				sessionCount = extension.getValue();
				continue;
			}
			
			if (extension instanceof TermCode) {
				termCode = extension.getValue();
				continue;
			}
			
			if (extension instanceof TermLabel) {
				sessionDates = extension.getValue();
				continue;
			}
			
			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.presenter) {
					teacherId = webAuthCode.getValue();
				}
				continue;
			}
			
			if (extension instanceof Session) {
				Session session = (Session)extension;
				if (session.getIdentifiers().length > 0) {
					sessions.add(session);
					continue;
				}
			}
		
		}
		
		if (null != presentation.getPlaces() &&
				!presentation.getPlaces().getValue().isEmpty()) {
			try {
				capacity = Integer.parseInt(presentation.getPlaces().getValue());
			} catch (Exception e) {
				XcriOxcapPopulatorInstanceData.logMe(
						"Log Warning Presentation ["+id+"] value in places tag is not a number ["+presentation.getPlaces().getValue()+"]");
			}
		}
		
		Set<String> groups = new HashSet<String>();
		groups.add(assessmentunitCode);
		
		Collection<CourseGroupDAO> courseGroups = getCourseGroups(groups);
		
		data.incrComponentSeen();
		
		if (validComponent(id, title, subject, 
				openDate, openText, closeDate, closeText, startDate, startText, endDate, endText,
				bookable, capacity, 
				termCode,  teachingcomponentId, sessionDates,
				teacherId, teacherName, teacherEmail,
				attendanceMode, attendanceModeText, 
				attendancePattern, attendancePatternText, 
				slot, sessionCount, location, applyTo, memberApplyTo,
				(Set<Session>) sessions, (Set<CourseGroupDAO>) courseGroups)) {
			
			if (updateComponent(id, title, subject, 
					openDate, openText, closeDate, closeText, startDate, startText, endDate, endText,
					bookable, capacity, 
					termCode,  teachingcomponentId, sessionDates,
					teacherId, teacherName, teacherEmail,
					attendanceMode, attendanceModeText, 
					attendancePattern, attendancePatternText, 
					slot, sessionCount, location, applyTo, memberApplyTo, data.getFeed(),
					(Set<Session>) sessions, (Set<CourseGroupDAO>) courseGroups)) {
				data.incrComponentCreated();
			} else {
				data.incrComponentUpdated();
			}
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typePresentationId(String type) {
		if ("ns:daisy-presentation".equals(type) ||
			"ns:careers-presentation".equals(type) ||
			"ns:itlp-presentation".equals(type) ||
			"ns:language-centre-presentation".equals(type) ||
			"ns:medsci-presentation".equals(type) ||
			"ns:sharepoint-presentation".equals(type)) {
			return true;
		}
		return false;
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
				approve+":"+approvers.iterator().next()+"]");
		
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
	 * @param code
	 * @param administrators
	 * @return
	 */
	protected static boolean validCourse(String code, String title, String departmentCode, String subunitCode, 
			String description, String departmentName, String subunitName, 
			String visibility, boolean supervisorApproval, boolean administratorApproval,
			String divisionEmail, String regulations,
			Set<String> administrators, Set<String> superusers, Set<String> otherDepartments,
			Set<Subject> researchCategories, Set<Subject> skillsCategories, Set<Subject> jacsCategories) {
		
		log.debug("XcriPopulatorImpl.validCourse ["+code+":"+title+":"+departmentCode+":"+subunitCode+":"+ 
					description+":"+departmentName+":"+subunitName+":"+ 
					visibility+":"+supervisorApproval+":"+administratorApproval+":"+
					divisionEmail+":"+ 
					administrators.size()+":"+superusers.size()+":"+otherDepartments.size()+":"+
					researchCategories.size()+":"+skillsCategories.size()+"]");
		
		int i=0;
		
		try {
			if (null == code) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Assessment Unit ["+code+"] No AssessmentUnit code");
				i++;
			}
			/*
			if (administrators.isEmpty()) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Assessment Unit ["+code+"] No Group Administrators");
				i++;
			}
			*/
			if (i == 0) {
				return true;
			}
			
		} catch (IOException e) {
			
		}
		
		return false;
	}
	/**
	 * 
	 * @param code
	 * @param title
	 * @param departmentCode
	 * @param subunitCode
	 * @param description
	 * @param departmentName
	 * @param subunitName
	 * @param publicView
	 * @param supervisorApproval
	 * @param administratorApproval
	 * @param divisionEmail
	 * @param administrators
	 * @param superusers
	 * @param otherDepartments
	 * @return
	 * @throws IOException 
	 */
	private boolean updateCourse(String id, String title, String departmentCode, String subunitCode, 
			String description, String departmentName, String subunitName, 
			String visibility, boolean supervisorApproval, boolean administratorApproval,
			String divisionEmail, String regulations, String feed,
			Set<String> administrators, 
			Set<String> superusers, 
			Set<String> otherDepartments,
			Set<Subject> researchCategories, 
			Set<Subject> skillsCategories, 
			Set<Subject> jacsCategories) throws IOException {
		
		boolean created = false;
		
		if (null != dao) {
			CourseGroupDAO groupDao = dao.findCourseGroupById(id);
		
			if (groupDao == null) {
				groupDao = dao.newCourseGroup(id, title, departmentCode, subunitCode);
				created = true;
			} else {
				groupDao.setDept(departmentCode);
				groupDao.setSubunit(subunitCode);
				groupDao.setTitle(title);
			}
			groupDao.setDescription(description);
			groupDao.setDepartmentName(departmentName);
			groupDao.setSubunitName(subunitName);
			groupDao.setVisibility(visibility);
			groupDao.setSource(feed);
			groupDao.setSupervisorApproval(supervisorApproval);
			groupDao.setAdministratorApproval(administratorApproval);
			groupDao.setContactEmail(divisionEmail);
			groupDao.setAdministrators(administrators);
			groupDao.setDeleted(false);
			
			if (null==superusers) {
				superusers = Collections.<String>emptySet();
			}
			groupDao.setSuperusers(superusers);
			
			if (null==otherDepartments) {
				otherDepartments = Collections.<String>emptySet();
			}
			groupDao.setOtherDepartments(otherDepartments);
			
			Set<CourseCategoryDAO> categories = new HashSet<CourseCategoryDAO>();
			for (Subject subject : researchCategories) {
				categories.add(new CourseCategoryDAO(
						CourseGroup.Category_Type.RM, subject.getIdentifier(), subject.getValue()));
			}
			for (Subject subject : skillsCategories) {
				categories.add(new CourseCategoryDAO(
						CourseGroup.Category_Type.RDF, subject.getIdentifier(), subject.getValue()));
			}
			for (Subject subject : jacsCategories) {
				categories.add(new CourseCategoryDAO(
						CourseGroup.Category_Type.JACS, subject.getIdentifier(), subject.getValue()));
			}
			
			//remove unwanted categories
			// done this way to avoid java.util.ConcurrentModificationException 
			for (Iterator<CourseCategoryDAO> itr = groupDao.getCategories().iterator(); itr.hasNext();) {
				CourseCategoryDAO category = itr.next();
				if (!categories.contains(category)) {
			        itr.remove();
			    }
			}
			
			//add any new categories
			for (CourseCategoryDAO category : categories) {
				if (!groupDao.getCategories().contains(category)) {
					groupDao.getCategories().add(category);
				}
			}
			
			dao.save(groupDao);
		}
		
		if (created) {
			XcriOxcapPopulatorInstanceData.logMs("Log Success Course Group created ["+id+":"+title+"]");
		} else {
			XcriOxcapPopulatorInstanceData.logMs("Log Success Course Group updated ["+id+":"+title+"]");
		}
		return created;
	}
	
	/**
	 * 
	 * @param id
	 * @param title
	 * @param subject
	 * @param openDate
	 * @param closeDate
	 * @param expiryDate
	 * @param termCode
	 * @param teachingComponentId
	 * @param termName
	 * @param groups
	 * @return
	 */
	protected static boolean validComponent(String id, String title, String subject, 
			Date openDate, String openText, Date closeDate, String closeText, Date startDate, String startText, Date endDate, String endText, 
			boolean bookable, int capacity, 
			String termCode,  String teachingComponentId, String termName,
			String teacherId, String teacherName, String teacherEmail,
			String attendanceMode, String attendanceModeText,
			String attendancePattern, String attendancePatternText,
			String sessionDates, String sessionCount, String location, String applyTo, String memberApplyTo,
			Set<Session> sessions, Set<CourseGroupDAO> groups) {
		
		log.debug("XcriPopulatorImpl.validComponent ["+id+":"+title+":"+subject+":"+
				viewDate(openDate, openText)+":"+viewDate(closeDate, closeText)+":"+viewDate(startDate, startText)+":"+viewDate(endDate, endText)+":"+
				bookable+":"+capacity+":"+
				termCode+":"+teachingComponentId+":"+termName+":"+
				teacherId+":"+teacherName+":"+teacherEmail+":"+
				attendanceMode+":"+attendanceModeText+":"+
				attendancePattern+":"+attendancePatternText+":"+
				sessionDates+":"+sessions+":"+location+":"+
				applyTo+":"+memberApplyTo+":"+
				groups.size()+"]");
		
		int i=0;
		
		try {
			/*
			if (null == openDate) { 
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] No open date set");
				i++;
			}
		
			if (null == closeDate) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] No close date set");
				i++;
			}
			*/
			if (null != openDate && null != closeDate) {
				if (openDate.after(closeDate)){
					XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] Open date is after close date");
					i++;
				}
			}
			/*
			if (subject == null || subject.trim().length() == 0) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] Subject isn't set");
				i++;
			}
			*/
			if (title == null || title.trim().length() == 0) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] Title isn't set");
				i++;
			}
			/*
			if (termCode == null || termCode.trim().length() == 0) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] Term code can't be empty");
				i++;
			}
		
			if (termName == null || termName.trim().length() == 0) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] Term name can't be empty");
				i++;
			}
			
			if (teachingComponentId == null || teachingComponentId.trim().length()==0) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] No teaching component ID found");
				i++;
			}
			*/
			if (groups.isEmpty()) {
				XcriOxcapPopulatorInstanceData.logMe("Log Failure Teaching Instance ["+id+"] No Assessment Unit codes");
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
	 * @param id
	 * @param title
	 * @param subject
	 * @param openDate
	 * @param closeDate
	 * @param expiryDate
	 * @param startDate
	 * @param endDate
	 * @param bookable
	 * @param capacity
	 * @param termCode
	 * @param teachingComponentId
	 * @param termName
	 * @param teacherId
	 * @param teacherName
	 * @param teacherEmail
	 * @param sessionDates
	 * @param sessions
	 * @param location
	 * @param groups
	 * @return
	 * @throws IOException 
	 */
	private boolean updateComponent(String id, String title, String subject, 
			Date openDate, String openText, Date closeDate, String closeText, Date startDate, String startText, Date endDate, String endText, 
			boolean bookable, int capacity, 
			String termCode,  String teachingComponentId, String termName,
			String teacherId, String teacherName, String teacherEmail,
			String attendanceMode, String attendanceModeText,
			String attendancePattern, String attendancePatternText, 
			String sessionDates, String sessionCount, String location, 
			String applyTo, String memberApplyTo, String feed,
			Set<Session> sessions, Set<CourseGroupDAO> groups) throws IOException {
		
		boolean created = false;
		if (null != dao) {
			CourseComponentDAO componentDao = dao.findCourseComponent(id);
			if (componentDao == null) {
				componentDao = dao.newCourseComponent(id);
				created = true;
			}
			componentDao.setTitle(title);
			componentDao.setSubject(subject);
			componentDao.setOpens(openDate);
			componentDao.setOpensText(openText);
			componentDao.setCloses(closeDate);
			componentDao.setClosesText(closeText);
			componentDao.setStarts(startDate);
			componentDao.setStartsText(startText);
			componentDao.setEnds(endDate);
			componentDao.setEndsText(endText);
			componentDao.setBookable(bookable);
			componentDao.setSize(capacity);
			componentDao.setTermcode(termCode);
			componentDao.setAttendanceMode(attendanceMode);
			componentDao.setAttendanceModeText(attendanceModeText);
			componentDao.setAttendancePattern(attendancePattern);
			componentDao.setAttendancePatternText(attendancePatternText);
			componentDao.setComponentId(teachingComponentId+":"+termCode);
		
			componentDao.setBaseDate(baseDate(componentDao));
			componentDao.setSource(feed);
					
			// Cleanout existing groups.
			componentDao.setGroups(new HashSet<CourseGroupDAO>());
		
			// Populate teacher details.
			// Look for details in WebLearn first then fallback to details in DAISY.
			if (teacherId != null && teacherId.length() > 0) {
				UserProxy teacher = proxy.findUserByEid(teacherId);
				if (teacher != null) {
					teacherName = teacher.getDisplayName();
					teacherEmail = teacher.getEmail();
				}
			}
			componentDao.setTeacherName(teacherName);
			componentDao.setTeacherEmail(teacherEmail);
			componentDao.setWhen(termName);
			componentDao.setSlot(sessionDates);
			componentDao.setSessions(sessionCount);
			componentDao.setLocation(location);
			componentDao.setApplyTo(applyTo);
			componentDao.setMemberApplyTo(memberApplyTo);
			componentDao.setGroups(groups);
			componentDao.setDeleted(false);
			
			Collection<CourseComponentSessionDAO> componentSessions = componentDao.getComponentSessions();
			for (Session session : sessions) {
				componentSessions.add(
						new CourseComponentSessionDAO(session.getIdentifiers()[0].getValue(),
								session.getStart().getDtf(), session.getStart().getValue(), 
								session.getEnd().getDtf(), session.getEnd().getValue()));
			}
			
			
			dao.save(componentDao);
		}
		
		if (created) {
			XcriOxcapPopulatorInstanceData.logMs("Log Success Course Component created ["+id+":"+title+"]");
		} else {
			XcriOxcapPopulatorInstanceData.logMs("Log Success Course Component updated ["+id+":"+title+"]");
		}
		return created;
	}
	
	/**
	 * convert collection of userCodes to userIds
	 * 
	 * @param userCodes
	 * @return
	 */
	private Collection<String> getUsers (Collection<String> userCodes) {
		
		Set<String> userIds = new HashSet<String>();
		for (String userCode : userCodes) {
			String userId = getUser(userCode);
			if (null != userId) {
				userIds.add(userId);
			}
		}
		return userIds;
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
			System.out.println("Failed to find User [" + userCode +"]");
			return null;
		}
		return user.getId();
	}
	
	/**
	 * 
	 * @param groups
	 * @return
	 */
	private Collection<CourseGroupDAO> getCourseGroups (Collection<String> groups) {
		
		Set<CourseGroupDAO> courseGroups = new HashSet<CourseGroupDAO>();
		for (String group : groups) {
			CourseGroupDAO courseDao = null;
			if (null == dao) {
				courseDao = new CourseGroupDAO();
				courseDao.setCourseId(group);
			} else {
				courseDao = dao.findCourseGroupById(group);
			}
			if (null == courseDao) {
				System.out.println("Failed to find Group [" + group +"]");	
			} else {
				courseGroups.add(courseDao);
			}
		}
		return courseGroups;
	}
	

	protected static String viewDate(Date date, String text) {
		if (null == date) {
			return text+"[null]";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	    return text+"["+sdf.format(date)+"]";
	}

	protected String getSiteId() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("course-signup.site-id", "course-signup");
		}
		return "course-signup";
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
	 * 
	 * @param component
	 * @return
	 */
	public static Date baseDate(CourseComponentDAO component) {
		if (null != component.getStarts()) {
			return component.getStarts();
		}
		if (null != component.getCloses()) {
			return component.getCloses();
		}
		return null;
	}
	/**
	 * 
	 * @param data
	 * @return
	 */
	private String parse(String data) {
		
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
	 * This sets up the user for the current request.
	 */
	private void switchUser() {
		if (null != sessionManager) {
			org.sakaiproject.tool.api.Session session = sessionManager.getCurrentSession();
			session.setUserEid("admin");
			session.setUserId("admin");
		}
	}
}
