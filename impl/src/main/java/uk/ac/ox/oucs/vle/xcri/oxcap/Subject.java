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
import org.xcri.exceptions.InvalidElementException;

/**
 * This is our custom parsing of subjects as we have a limited vocabulary.
 */
public class Subject extends org.xcri.common.Subject implements Extension {

	private static final Log log = LogFactory.getLog(Subject.class);

	private Namespace categoryNamespace;
	private String identifier;

    /**
     * This is the namespace of the Researcher Definition Framework. A list of skills that you may gain from the
     * course.
     */
	public static final Namespace RDF = Namespace.getNamespace("https://data.ox.ac.uk/id/ox-rdf/");
    /**
     * This defines if the course is qualitative or quantitative.
     */
	public static final Namespace RM = Namespace.getNamespace("https://data.ox.ac.uk/id/ox-rm/");
    /**
     * This is the namespace for the JACS subjects which are recommended to be used by JISC, but we aren't
     * interested in them as they are aimed at classifying degree programmes which isn't applicable for us.
     */
	public static final Namespace JACS = Namespace.getNamespace("http://xcri.co.uk");

	public interface SubjectIdentifier {
		public String getValue();
	}

	public enum RDFSubjectIdentifier implements SubjectIdentifier {
		// RDS Domain A
		CO("Computing"),
		DA("Data Analysis"),
		DM("Data Management"),
		FW("Field Work"),
		RD("General Researcher Development"),
		IN("Information Skills"),
		LS("Laboratory Skills"),
		LA("Languages"),
		MM("Mathematical Methods"),
		RM("Research Methods"),
		ST("Statistics"),
		TE("Technical Skills"),
		SR("Study and Research Skills"),
		// RDS Domain B
		CD("Career Development"),
		PE("Personal Effectiveness"),
		PS("Presentation Skills"),
		// RDS Domain C
		ET("Ethics"),
		IP("Intellectual property skills"),
		IL("Interpersonal skills"),
		RF("Research and financial management"),
		HS("Safety"),
		// RDS Domain D
		CS("Communication skills"),
		EE("Enterprise and Entrepreneurs"),
		// Appears in 2 domains.
		// PS("Presentation skills"),
		SC("Seminars / Colloquia"),
		TA("Teaching & Academic skills");

		private final String value;

		RDFSubjectIdentifier(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum RMSubjectIdentifier implements SubjectIdentifier {
		QL("Qualitative"),
		QN("quantitative");

		private final String value;

		RMSubjectIdentifier(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}
	
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);

		String identifier = element.getAttributeValue("identifier");
		if (identifier != null){
			this.setIdentifier(identifier);
		}
		
		//<dc:subject xmlns:ns="https://data.ox.ac.uk/id/ox-RDF/" xsi:type="ns:notation" identifier="CD">Career Development</dc:subject>
	      
		for (Object object : element.getAttributes()) {
			Attribute attribute = (Attribute) object;
			if ("type".equals(attribute.getName()) && "xsi".equals(attribute.getNamespacePrefix())) {
				String[] bits = attribute.getValue().split(":");
				this.setCategoryNamespace(element.getNamespace(bits[0]));
			}
		}
	}

	public boolean isValid() {
		return isJACSCategory() || isRMCategory() || isRDFCategory();
	}
	
	public boolean isJACSCategory() {
		return JACS.equals(this.getCategoryNamespace());
	}
	
	public boolean isRMCategory() {
		return RM.equals(this.getCategoryNamespace());
	}
	
	public boolean isRDFCategory() {
		return RDF.equals(this.getCategoryNamespace());
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
