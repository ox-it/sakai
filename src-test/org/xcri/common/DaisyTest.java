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
package org.xcri.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.xcri.Extension;
import org.xcri.core.Catalog;
import org.xcri.core.Course;
import org.xcri.core.Presentation;
import org.xcri.core.Provider;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.extensions.daisy.Bookable;
import org.xcri.extensions.daisy.CourseSubUnit;
import org.xcri.extensions.daisy.DaisyIdentifier;
import org.xcri.extensions.daisy.DepartmentThirdLevelApproval;
import org.xcri.extensions.daisy.DepartmentalSubUnit;
import org.xcri.extensions.daisy.Division;
import org.xcri.extensions.daisy.DivisionWideEmail;
import org.xcri.extensions.daisy.EmployeeEmail;
import org.xcri.extensions.daisy.EmployeeName;
import org.xcri.extensions.daisy.ModuleApproval;
import org.xcri.extensions.daisy.PublicView;
import org.xcri.extensions.daisy.Sessions;
import org.xcri.extensions.daisy.SupervisorApproval;
import org.xcri.extensions.daisy.TermCode;
import org.xcri.extensions.daisy.TermLabel;
import org.xcri.extensions.daisy.WebAuthCode;
import org.xcri.extensions.oxcap.BookingEndpoint;
import org.xcri.extensions.oxcap.MemberApplyTo;
import org.xcri.extensions.oxcap.OxcapCourse;
import org.xcri.extensions.oxcap.OxcapPresentation;
import org.xcri.extensions.oxcap.Session;

public class DaisyTest extends TestCase {
	
	Catalog catalog;
	SAXBuilder builder;
	
	protected void setUp() throws JDOMException, IOException, InvalidElementException {
		
		// Daisy Extensions
		ExtensionManager.registerExtension(new WebAuthCode());
		ExtensionManager.registerExtension(new DepartmentalSubUnit());
		ExtensionManager.registerExtension(new DepartmentThirdLevelApproval());
		ExtensionManager.registerExtension(new Division());
		ExtensionManager.registerExtension(new DivisionWideEmail());
		ExtensionManager.registerExtension(new CourseSubUnit());
		ExtensionManager.registerExtension(new PublicView());
		ExtensionManager.registerExtension(new ModuleApproval());
		ExtensionManager.registerExtension(new SupervisorApproval());
		ExtensionManager.registerExtension(new Sessions());
		ExtensionManager.registerExtension(new Bookable());
		ExtensionManager.registerExtension(new TermCode());
		ExtensionManager.registerExtension(new TermLabel());
		ExtensionManager.registerExtension(new EmployeeName());
		ExtensionManager.registerExtension(new EmployeeEmail());
		ExtensionManager.registerExtension(new DaisyIdentifier());
		// Oxcap Extensions
		ExtensionManager.registerExtension(new MemberApplyTo());
		ExtensionManager.registerExtension(new BookingEndpoint());
		ExtensionManager.registerExtension(new Session());
		
		OverrideManager.registerOverride(Course.class, new OxcapCourse());
		OverrideManager.registerOverride(Presentation.class, new OxcapPresentation());
		
		catalog = new Catalog();
		builder = new SAXBuilder();
		InputStream inStream = new java.io.FileInputStream(new File("src-test/XCRI_OXCAP_beta4.xml"));
		Document document = builder.build(inStream);
		catalog.fromXml(document);
	}
	
	protected void tearDown() {
		ExtensionManager.clear();
		OverrideManager.clear();
	}
	
	@Test
	public void testProvider() {
		
		Provider[] providers = catalog.getProviders();
		
		for (Provider provider : providers) {
			
			assertNotNull(provider.getTitles()[0].getValue());
			assertNotNull(provider.getIdentifiers()[0].getValue());
		
			Collection<String> divisionSuperUsers = new HashSet<String>();
			Map<String, String> subunits = new HashMap<String, String>();
		
			for (Extension extension : provider.getExtensions()) {
			
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
					subunits.put(subUnit.getCode(), subUnit.getValue());
				}
			}
			assertTrue("No Superusers on ["+provider.getTitles()[0].getValue()+"]", !divisionSuperUsers.isEmpty());
			assertTrue("No SubUnits on ["+provider.getTitles()[0].getValue()+"]", !subunits.isEmpty());
		}
		
	}
	
	@Test
	public void testCourse() {
		
		Provider[] providers = catalog.getProviders();
		for (Provider provider : providers) {
			Course[] courses = provider.getCourses();
			for (Course course : courses) {
			
				assertTrue (course instanceof OxcapCourse);
				
				String assessmentunitCode = null;
				String teachingcomponentId = null;
				Collection<String> administrators = new HashSet<String>();
				Subject[] subjects = course.getSubjects();
			
				for (Extension extension : course.getExtensions()) {
					
					if (extension instanceof DaisyIdentifier) {
						DaisyIdentifier identifier = (DaisyIdentifier) extension;
						if ("assessmentUnitCode".equals(identifier.getType())) {
							assessmentunitCode = identifier.getValue();
							continue;
						}
						if ("teachingComponentId".equals(identifier.getType())) {
							teachingcomponentId = identifier.getValue();
							continue;
						}
					}
				
					if (extension instanceof WebAuthCode) {
						WebAuthCode webAuthCode = (WebAuthCode) extension;
					
						if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.administrator) {
							administrators.add(webAuthCode.getValue());
							continue;
						}
					}
					
					System.out.println("Extension not processed ["+extension.getClass().getName()+":"+assessmentunitCode+"]");
				
				}
				
				if (!"3C11AE0001".equals(assessmentunitCode)) {
					continue;
				}
				
				for (Subject subject : subjects) {
					System.out.println("Subject ["+subject.getIdentifier()+":"+subject.getType()+":"+subject.getValue()+"]");
				}
				assertEquals("Field Research Methods Class (PRS, 2nd yr M.Phil., VA)", course.getTitles()[0].getValue());
				assertNotNull("AssessmentUnitCode is null ["+provider.getTitles()[0]+"]", assessmentunitCode);
				assertNotNull("TeachingComponentId is null ["+assessmentunitCode+"]", teachingcomponentId);
				assertTrue("No Administrators on ["+assessmentunitCode+"]", !administrators.isEmpty());
			}
		}
	}
	
	@Test
	public void testPresentation() {
		
		Provider[] providers = catalog.getProviders();
		for (Provider provider : providers) {
			Course[] courses = provider.getCourses();
			for (Course course : courses) {
				Presentation[] presentations = course.getPresentations();
				for (Presentation presentation : presentations) {
					
					assertTrue(presentation instanceof OxcapPresentation);
					
					//String id = presentation.getIdentifiers()[0].getValue();
					String subject = presentation.getTitles()[0].getValue();
					String title = presentation.getAttendanceMode().getValue();
					String slot = presentation.getAttendancePattern().getValue();
					String applyTo = presentation.getApplyTo().getValue();
					
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
					
					String memberApplyTo = null;
					String bookingEndpoint = null;
					
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
							sessions = extension.getValue();
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
						
						if (extension instanceof DaisyIdentifier) {
							continue;
						}
						
						if (extension instanceof Session) {
							assertNotNull(extension.getValue());
							continue;
						}
						
						System.out.println("Extension not processed ["+extension.getClass().getName()+":"+id+"]");
					}
					
					if (!"755".equals(id)) {
						continue;
					}
					
					assertEquals("Field Research Methods Class (PRS, 2nd yr M.Phil., VA", title);
					
					assertNotNull(id);
					assertNotNull("Subject null on ["+id+"]", subject);
					assertNotNull("Title null on ["+id+"]", title);
					assertNotNull("Slot null on ["+id+"]", slot);
					
					//assertNotNull(startDate);
					//assertNotNull(endDate);
					assertNotNull("Open date null on ["+id+"]", openDate);
					assertNotNull("Close date null on ["+id+"]");
					
					assertTrue("no capacity on ["+id+"]", capacity>0);
					//assertNotNull("location null on ["+id+"]", location);
					assertNotNull("MemberApplyTo null on ["+id+"]", memberApplyTo);
					assertNotNull("ApplyTo null on ["+id+"]", applyTo);
					assertNotNull("TermCode null on ["+id+"]", termCode);
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
