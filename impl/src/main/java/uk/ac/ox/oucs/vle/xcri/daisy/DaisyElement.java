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
import org.jdom.Namespace;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.types.XcriElement;

public class DaisyElement extends XcriElement {
	
	/**
	 * @return the namespace
	 */
	@Override
	public Namespace getNamespace() {
		return DaisyNamespace.DAISY_NAMESPACE_NS;
	}
	
	/*
	 * 
	 */
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	public Element toXml() {
		return super.toXml();
	}
	
}
