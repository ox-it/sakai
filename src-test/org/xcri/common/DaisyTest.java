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

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.xcri.Extension;
import org.xcri.core.Catalog;
import org.xcri.core.Course;
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
import org.xcri.extensions.daisy.ExpiryDate;
import org.xcri.extensions.daisy.ModuleApproval;
import org.xcri.extensions.daisy.PublicView;
import org.xcri.extensions.daisy.Sessions;
import org.xcri.extensions.daisy.SupervisorApproval;
import org.xcri.extensions.daisy.TermCode;
import org.xcri.extensions.daisy.TermLabel;
import org.xcri.extensions.daisy.WebAuthCode;

public class DaisyTest {
	
	/**
	 * The content of a Descriptive Text Element MUST be one of either:
	 * Empty
	 * Plain unescaped text content
	 * Valid XHTML 1.0 content
	 */
	@Test
	public void daisyContent() throws JDOMException, IOException, InvalidElementException{
		
		ExtensionManager.registerExtension(new WebAuthCode());
		
		ExtensionManager.registerExtension(new DepartmentalSubUnit());
		ExtensionManager.registerExtension(new DepartmentThirdLevelApproval());
		ExtensionManager.registerExtension(new Division());
		ExtensionManager.registerExtension(new DivisionWideEmail());
		ExtensionManager.registerExtension(new CourseSubUnit());
		ExtensionManager.registerExtension(new PublicView());
		ExtensionManager.registerExtension(new ModuleApproval());
		ExtensionManager.registerExtension(new SupervisorApproval());
		ExtensionManager.registerExtension(new ExpiryDate());
		ExtensionManager.registerExtension(new Sessions());
		ExtensionManager.registerExtension(new Bookable());
		ExtensionManager.registerExtension(new TermCode());
		ExtensionManager.registerExtension(new TermLabel());
		ExtensionManager.registerExtension(new EmployeeName());
		ExtensionManager.registerExtension(new EmployeeEmail());
		ExtensionManager.registerExtension(new DaisyIdentifier());
		
		Catalog catalog = new Catalog();
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new File("src-test/daisy.xml"));
		catalog.fromXml(document);
		
		Provider provider = catalog.getProviders()[0];
		System.out.println("["+provider.getExtensions().length+"]");
		
		System.out.println("["+provider.getIdentifiers()[0].getValue()+"]");
		System.out.println("["+provider.getTitles()[0].getValue()+"]");
		
		for (Extension extension : provider.getExtensions()) {
			if (extension instanceof Division) {
				Division division = (Division) extension;
				System.out.println("["+division.getValue()+":"+division.getCode()+"]");
			}
		}
		
		for (Course course : provider.getCourses()) {
			for (Extension extension : course.getExtensions()) {
				if (extension instanceof DaisyIdentifier) {
					DaisyIdentifier identifier = (DaisyIdentifier) extension;
					System.out.println("["+identifier.toString()+"]");
				}
			}
		}
		
		//assertEquals(content, catalog.getProviders()[0].getDescriptions()[0].getValue());
		//assertNotNull(catalog.getProviders()[0].getDescriptions()[0].toXml().getChild("div", Namespaces.XHTML_NAMESPACE_NS));
	}
}
