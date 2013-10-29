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

import org.jdom.Element;
import org.jdom.Namespace;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

/**
 * This is our custom parsing of subjects as we have a limited vocabulary.
 *
 */
public class Subject extends org.xcri.common.Subject implements Extension {

	// Parses tags link
	// <dc:subject xmlns:ns="https://data.ox.ac.uk/id/ox-RDF/" xsi:type="ns:notation" identifier="CD">Career Development</dc:subject>

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

	/**
	 * At the moment this is the made up names for the vitae skills. We don't currently import them
	 * but this is here for completeness.
	 */
	public static final Namespace VITAE = Namespace.getNamespace("http://vitae.ac.uk");

	/**
	 * The standard XML Schema namespace.
	 */
	public static final Namespace XSI = Namespace.getNamespace("http://www.w3.org/2001/XMLSchema-instance");

	/**
	 * A subject identifier.
	 * We have this interface because enums can't extend other enums.
	 */
	public interface SubjectIdentifier {
		/**
		 * @return The value of this subject identifier. Typically the title.
		 */
		public String getValue();

		/**
		 * @return The name of this identifier. Typically it's the unique ID.
		 */
		public String name();
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

		@Override
		public String toString() {
			return name()+ "("+ value+ ")";
		}
	}

	public enum RMSubjectIdentifier implements SubjectIdentifier {
		QL("Qualitative"),
		QN("Quantitative");

		private final String value;

		RMSubjectIdentifier(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return name()+ "("+ value+ ")";
		}
	}

	/**
	 * This contains both the Vitae Domain and Sub Domain skills.
	 */
	public enum VITAESubjectIdentifier implements SubjectIdentifier {
		A("Knowledge and intellectual abilities"),
		A1("Knowledge base"),
		A2("Cognitive abilities"),
		A3("Creativity"),
		B("Personal effectiveness"),
		B1("Personal qualities"),
		B2("Self-management"),
		B3("Professional and career development"),
		C("Research governance and organisation"),
		C1("Professional conduct"),
		C2("Research management"),
		C3("Finance, funding and resources"),
		D("Engagement, influence and impact"),
		D1("Working with others"),
		D2("Communication and dissemination"),
		D3("Engagement and impact");

		private final String value;

		VITAESubjectIdentifier(String value) {
			this.value = value;
		}

		public String getValue() {
			// We want the code prefixed for the VITAE skills.
			return name()+ " "+ this.value;
		}

		@Override
		public String toString() {
			return name()+ "("+ value+ ")";
		}
	}

	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		String identifier = element.getAttributeValue("identifier");
		if (identifier != null){
			this.setIdentifier(identifier);
		}
		String value = element.getAttributeValue("type", XSI);
		if (value != null) {
			String[] bits = value.split(":");
			if (bits.length == 2) {
				this.setCategoryNamespace(element.getNamespace(bits[0]));
			}
		}
	}

	public boolean isValid() {
		return isJACSCategory() || isRMCategory() || isRDFCategory() || isVITAECategory();
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

	public boolean isVITAECategory() {
		return VITAE.equals(this.getCategoryNamespace());
	}

	/**
	 * Gets the subject identifier based on the identifier of this element.
	 * It doesn't use the value from the XML element, but our static definition.
	 * @return One of the known subject identifier or <code>null</code> if no match was found.
	 */
	public SubjectIdentifier getSubjectIdentifier() {
		try {
			if (isRMCategory()) {
				return RMSubjectIdentifier.valueOf(getIdentifier());
			}
			if (isRDFCategory()) {
				return RDFSubjectIdentifier.valueOf(getIdentifier());
			}
			if (isVITAECategory() ) {
				return VITAESubjectIdentifier.valueOf(getIdentifier());
			}
		} catch (IllegalArgumentException iae) {
		}
		return null;
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
	 * @param categoryNamespace Set the namespace of the element,
	 */
	public void setCategoryNamespace(Namespace categoryNamespace) {
		this.categoryNamespace = categoryNamespace;
	}

	@Override
	public String toString() {
		return "Subject{" +
				"categoryNamespace=" + categoryNamespace +
				", identifier='" + identifier + '\'' +
				'}';
	}
}
