package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.xcri.profiles.x12.catalog.CatalogDocument;
import org.xml.sax.SAXParseException;


public class XcriPopulatorImpl implements Populator {
	
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
	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	/**
	 * 
	 */
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	/**
	 * 
	 */
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private static final Log log = LogFactory.getLog(XcriPopulatorImpl.class);
	
	private final static String XCRI_URL = "http://localhost:8080/test/courses.xml";

	private Writer writer;
	
	private String preHTMLa = "<html><head></head><body>" +
		"<h3>Errors and Warnings from SES Import ";
	private String preHTMLb = "</h3>";
	private String preHTMLc = "<h3>Using the XCRI file generated on ";
	private String preHTMLd = "</h3>";
	private String preHTML = "<pre>";
	private String postHTML = "</pre></body></html>";
	
	private int departmentSeen;
	private int departmentCreated;
	private int departmentUpdated;
	private int subunitSeen;
	private int subunitCreated;
	private int subunitUpdated;
	private int groupSeen;
	private int groupCreated;
	private int groupUpdated;
	private int componentSeen;
	private int componentCreated;
	private int componentUpdated;
	
	private String lastGroup = null;
	
	
	/**
	 * 
	 */
	public void update() {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		try {
			URL xcri = new URL(getXcriURL());
		
			HttpHost targetHost = new HttpHost(xcri.getHost(), xcri.getPort(), xcri.getProtocol());

	        httpclient.getCredentialsProvider().setCredentials(
	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
	                new UsernamePasswordCredentials(getXcriAuthUser(), getXcriAuthPassword()));

            HttpGet httpget = new HttpGet(xcri.getPath());

            HttpResponse response = httpclient.execute(targetHost, httpget);
            HttpEntity entity = response.getEntity();
             
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
            	throw new IllegalStateException(
            			"Invalid response ["+response.getStatusLine().getStatusCode()+"]");
            }
            process(entity.getContent());

		} catch (MalformedURLException e) {
			log.warn("MalformedURLException ["+getXcriURL()+"]", e);
			
        } catch (IllegalStateException e) {
        	log.warn("IllegalStateException ["+getXcriURL()+"]", e);
			
		} catch (IOException e) {
			log.warn("IOException ["+getXcriURL()+"]", e);
			
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
	public void process(InputStream inputStream) {
		
		switchUser();
		ContentResourceEdit cre = null;
		
		try {
			// Bind the incoming XML to an XMLBeans type.
			//URL xcri = new URL(inputSource);
			//CatalogDocument catalog = CatalogDocument.Factory.parse(xcri.openStream());
			CatalogDocument catalog = CatalogDocument.Factory.parse(inputStream);

			String generated = null;
			XmlObject[] objects = XcriUtils.selectPath(catalog, "catalog");
			for (int i=0; i<objects.length; i++) {
				XmlObject object = objects[i];
				generated = XcriUtils.getAttribute(object.newCursor(), "generated");
			}
		
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(out);
			
			writer.write(preHTMLa);
			writer.write(now());
			writer.write(preHTMLb);
			if (null != generated) {
				writer.write(preHTMLc);
				writer.write(generated);
				writer.write(preHTMLd);
			}
			writer.write(preHTML);
		
			departmentSeen = 0;
			departmentCreated = 0;
			departmentUpdated = 0;
			subunitSeen = 0;
			subunitCreated = 0;
			subunitUpdated = 0;
			groupSeen = 0;
			groupCreated = 0;
			groupUpdated = 0;
			componentSeen = 0;
			componentCreated = 0;
			componentUpdated = 0;
			
			lastGroup = null;
		
			XmlObject[] providers = XcriUtils.selectPath(catalog, "provider");
		
			// First pass to create course groups
			for (int i=0; i<providers.length; i++) {
				provider(providers[i], true);		
			}
		
			// Second pass to create course components
			for (int i=0; i<providers.length; i++) {
				provider(providers[i], false);		
			}
		
			logMe("CourseDepartments (seen: "+ departmentSeen+ " created: "+ departmentCreated+ ", updated: "+ departmentUpdated+")");
			logMe("CourseSubUnits (seen: "+ subunitSeen+ " created: "+ subunitCreated+ ", updated: "+ subunitUpdated+")");
			logMe("CourseGroups (seen: "+ groupSeen+ " created: "+ groupCreated+ ", updated: "+ groupUpdated+")");
			logMe("CourseComponents (seen: "+ componentSeen+ " created: "+ componentCreated+ ", updated: "+ componentUpdated+")");
	
			writer.write(postHTML);
			writer.flush();
			writer.close();
			
			if (null != contentHostingService) {
					
				String jsonResourceId = contentHostingService.getSiteCollection(getSiteId())+ "import.html";

				try {
					// editResource() doesn't throw IdUnusedExcpetion but PermissionException
					// when the resource is missing so we first just tco to find it.
					contentHostingService.getResource(jsonResourceId);
					cre = contentHostingService.editResource(jsonResourceId);
				
				} catch (IdUnusedException e) {
					try {
						cre = contentHostingService.addResource(jsonResourceId);
						ResourceProperties props = cre.getPropertiesEdit();
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, "importlog.html");
						cre.setContentType("text/html");
					} catch (Exception e1) {
						log.warn("Failed to create the import log file.", e1);
					}
				}
			
				cre.setContent(out.toByteArray());
				// Don't notify anyone about this resource.
				contentHostingService.commitResource(cre, NotificationService.NOTI_NONE);
			}
			
		} catch (XmlException e) {
			Throwable currExp = e.getCause();
			if (currExp instanceof SAXParseException) {
				log.error("SAXParseException thrown ["+
						((SAXParseException) currExp).getLineNumber() + "," +
						((SAXParseException) currExp).getColumnNumber() + "]");
			}
			log.warn("Failed to write content to logfile.", e);
			
		} catch (Exception e) {
			log.warn("Failed to write content to logfile.", e);
			
		} finally {
			if (null != cre && cre.isActiveEdit()) {
				contentHostingService.cancelResource(cre);
			}
		}
	}
		
	/**
	 * 
	 * @param provider
	 * @param createGroups
	 * @throws IOException 
	 */
	private void provider(XmlObject provider, boolean createGroups) throws IOException {
		
		String departmentName = XcriUtils.getString(provider, "providerTitle");
		String departmentCode = XcriUtils.getString(provider, "providerIdentifier");
		String divisionEmail = XcriUtils.getString(provider, "providerDivisionEmail");
		boolean departmentApproval = XcriUtils.getBoolean(provider, "providerDepartmentApproval", false);
		String departmentApprover = XcriUtils.getString(provider, "providerApprover");
		Collection<String> divisionSuperUsers = XcriUtils.getSet(provider, "providerSuperUser");
		Map<String, String> subunits = XcriUtils.getMap(provider, "providerSubUnit", "code");
		
		Collection<String> superusers = getUsers(divisionSuperUsers);
		String approver = getUser(departmentApprover);
		
		if (createGroups) {
			
			departmentSeen++;
			if (updateDepartment(departmentCode, departmentName, departmentApproval, 
				(Set<String>)Collections.singleton(approver))) {
				departmentCreated++;;
			} else {
				departmentUpdated++;
			}
			
			for (Map.Entry<String, String> entry : subunits.entrySet()) {
				subunitSeen++;
				if (updateSubUnit(entry.getKey(), entry.getValue(), departmentCode)) {
					subunitCreated++;;
				} else {
					subunitUpdated++;
				}
			}
		}
		
		XmlObject[] courses = XcriUtils.selectPath(provider, "providerCourse");
		
		if (createGroups) {
			for (int i=0; i<courses.length; i++) {
				course(courses[i], departmentCode, departmentName, divisionEmail, superusers, false);
			}
		} else {
			for (int i=0; i<courses.length; i++) {
				course(courses[i], departmentCode, departmentName, divisionEmail, superusers, true);
			}
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
	private void course(XmlObject course, 
			String departmentCode, String departmentName, 
			String divisionEmail, Collection<String> divisionSuperUsers,
			boolean createComponents) throws IOException {
		
		String assessmentunitCode = XcriUtils.getString(course, "courseIdentifierCode");
		String teachingcomponentId = XcriUtils.getString(course, "courseIdentifierComponent");
		String title = XcriUtils.getString(course, "courseSubject");
		String description = XcriUtils.getString(course, "courseDescription");
		boolean publicView = XcriUtils.getBoolean(course, "coursePublicView", true);
		boolean supervisorApproval = XcriUtils.getBoolean(course, "courseSupervisorApproval", true);
		boolean administratorApproval = XcriUtils.getBoolean(course, "courseModuleApproval", true);
	
		Map.Entry<String, String> subunit = XcriUtils.getEntry(course, "courseSubUnit", "code");
		
		Collection<String> researchCategories = XcriUtils.getSet(course, "courseCategoryResearch");
		Collection<String> skillsCategories = XcriUtils.getSet(course, "courseCategorySkills");
		
		Collection<String> administratorCodes = XcriUtils.getSet(course, "courseAdministrator");
		Collection<String> otherDepartments = XcriUtils.getSet(course, "courseOtherDepartment");
		
		Collection<String> administrators = getUsers(administratorCodes);
		
		if (createComponents) {
			
			XmlObject[] presentations = XcriUtils.selectPath(course, "coursePresentation");
			for (int i=0; i<presentations.length; i++) {
				presentation(presentations[i], 
						assessmentunitCode, teachingcomponentId);
			}
			
		} else {
			
			if (!assessmentunitCode.equals(lastGroup)) {
				
				groupSeen++;
				lastGroup = assessmentunitCode;
			
				if (validGroup(assessmentunitCode, title, departmentCode, subunit.getKey(), description,
						departmentName, subunit.getValue(), publicView, 
						supervisorApproval, administratorApproval,
						divisionEmail, (Set<String>) administrators, 
						(Set<String>) divisionSuperUsers, (Set<String>) otherDepartments,
						(Set<String>) researchCategories, (Set<String>) skillsCategories)) {
			
					if (updateGroup(assessmentunitCode, title, departmentCode, subunit.getKey(), description,
							departmentName, subunit.getValue(), publicView, 
							supervisorApproval, administratorApproval,
							divisionEmail, (Set<String>) administrators, 
							(Set<String>) divisionSuperUsers, (Set<String>) otherDepartments,
							(Set<String>) researchCategories, (Set<String>) skillsCategories)) {
						groupCreated++;
					} else {
						groupUpdated++;
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
	private void presentation(XmlObject presentation, 
			String assessmentunitCode, String teachingcomponentId) throws IOException {
		
		String id = XcriUtils.getString(presentation, "presentationIdentifier");
		String subject = XcriUtils.getString(presentation, "presentationTitle");
		Date startDate = XcriUtils.getDate(presentation, "presentationStart");
		Date endDate = XcriUtils.getDate(presentation, "presentationEnd");
		String title = XcriUtils.getString(presentation, "presentationAttendanceMode");
		String slot = XcriUtils.getString(presentation, "presentationAttendancePattern");
		int capacity = XcriUtils.getInt(presentation, "presentationPlaces");
		Date openDate = XcriUtils.getDate(presentation, "presentationApplyFrom");
		Date closeDate = XcriUtils.getDate(presentation, "presentationApplyUntil");
		String location = XcriUtils.getString(presentation, "presentationVenue");
		boolean bookable = XcriUtils.getBoolean(presentation, "presentationBookable", false);
		Date expiryDate = XcriUtils.getDate(presentation, "presentationExpiry");
		String teacherId = XcriUtils.getString(presentation, "presentationPresenter");
		String teacherName = XcriUtils.getString(presentation, "presentationPresenterName");
		String teacherEmail = XcriUtils.getString(presentation, "presentationPresenterEmail");
		String sessions = XcriUtils.getString(presentation, "presentationSessions");
		String termCode = XcriUtils.getString(presentation, "presentationTermCode");
		String sessionDates = XcriUtils.getString(presentation, "presentationTermLabel");
		
		Set<String> groups = new HashSet<String>();
		groups.add(assessmentunitCode);
		
		Collection<CourseGroupDAO> courseGroups = getCourseGroups(groups);
		if (expiryDate == null) {
			expiryDate = closeDate;
		}
		
		componentSeen++;
		
		if (validComponent(id, title, subject, 
				openDate, closeDate, expiryDate, startDate, endDate,
				bookable, capacity, 
				termCode,  teachingcomponentId, sessionDates,
				teacherId, teacherName, teacherEmail,
				slot, sessions, location,
				(Set<CourseGroupDAO>) courseGroups)) {
			
			if (updateComponent(id, title, subject, 
					openDate, closeDate, expiryDate, startDate, endDate,
					bookable, capacity, 
					termCode,  teachingcomponentId, sessionDates,
					teacherId, teacherName, teacherEmail,
					slot, sessions, location,
					(Set<CourseGroupDAO>) courseGroups)) {
				componentCreated++;
			} else {
				componentUpdated++;
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
				logMe("Log Failure ["+code+":"+"No AssessmentUnit code");
				i++;
			}
		
			if (administrators.isEmpty()) {
				logMe("Log Failure ["+code+":"+"No Group Administrators");
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
						code, CourseGroup.Category_Type.RDF, "", category));
			}
			for (String category : skillsCategories) {
				categories.add(new CourseCategoryDAO(
						code, CourseGroup.Category_Type.RM, "", category));
			}
			
			//remove unwanted categories
			for (CourseCategoryDAO category : groupDao.getCategories()) {
				if (!categories.contains(category)) {
					groupDao.getCategories().remove(category);
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
			logMe("Log Success created ["+code+":"+title);
		} else {
			logMe("Log Success updated ["+code+":"+title);
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
			Date openDate, Date closeDate, Date expiryDate, Date startDate, Date endDate,
			boolean bookable, int capacity, 
			String termCode,  String teachingComponentId, String termName,
			String teacherId, String teacherName, String teacherEmail,
			String sessionDates, String sessions, String location,
			Set<CourseGroupDAO> groups) {
		
		if (log.isDebugEnabled()) {
			System.out.println("XcriPopulatorImpl.validComponent ["+id+":"+title+":"+subject+":"+
				viewDate(openDate)+":"+viewDate(closeDate)+":"+viewDate(expiryDate)+":"+viewDate(startDate)+":"+viewDate(endDate)+":"+
				bookable+":"+capacity+":"+
				termCode+":"+teachingComponentId+":"+termName+":"+
				teacherId+":"+teacherName+":"+teacherEmail+":"+
				sessionDates+":"+sessions+":"+location+":"+
				groups.size()+"]");
		}
		
		int i=0;
		
		try {
			
			if (null == openDate) { 
				logMe("Log Failure ["+id+":"+"No open date set");
				i++;
			}
		
			if (null == closeDate) {
				logMe("Log Failure ["+id+":"+"No close date set");
				i++;
			}
		
			if (null != openDate && null != closeDate) {
				if (openDate.after(closeDate)){
					logMe("Log Failure ["+id+":"+"Open date is after close date");
					i++;
				}
			}
		
			if (null != expiryDate && null != closeDate) {
				if(expiryDate.before(closeDate)){
					logMe("Log Failure ["+id+":"+"Expiry date is before close date");
					i++;
				}
			}
		
			if (subject == null || subject.trim().length() == 0) {
				logMe("Log Failure ["+id+":"+"Subject isn't set.");
				i++;
			}
		
			if (title == null || title.trim().length() == 0) {
				logMe("Log Failure ["+id+":"+"Title isn't set.");
				i++;
			}
		
			if (termCode == null || termCode.trim().length() == 0) {
				logMe("Log Failure ["+id+":"+"Term code can't be empty");
				i++;
			}
		
			if (termName == null || termName.trim().length() == 0) {
				logMe("Log Failure ["+id+":"+"Term name can't be empty");
				i++;
			}
		
			if (teachingComponentId == null || teachingComponentId.trim().length()==0) {
				logMe("Log Failure ["+id+":"+"No teaching component ID found.");
				i++;
			}
		
			if (groups.isEmpty()) {
				logMe("Log Failure ["+id+":"+"No Assessment Unit codes.");
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
			Date openDate, Date closeDate, Date expiryDate, Date startDate, Date endDate,
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
			componentDao.setExpires(expiryDate);
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
			logMe("Log Success created ["+id+":"+subject);
		} else {
			logMe("Log Success updated ["+id+":"+subject);
		}
		return created;
	}
	

	private void logMe(String message) throws IOException {
		log.warn(message);
		writer.write(message+"\n");
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
	
	private String now() {
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    return sdf.format(cal.getTime());
	}
	
	protected String getSiteId() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("course-signup.site-id", "course-signup");
		}
		return "course-signup";
	}
	
	protected String getXcriURL() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.url", 
					"http://daisy-feed.socsci.ox.ac.uk/XCRI_course_feed.php");
		}
		return "http://daisy-feed.socsci.ox.ac.uk/XCRI_course_feed.php";
	}
	
	protected String getXcriAuthUser() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.auth.user", "sesuser");
		}
		return "sesuser";
	}
	
	protected String getXcriAuthPassword() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.auth.password", "blu3D0lph1n");
		}
		return "blu3D0lph1n";
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
	
	public static void main(String[] args) {
		try {	
			XcriPopulatorImpl reader = new XcriPopulatorImpl();
			
			reader.update();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
