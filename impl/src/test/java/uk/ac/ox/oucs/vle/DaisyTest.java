/**
 * Copyright (c) 2011 University of Bolton
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, 
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * The above copyright notice and this permission notice shall be included in all copies 
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package uk.ac.ox.oucs.vle;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
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
import uk.ac.ox.oucs.vle.xcri.oxcap.BookingEndpoint;
import uk.ac.ox.oucs.vle.xcri.oxcap.MemberApplyTo;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapCourse;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapPresentation;
import uk.ac.ox.oucs.vle.xcri.oxcap.Session;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject;
public class DaisyTest extends TestCase {
	
	Catalog catalog;
	SAXBuilder builder;
	
	protected void setUp() throws JDOMException, IOException, InvalidElementException {
		
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
		
		// Careers
		//URL url = new URL("https://course.data.ox.ac.uk/catalogues/?uri=https%3A//course.data.ox.ac.uk/id/careers/catalogue&format=xcricap");
		
		// Continuing Education
		//URL url = new URL("https://course.data.ox.ac.uk/catalogues/?uri=http%3A//course.data.ox.ac.uk/id/continuing-education/catalog&format=xcricap");
		
		// ITLP
		URL url = new URL("https://course.data.ox.ac.uk/catalogues/?uri=https%3A//course.data.ox.ac.uk/id/itlp/catalogue&format=xcricap");
		
		// Language Centre
		//URL url = new URL("https://course.data.ox.ac.uk/catalogues/?uri=https%3A//course.data.ox.ac.uk/id/language-centre/catalogue&format=xcricap");
		
		// Medical Sciences
		//URL url = new URL("https://course.data.ox.ac.uk/catalogues/?uri=https%3A//course.data.ox.ac.uk/id/medsci/catalogue&format=xcricap-full");
		
		// SharePoint
		//URL url = new URL("https://course.data.ox.ac.uk/catalogues/?uri=https%3A//course.data.ox.ac.uk/id/sharepoint/catalogue&format=xcricap-full");
		  
		InputStream inStream = url.openStream();
		
        BufferedReader in = new BufferedReader(
        		new InputStreamReader(url.openStream()));

        String inputLine;
        OutputStream out = new java.io.FileOutputStream(new File("xcri.xml"));
        while ((inputLine = in.readLine()) != null) {
            out.write(inputLine.getBytes());
            out.write('\n');
        }
        in.close();
        
        // Daisy
        //InputStream inStream = getClass().getResourceAsStream("/XCRI_OXCAP.xml");
        
		catalog = new Catalog();
		builder = new SAXBuilder();
		Document document = builder.build(inStream);
		catalog.fromXml(document);
	}
	
	protected void tearDown() {
		ExtensionManager.clear();
		OverrideManager.clear();
	}
	
	
	public void testProvider() {
		
		Provider[] providers = catalog.getProviders();
		
		for (Provider provider : providers) {
			
			String departmentName = null;
			if (provider.getTitles().length > 0) {
				departmentName = provider.getTitles()[0].getValue();
			}
			
			String id = null;
			String divisionCode = null;
			
			Collection<String> divisionSuperUsers = new HashSet<String>();
			Collection<String> subunits = new HashSet<String>();
		
			for (Extension extension : provider.getExtensions()) {
			
				if (extension instanceof Identifier) {
					Identifier identifier = (Identifier) extension;
					if (XcriOxCapPopulatorImpl.typeProviderId(identifier.getType())) {
						id = identifier.getValue();
						continue;
					}
					
					if (XcriOxCapPopulatorImpl.typeProviderDivision(identifier.getType())) {
						divisionCode = identifier.getValue();
						continue;
					}
				}
				
				if (extension instanceof DepartmentThirdLevelApproval) {
					assertEquals(false, parseBoolean(extension.getValue()));
				}
			
				if (extension instanceof WebAuthCode) {
					WebAuthCode webAuthCode = (WebAuthCode) extension;
				
					if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.superUser) {
						divisionSuperUsers.add(webAuthCode.getValue());
					}
				}
			
				if (extension instanceof DepartmentalSubUnit) {
					DepartmentalSubUnit subUnit = (DepartmentalSubUnit) extension;
					subunits.add(subUnit.getValue());
				}
			}
			
		}
		
	}
	

	
	public void testCourse() {
		
		Provider[] providers = catalog.getProviders();
		for (Provider provider : providers) {
			
			String departmentName = null;
			if (provider.getTitles().length > 0) {
				departmentName = provider.getTitles()[0].getValue();
			}
			String departmentCode = null;
			String divisionEmail = null;
			Collection<String> divisionSuperUsers = new HashSet<String>();
			
			for (Extension extension : provider.getExtensions()) {
				
				if (extension instanceof Identifier) {
					Identifier identifier = (Identifier) extension;
					if (XcriOxCapPopulatorImpl.typeProviderId(identifier.getType())) {
						departmentCode = identifier.getValue();
						continue;
					}
				}
				
				if (extension instanceof DivisionWideEmail) {
					divisionEmail = extension.getValue();
					continue;
				}
				
				if (extension instanceof WebAuthCode) {
					WebAuthCode webAuthCode = (WebAuthCode) extension;
					
					if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.superUser) {
						divisionSuperUsers.add(webAuthCode.getValue());
					}
					continue;
				}
			}
			
			Course[] courses = provider.getCourses();
			for (Course course : courses) {
			
				assertTrue (course instanceof OxcapCourse);
				
				OxcapCourse myCourse = (OxcapCourse)course;
				String visibility = myCourse.getVisibility().toString();
				
				String description;
				if (course.getDescriptions().length > 0) {
					Description xDescription = course.getDescriptions()[0];
					if (xDescription.isXhtml()) {
						description = xDescription.getValue();
					} else {
						description = XcriOxCapPopulatorImpl.parse(xDescription.getValue());
					}
				}
				
				String regulations = course.getRegulations()[0].getValue();
				
				String id = null;
				String teachingcomponentId = null;
				Collection<String> administrators = new HashSet<String>();
				Collection<Subject> subjects = new HashSet<Subject>();
				String subunitCode = null;
				String subunitName = null;
				boolean supervisorApproval = true;
				boolean administratorApproval = true;
				Collection<String> otherDepartments = new HashSet<String>();
				
				Collection<Subject> researchCategories = new HashSet<Subject>();
				Collection<Subject> skillsCategories = new HashSet<Subject>();
				Collection<Subject> jacsCategories = new HashSet<Subject>();
			
				for (Extension extension : course.getExtensions()) {
					
					if (extension instanceof Identifier) {
						Identifier identifier = (Identifier) extension;
						if (XcriOxCapPopulatorImpl.typeCourseId(identifier.getType())) {
							id = identifier.getValue();
							continue;
						}
						if ("teachingComponentId".equals(identifier.getType())) {
							teachingcomponentId = identifier.getValue();
							continue;
						}
					}
					
					if (extension instanceof CourseSubUnit) {
						CourseSubUnit subUnit = (CourseSubUnit)extension;
						subunitCode = subUnit.getCode();
						subunitName = subUnit.getValue();
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
				
					if (extension instanceof WebAuthCode) {
						WebAuthCode webAuthCode = (WebAuthCode) extension;
					
						if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.administrator) {
							administrators.add(webAuthCode.getValue());
							continue;
						}
					}
					
					if (extension instanceof OtherDepartment) {
						otherDepartments.add(extension.getValue());
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
					
					System.out.println("Extension not processed ["+extension.getClass().getName()+":"+id+"]");
				
				}
				
				if ("4D02DN0005".equals(id)) {
					
						Description xDescription = course.getDescriptions()[0];
						if (xDescription.isXhtml()) {
							description = xDescription.getValue();
						} else {
							description = XcriOxCapPopulatorImpl.parse(xDescription.getValue());
						}
					
				}
				
				assertNotNull(id);
				
			}
		}
	}
	
	
	public void testPresentation() {
		
		Provider[] providers = catalog.getProviders();
		for (Provider provider : providers) {
			Course[] courses = provider.getCourses();
			for (Course course : courses) {
				
				String courseId = null;
				String teachingcomponentId = null;
				for (Extension extension : course.getExtensions()) {
					
					if (extension instanceof Identifier) {
						Identifier identifier = (Identifier) extension;
						if (XcriOxCapPopulatorImpl.typeCourseId(identifier.getType())) {
							courseId = identifier.getValue();
						}
						if ("teachingComponentId".equals(identifier.getType())) {
							teachingcomponentId = identifier.getValue();
						}
					}
				}
						
				Presentation[] presentations = course.getPresentations();
				for (Presentation presentation : presentations) {
					
					assertTrue(presentation instanceof OxcapPresentation);
					
					String title = presentation.getTitles()[0].getValue();
					
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
					
					String applyTo = null;
					if (null != presentation.getApplyTo()) {
						applyTo = presentation.getApplyTo().getValue();
					}
					
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
					if (null != presentation.getPlaces() &&
						!presentation.getPlaces().getValue().isEmpty()) {
						try {
							capacity = Integer.parseInt(presentation.getPlaces().getValue());
						} catch (Exception e) {
						}
					}
					if (0 != presentation.getVenues().length) {
						location = presentation.getVenues()[0].getProvider().getTitles()[0].getValue();
					}
					
					boolean bookable = false;
					
					String componentId = null;
					String uri = null;
					String teacherId = null;
					String teacherName = null;
					String teacherEmail = null;
					String sessionCount = null;
					String termCode = null;
					String sessionDates = null;
					
					String memberApplyTo = null;
					String bookingEndpoint = null;
					Set<Session> sessions = new HashSet<Session>();
					
					for (Extension extension : presentation.getExtensions()) {
						
						if (extension instanceof Identifier) {
							Identifier identifier = (Identifier) extension;
							if ("presentationURI".equals(identifier.getType())) {
								uri = identifier.getValue();
								continue;
							}
							if (XcriOxCapPopulatorImpl.typePresentationId(identifier.getType())) {
								componentId = identifier.getValue();
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
								continue;
							}
						}
						
						if (extension instanceof MemberApplyTo) {
							memberApplyTo = extension.getValue();
							continue;
						}
						
						if (extension instanceof BookingEndpoint) {
							bookingEndpoint = extension.getValue();
							continue;
						}
						
						if (extension instanceof Identifier) {
							continue;
						}
						
						if (extension instanceof Session) {
							Session session = (Session)extension;
							if (session.getIdentifiers().length > 0) {	
								sessions.add(session);
								continue;
							}
						}
						
						System.out.println("Extension not processed ["+extension.getClass().getName()+":"+componentId+"]");
					}
					
					Set<CourseGroupDAO> courseGroups = new HashSet<CourseGroupDAO>();
					CourseGroupDAO courseDao = new CourseGroupDAO();
					courseDao.setCourseId(courseId);
					courseGroups.add(courseDao);
					
					assertNotNull(componentId);
					
				}
			}
		}
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
}
