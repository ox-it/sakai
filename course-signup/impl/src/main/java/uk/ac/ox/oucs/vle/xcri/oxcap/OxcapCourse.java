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
package uk.ac.ox.oucs.vle.xcri.oxcap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Element;
import org.xcri.core.Course;
import org.xcri.exceptions.InvalidElementException;

public class OxcapCourse extends Course {
	
	private Log log = LogFactory.getLog(OxcapCourse.class);
	
	private String statusCode;
	private String visible;
	
	public enum Visibility {
		PB,RS,PR;
	}
	
	public enum Status {
		AC,CN,DC;
	}
	
	
	/**
	 * @return
	 */
	public Visibility getVisibility(){
		try {
			return Visibility.valueOf(getVisible());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return visibility
	 */
	public Status getStatus() {
		try {
			return Status.valueOf(getStatusCode());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		
		this.setVisible(element.getAttributeValue("visibility", OxcapNamespace.OXCAP_NAMESPACE_NS));
		if (this.getVisibility() == null) {
			log.warn("OxcapCourse : visibility (\""+this.getVisible()+"\") is not a member of the recommended vocabulary");
		}
		
		this.setStatusCode(element.getAttributeValue("status", OxcapNamespace.OXCAP_NAMESPACE_NS));
		if (this.getStatus() == null) {
			log.warn("OxcapCourse : status (\""+this.getStatusCode()+"\") is not a member of the recommended vocabulary");
		}
	}
	
	/**
	 * @return the status
	 */
	private String getStatusCode() {
		if (null == this.statusCode) {
			return "AC";
		}
		return this.statusCode;
	}

	/**
	 * 
	 */
	private void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	/**
	 * @return the visibility
	 */
	private String getVisible() {
		if (null == this.visible) {
			return "PB";
		}
		return this.visible;
	}

	/**
	 * 
	 * @param visible
	 */
	private void setVisible(String visible) {
		this.visible = visible;
	}

}
