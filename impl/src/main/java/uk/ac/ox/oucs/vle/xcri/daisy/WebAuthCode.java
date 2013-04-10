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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

public class WebAuthCode extends DaisyElement implements Extension {
	
	private Log log = LogFactory.getLog(WebAuthCode.class);
	
	private String webAuthType;
	
	public enum WebAuthCodeType {
		superUser,
		administrator,
		presenter
	}
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "webAuthCode";
	}
	
	/**
	 * @return the identifier
	 */
	public WebAuthCodeType getWebAuthCodeType() {
		try {
			return WebAuthCodeType.valueOf(getWebAuthType());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param webauthtype the webauthtype to set
	 */
	public void setWebAuthType(String webAuthType) {
		this.webAuthType = webAuthType;
	}
	
	public String getWebAuthType() {
		return this.webAuthType;
	}
	
	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		this.setWebAuthType(element.getAttributeValue("type", DaisyNamespace.DAISY_NAMESPACE_NS));
		if (this.getWebAuthCodeType() == null) {
			log.warn("WebAuthCode : type (\""+this.getWebAuthType()+"\") is not a member of the recommended vocabulary");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getWebAuthCodeType() != null) {
			element.setAttribute("type", this.getWebAuthType(), DaisyNamespace.DAISY_NAMESPACE_NS);
		}
		return element;
	}

}
