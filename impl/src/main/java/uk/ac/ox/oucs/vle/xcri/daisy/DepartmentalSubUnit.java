package uk.ac.ox.oucs.vle.xcri.daisy;

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

import org.jdom.Element;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

public class DepartmentalSubUnit extends DaisyElement implements Extension {
	
	private String code;
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "departmentalSubUnit";
	}
	
	/**
	 * @return the identifier
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		this.setCode(element.getAttributeValue("code", DaisyNamespace.DAISY_NAMESPACE_NS));
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getCode() != null) {
			element.setAttribute("code", this.getCode(), getNamespace());
		}
		return element;
	}
	
}
