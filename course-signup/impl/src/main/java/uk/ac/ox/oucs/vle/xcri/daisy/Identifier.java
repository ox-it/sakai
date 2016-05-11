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
package uk.ac.ox.oucs.vle.xcri.daisy;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.xcri.Extension;
import org.xcri.Namespaces;
import org.xcri.exceptions.InvalidElementException;

public class Identifier extends org.xcri.common.Identifier implements Extension {
	
	private String type;
	
	/**
	 * @return the identifier
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		if (null != element.getAttribute("type", DaisyNamespace.DAISY_NAMESPACE_NS)) {
			this.setType(element.getAttributeValue("type", DaisyNamespace.DAISY_NAMESPACE_NS));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getType() != null) {
			element.setAttribute("type", this.getType(), DaisyNamespace.DAISY_NAMESPACE_NS);
		}
		return element;
	}
	
	public Namespace getNamespace() {
		return Namespaces.DC_NAMESPACE_NS;
	}
	
}
