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

package uk.ac.ox.oucs.vle.xcri.oxcap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.xcri.Extension;
import org.xcri.Namespaces;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.presentation.AttendanceMode;
import org.xcri.presentation.AttendanceMode.AttendanceModeType;
import org.xcri.types.XcriElement;

public class Subject extends org.xcri.common.Subject implements Extension {
	
	private Log log = LogFactory.getLog(Subject.class);
	
	private Namespace categoryNamespace;
	private String identifier;
	
	private static String rdf = "https://data.ox.ac.uk/id/ox-rdf/";
	private static String rm = "https://data.ox.ac.uk/id/ox-rm/";
	private static String jacs = "http://xcri.co.uk";
	
	public enum SubjectIdentifier {
		CO,	CD,	CS, DA,	DM,	EE, ET, FW,	GT,	HS, IN,	IP, IL, LS,	MM,	PE,	PS,	QL,	QN,	RD,	RF, RM,	SC, SR,	ST,	TA, TE;
	}
	
	public SubjectIdentifier getSubjectIdentifier(){
		try {
			return SubjectIdentifier.valueOf(getIdentifier());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);

		/**
		 * Recommended Values: Producers SHOULD use the following values for this element, with the two-letter code used in the @identifier attribute, and the label in the element content:
		 * The value of this element MUST be one of:
		 * CM Campus
		 * DA Distance with attendance
		 * DS Distance without attendance
		 * NC Face-to-face non-campus
		 * MM Mixed mode
		 * ON Online (no attendance)
		 * WB Work-based
		 */
		
		String identifier = element.getAttributeValue("identifier");
		if (identifier != null){
			this.setIdentifier(identifier);
			if (this.getSubjectIdentifier() == null){
				log.warn("Subject : identifier (\""+identifier+"\") is not a member of the recommended vocabulary");
			}
		}
		
		//<dc:subject xmlns:ns="https://data.ox.ac.uk/id/ox-rdf/" xsi:type="ns:notation" identifier="CD">Career Development</dc:subject>
	      
		for (Object object : element.getAttributes()) {
			Attribute attribute = (Attribute) object;
			if ("type".equals(attribute.getName()) && "xsi".equals(attribute.getNamespacePrefix())) {
				String[] bits = attribute.getValue().split(":");
				this.setCategoryNamespace(element.getNamespace(bits[0]));
				if (!rdf.equals(this.getCategoryNamespace().getURI()) && 
					!rm.equals(this.getCategoryNamespace().getURI()) &&
					!jacs.equals(this.getCategoryNamespace().getURI())) {
					log.warn("Subject : categoryNamespace (\""+categoryNamespace+"\") is not a member of the recommended vocabulary");
				}
			}
		}
			
	}
	
	public boolean isJACSCategory() {
		if (null == this.getCategoryNamespace()) {
			return false;
		}
		return jacs.equals(this.getCategoryNamespace().getURI());
	}
	
	public boolean isRMCategory() {
		if (null == this.getCategoryNamespace()) {
			return false;
		}
		return rm.equals(this.getCategoryNamespace().getURI());
	}
	
	public boolean isRDFCategory() {
		if (null == this.getCategoryNamespace()) {
			return false;
		}
		return rdf.equals(this.getCategoryNamespace().getURI());
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	
	/**
	 * @return the identifier
	 */
	public Namespace getCategoryNamespace() {
		return categoryNamespace;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setCategoryNamespace(Namespace categoryNamespace) {
		this.categoryNamespace = categoryNamespace;
	}

}
