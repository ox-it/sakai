package uk.ac.ox.oucs.vle.xcri.oxcap;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.core.Presentation;
import org.xcri.exceptions.InvalidElementException;

public class OxcapPresentation extends Presentation {
	
	private Log log = LogFactory.getLog(OxcapPresentation.class);
	
	private String identifier;
	
	public enum Status {
		AC,CN,DC;
	}
	
	/**
	 * @return the identifier
	 */
	private String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @param status the status to set
	 */
	private void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return status
	 */
	public Status getStatus(){
		try {
			return Status.valueOf(getIdentifier());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		
		this.setIdentifier(element.getAttributeValue("status", OxcapNamespace.OXCAP_NAMESPACE_NS));
		if (this.getStatus() == null) {
			log.warn("OxcapCourse : status (\""+this.getIdentifier()+"\") is not a member of the recommended vocabulary");
		}
	}

}
