package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.FormattedText;
import org.xcri.Extension;
import org.xcri.common.ExtensionManager;
import org.xcri.common.OverrideManager;
import org.xcri.common.Subject;
import org.xcri.core.Catalog;
import org.xcri.core.Course;
import org.xcri.core.Presentation;
import org.xcri.core.Provider;
import org.xcri.exceptions.InvalidElementException;

import uk.ac.ox.oucs.vle.xcri.daisy.Bookable;
import uk.ac.ox.oucs.vle.xcri.daisy.CourseSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.DaisyIdentifier;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentThirdLevelApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentalSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.Division;
import uk.ac.ox.oucs.vle.xcri.daisy.DivisionWideEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeName;
import uk.ac.ox.oucs.vle.xcri.daisy.ModuleApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.OtherDepartment;
import uk.ac.ox.oucs.vle.xcri.daisy.PublicView;
import uk.ac.ox.oucs.vle.xcri.daisy.Sessions;
import uk.ac.ox.oucs.vle.xcri.daisy.SupervisorApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.TermCode;
import uk.ac.ox.oucs.vle.xcri.daisy.TermLabel;
import uk.ac.ox.oucs.vle.xcri.daisy.WebAuthCode;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapCourse;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapPresentation;

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

	XcriOxcapPopulatorInstanceData data;
	
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy  hh:mm");
	
	private String SUBJECTTYPE_RDF = "ox-rdf:notation";
	private String SUBJECTTYPE_JACS = "ox-jacs:notation";
	private String SUBJECTTYPE_RM = "ox-rm:notation";
	
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

            HttpGet httpget = new HttpGet(xcri.getPath());

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
			
			ExtensionManager.registerExtension(new WebAuthCode());
			ExtensionManager.registerExtension(new DepartmentalSubUnit());
			ExtensionManager.registerExtension(new DepartmentThirdLevelApproval());
			ExtensionManager.registerExtension(new Division());
			ExtensionManager.registerExtension(new DivisionWideEmail());
			ExtensionManager.registerExtension(new CourseSubUnit());
			ExtensionManager.registerExtension(new PublicView());
			ExtensionManager.registerExtension(new ModuleApproval());
			ExtensionManager.registerExtension(new SupervisorApproval());
			ExtensionManager.registerExtension(new OtherDepartment());
			ExtensionManager.registerExtension(new Sessions());
			ExtensionManager.registerExtension(new Bookable());
			ExtensionManager.registerExtension(new TermCode());
			ExtensionManager.registerExtension(new TermLabel());
			ExtensionManager.registerExtension(new EmployeeName());
			ExtensionManager.registerExtension(new EmployeeEmail());
			ExtensionManager.registerExtension(new DaisyIdentifier());
			//ExtensionManager.registerExtension(new Subject());
			
			OverrideManager.registerOverride(Course.class, new OxcapCourse());
			OverrideManager.registerOverride(Presentation.class, new OxcapPresentation());
			
			Catalog catalog = new Catalog();
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(inputStream);
			catalog.fromXml(document);
			
			data = new XcriOxcapPopulatorInstanceData(contentHostingService, getSiteId(), name, simpleDateFormat.format(catalog.getGenerated()));
			
			Provider[] providers = catalog.getProviders();
		
			// First pass to create course groups
			for (Provider provider : providers) {
				provider(provider, true);		
			}
		
			// Second pass to create course components
			for (Provider provider : providers) {
				provider(provider, false);
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
	private void provider(Provider provider, boolean createGroups) 
			throws IOException {
		
		String departmentName = provider.getTitles()[0].getValue();
		String departmentCode = provider.getIdentifiers()[0].getValue();
		String divisionEmail = null;
		boolean departmentApproval = false;
		String departmentApprover = null;
		Collection<String> divisionSuperUsers = new HashSet<String>();
		Map<String, String> subunits = new HashMap<String, String>();
		
		for (Extension extension : provider.getExtensions()) {
			
			if (extension instanceof DivisionWideEmail) {
				divisionEmail = extension.getValue();
			}
			
			if (extension instanceof DepartmentThirdLevelApproval) {
				departmentApproval = parseBoolean(extension.getValue());
			}
			
			if (extension instanceof ModuleApproval) {
				departmentApprover = extension.getValue();
			}
			
			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.superUser) {
					divisionSuperUsers.add(webAuthCode.getValue());
				}
			}
			
			if (extension instanceof DepartmentalSubUnit) {
				DepartmentalSubUnit subUnit = (DepartmentalSubUnit) extension;
				subunits.put(subUnit.getCode(), subUnit.getValue());
			}
		}
		
		Collection<String> superusers = getUsers(divisionSuperUsers);
		String approver = getUser(departmentApprover);
		
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
			course(course, departmentCode, departmentName, divisionEmail, superusers, !createGroups);
		}
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
			boolean createComponents) 
					throws IOException {
		
		String title = course.getTitles()[0].getValue();
		String description = parseToPlainText(course.getDescriptions()[0].getValue());
		
		Collection<String> researchCategories = new HashSet<String>();
		Collection<String> skillsCategories = new HashSet<String>();
		
		for (Subject subject : course.getSubjects()) {
			if (SUBJECTTYPE_RDF.equals(subject.getType())) {
				skillsCategories.add(subject.getValue());
			}
			if (SUBJECTTYPE_RM.equals(subject.getType())) {
				researchCategories.add(subject.getValue());
			}
		}
		
		String assessmentunitCode = null;
		String teachingcomponentId = null;
		boolean publicView = true;
		boolean supervisorApproval = true;
		boolean administratorApproval = true;
		String subunitCode = null;
		String subunitName = null;
		Collection<String> administratorCodes = new HashSet<String>();
		Collection<String> otherDepartments = new HashSet<String>();
		
		for (Extension extension : course.getExtensions()) {
			
			if (extension instanceof DaisyIdentifier) {
				DaisyIdentifier identifier = (DaisyIdentifier) extension;
				if ("assessmentUnitCode".equals(identifier.getType())) {
					assessmentunitCode = identifier.getValue();
				}
				if ("teachingComponentId".equals(identifier.getType())) {
					teachingcomponentId = identifier.getValue();
				}
			}
			
			if (extension instanceof PublicView) {
				publicView = parseBoolean(extension.getValue());
			}
			
			if (extension instanceof SupervisorApproval) {
				supervisorApproval = parseBoolean(extension.getValue());
			}
			
			if (extension instanceof ModuleApproval) {
				administratorApproval = parseBoolean(extension.getValue());
			}
			
			if (extension instanceof CourseSubUnit) {
				CourseSubUnit subUnit = (CourseSubUnit)extension;
				subunitCode = subUnit.getCode();
				subunitName = subUnit.getValue();
			}
			
			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.administrator) {
					administratorCodes.add(webAuthCode.getValue());
				}
			}
			
			if (extension instanceof OtherDepartment) {
				otherDepartments.add(extension.getValue());
			}
		}
		
		Collection<String> administrators = getUsers(administratorCodes);
		
		if (createComponents) {
			
			Presentation[] presentations = course.getPresentations();
			for (int i=0; i<presentations.length; i++) {
				presentation(presentations[i], 
						assessmentunitCode, teachingcomponentId);
			}
			
		} else {
			
			if (!assessmentunitCode.equals(data.getLastGroup())) {
				
				data.incrGroupSeen();
				data.setLastGroup(assessmentunitCode);
			
				if (validGroup(assessmentunitCode, title, departmentCode, subunitCode, description,
						departmentName, subunitName, publicView, 
						supervisorApproval, administratorApproval,
						divisionEmail, (Set<String>) administrators, 
						(Set<String>) divisionSuperUsers, (Set<String>) otherDepartments,
						(Set<String>) researchCategories, (Set<String>) skillsCategories)) {
			
					if (updateGroup(assessmentunitCode, title, departmentCode, subunitCode, description,
							departmentName, subunitName, publicView, 
							supervisorApproval, administratorApproval,
							divisionEmail, (Set<String>) administrators, 
							(Set<String>) divisionSuperUsers, (Set<String>) otherDepartments,
							(Set<String>) researchCategories, (Set<String>) skillsCategories)) {
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
	 * @param presentation
	 * @param teachingcomponentId
	 * @param groups
	 * @throws IOException 
	 */
	private void presentation(Presentation presentation, 
			String assessmentunitCode, String teachingcomponentId) 
					throws IOException {
		
		String subject = presentation.getTitles()[0].getValue();
		String title = presentation.getAttendanceMode().getValue();
		String slot = presentation.getAttendancePattern().getValue();
		
		Date startDate = null;
		Date endDate = null;
		Date openDate = null;
		Date closeDate = null;
		int capacity = 0;
		String location = null;
		
		if (null != presentation.getStart()) {
			startDate = presentation.getStart().getDtf();
		}
		if (null != presentation.getEnd()) {
			endDate = presentation.getEnd().getDtf();
		}
		if (null != presentation.getApplyFrom()) {
			openDate = presentation.getApplyFrom().getDtf();
		}
		if (null != presentation.getApplyUntil()) {
			closeDate = presentation.getApplyUntil().getDtf();
		}
		if (null != presentation.getPlaces() &&
			!presentation.getPlaces().getValue().isEmpty()) {
			capacity = Integer.parseInt(presentation.getPlaces().getValue());
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
		String sessions = null;
		String termCode = null;
		String sessionDates = null;
		
		for (Extension extension : presentation.getExtensions()) {
			
			if (extension instanceof DaisyIdentifier) {
				DaisyIdentifier identifier = (DaisyIdentifier) extension;
				if ("presentationURI".equals(identifier.getType())) {
					uri = identifier.getValue();
					continue;
				}
				if ("teachingInstanceId".equals(identifier.getType())) {
					id = identifier.getValue();
					continue;
				}
			}
			
			if (extension instanceof Bookable) {
				bookable = parseBoolean(extension.getValue());
			}
			
			if (extension instanceof EmployeeName) {
				teacherName = extension.getValue();
			}
			
			if (extension instanceof EmployeeEmail) {
				teacherEmail = extension.getValue();
			}
			
			if (extension instanceof Sessions) {
				sessions = extension.getValue();
			}
			
			if (extension instanceof TermCode) {
				termCode = extension.getValue();
			}
			
			if (extension instanceof TermLabel) {
				sessionDates = extension.getValue();
			}
			
			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.presenter) {
					teacherId = webAuthCode.getValue();
				}
			}
		
		}
		
		Set<String> groups = new HashSet<String>();
		groups.add(assessmentunitCode);
		
		Collection<CourseGroupDAO> courseGroups = getCourseGroups(groups);
		
		data.incrComponentSeen();
		
		if (validComponent(id, title, subject, 
				openDate, closeDate, startDate, endDate,
				bookable, capacity, 
				termCode,  teachingcomponentId, sessionDates,
				teacherId, teacherName, teacherEmail,
				slot, sessions, location,
				(Set<CourseGroupDAO>) courseGroups)) {
			
			if (updateComponent(id, title, subject, 
					openDate, closeDate, startDate, endDate,
					bookable, capacity, 
					termCode,  teachingcomponentId, sessionDates,
					teacherId, teacherName, teacherEmail,
					slot, sessions, location,
					(Set<CourseGroupDAO>) courseGroups)) {
				data.incrComponentCreated();
			} else {
				data.incrComponentUpdated();
			}
		}
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
		
		if (log.isDebugEnabled()) {
			System.out.println("XcriPopulatorImpl.updateDepartment ["+code+":"+name+":"+
				approve+":"+approvers.iterator().next()+"]");
		}
		
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
		
		if (log.isDebugEnabled()) {
			System.out.println("XcriPopulatorImpl.updateSubUnit ["+
				code+":"+name+":"+departmentCode+"]");
		}
		
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
	private boolean validGroup(String code, String title, String departmentCode, String subunitCode, 
			String description, String departmentName, String subunitName, 
			boolean publicView, boolean supervisorApproval, boolean administratorApproval,
			String divisionEmail, 
			Set<String> administrators, Set<String> superusers, Set<String> otherDepartments,
			Set<String> researchCategories, Set<String> skillsCategories) {
		
		if (log.isDebugEnabled()) {
			System.out.println("XcriPopulatorImpl.validGroup ["+code+":"+title+":"+departmentCode+":"+subunitCode+":"+ 
					description+":"+departmentName+":"+subunitName+":"+ 
					publicView+":"+supervisorApproval+":"+administratorApproval+":"+
					divisionEmail+":"+ 
					administrators.size()+":"+superusers.size()+":"+otherDepartments.size()+":"+
					researchCategories.size()+":"+skillsCategories.size()+"]");
		}
		
		int i=0;
		
		try {
			if (null == code) {
				data.logMe("Log Failure Assessment Unit ["+code+"] No AssessmentUnit code");
				i++;
			}
		
			if (administrators.isEmpty()) {
				data.logMe("Log Failure Assessment Unit ["+code+"] No Group Administrators");
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
	private boolean updateGroup(String code, String title, String departmentCode, String subunitCode, 
			String description, String departmentName, String subunitName, 
			boolean publicView, boolean supervisorApproval, boolean administratorApproval,
			String divisionEmail, 
			Set<String> administrators, Set<String> superusers, Set<String> otherDepartments,
			Set<String> researchCategories, Set<String> skillsCategories) throws IOException {
		
		boolean created = false;
		
		if (null != dao) {
			CourseGroupDAO groupDao = dao.findCourseGroupById(code);
		
			if (groupDao == null) {
				groupDao = dao.newCourseGroup(code, title, departmentCode, subunitCode);
				created = true;
			} else {
				groupDao.setDept(departmentCode);
				groupDao.setSubunit(subunitCode);
				groupDao.setTitle(title);
			}
			groupDao.setDescription(description);
			groupDao.setDepartmentName(departmentName);
			groupDao.setSubunitName(subunitName);
			groupDao.setPublicView(publicView);
			groupDao.setSupervisorApproval(supervisorApproval);
			groupDao.setAdministratorApproval(administratorApproval);
			groupDao.setContactEmail(divisionEmail);
			groupDao.setAdministrators(administrators);
			
			if (null==superusers) {
				superusers = Collections.<String>emptySet();
			}
			groupDao.setSuperusers(superusers);
			
			if (null==otherDepartments) {
				otherDepartments = Collections.<String>emptySet();
			}
			groupDao.setOtherDepartments(otherDepartments);
			
			Set<CourseCategoryDAO> categories = new HashSet<CourseCategoryDAO>();
			for (String category : researchCategories) {
				categories.add(new CourseCategoryDAO(
						code, CourseGroup.Category_Type.RM, "", category));
			}
			for (String category : skillsCategories) {
				categories.add(new CourseCategoryDAO(
						code, CourseGroup.Category_Type.RDF, "", category));
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
			data.logMs("Log Success Course Group created ["+code+":"+title+"]");
		} else {
			data.logMs("Log Success Course Group updated ["+code+":"+title+"]");
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
	private boolean validComponent(String id, String title, String subject, 
			Date openDate, Date closeDate, Date startDate, Date endDate,
			boolean bookable, int capacity, 
			String termCode,  String teachingComponentId, String termName,
			String teacherId, String teacherName, String teacherEmail,
			String sessionDates, String sessions, String location,
			Set<CourseGroupDAO> groups) {
		
		if (log.isDebugEnabled()) {
			System.out.println("XcriPopulatorImpl.validComponent ["+id+":"+title+":"+subject+":"+
				viewDate(openDate)+":"+viewDate(closeDate)+":"+viewDate(startDate)+":"+viewDate(endDate)+":"+
				bookable+":"+capacity+":"+
				termCode+":"+teachingComponentId+":"+termName+":"+
				teacherId+":"+teacherName+":"+teacherEmail+":"+
				sessionDates+":"+sessions+":"+location+":"+
				groups.size()+"]");
		}
		
		int i=0;
		
		try {
			
			if (null == openDate) { 
				data.logMe("Log Failure Teaching Instance ["+id+"] No open date set");
				i++;
			}
		
			if (null == closeDate) {
				data.logMe("Log Failure Teaching Instance ["+id+"] No close date set");
				i++;
			}
		
			if (null != openDate && null != closeDate) {
				if (openDate.after(closeDate)){
					data.logMe("Log Failure Teaching Instance ["+id+"] Open date is after close date");
					i++;
				}
			}
		
			if (subject == null || subject.trim().length() == 0) {
				data.logMe("Log Failure Teaching Instance ["+id+"] Subject isn't set");
				i++;
			}
		
			if (title == null || title.trim().length() == 0) {
				data.logMe("Log Failure Teaching Instance ["+id+"] Title isn't set");
				i++;
			}
		
			if (termCode == null || termCode.trim().length() == 0) {
				data.logMe("Log Failure Teaching Instance ["+id+"] Term code can't be empty");
				i++;
			}
		
			if (termName == null || termName.trim().length() == 0) {
				data.logMe("Log Failure Teaching Instance ["+id+"] Term name can't be empty");
				i++;
			}
		
			if (teachingComponentId == null || teachingComponentId.trim().length()==0) {
				data.logMe("Log Failure Teaching Instance ["+id+"] No teaching component ID found");
				i++;
			}
		
			if (groups.isEmpty()) {
				data.logMe("Log Failure Teaching Instance ["+id+"] No Assessment Unit codes");
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
			Date openDate, Date closeDate, Date startDate, Date endDate,
			boolean bookable, int capacity, 
			String termCode,  String teachingComponentId, String termName,
			String teacherId, String teacherName, String teacherEmail,
			String sessionDates, String sessions, String location,
			Set<CourseGroupDAO> groups) throws IOException {
		
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
			componentDao.setCloses(closeDate);
			componentDao.setStarts(startDate);
			componentDao.setEnds(endDate);
			componentDao.setBookable(bookable);
			componentDao.setSize(capacity);
			componentDao.setTermcode(termCode);
			componentDao.setComponentId(teachingComponentId+":"+termCode);
		
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
			componentDao.setSessions(sessions);
			componentDao.setLocation(location);
			componentDao.setGroups(groups);
			dao.save(componentDao);
		}
		
		if (created) {
			data.logMs("Log Success Course Component created ["+id+":"+subject+"]");
		} else {
			data.logMs("Log Success Course Component updated ["+id+":"+subject+"]");
		}
		return created;
	}
	
	/**
	 * Log errors and warnings
	 * @param message
	 * @throws IOException
	 */
	//private void logMe(String message) throws IOException {
	//	log.warn(message);
	//	eWriter.write(message+"\n");
	//}
	
	/**
	 * Log successes
	 * @param message
	 * @throws IOException
	 */
	//private void logMs(String message) throws IOException {
	//	log.warn(message);
	//	iWriter.write(message+"\n");
	//}
	
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
				courseDao.setId(group);
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
	

	private String viewDate(Date date) {
		if (null == date) {
			return "null";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	    return sdf.format(date);
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
	
	private String parseToPlainText(String data) {
		
		data = data.replaceAll("\\n", "<br /><br />");
		return FormattedText.convertFormattedTextToPlaintext(data);
	}
	
	/**
	 * This sets up the user for the current request.
	 */
	private void switchUser() {
		if (null != sessionManager) {
			Session session = sessionManager.getCurrentSession();
			session.setUserEid("admin");
			session.setUserId("admin");
		}
	}
	
	/*
	public static void main(String[] args) {
		try {	
			XcriOxCapPopulatorImpl reader = new XcriOxCapPopulatorImpl();
			PopulatorContext context = new PopulatorContext();
			context.setURI((String)jobDataMap.get("xcri.oxcap.populator.uri"));
			context.setUser((String)jobDataMap.get("xcri.oxcap.populator.username"));
			context.setPassword((String)jobDataMap.get("xcri.oxcap.populator.password"));
			context.setName((String)jobDataMap.get("xcri.oxcap.populator.name"));
			reader.update(context);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
}
