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

import org.jdom2.Namespace;

public class DaisyNamespace {

	public static final String DAISY_NAMESPACE = "http://daisy.socsci.ox.ac.uk/ns/";
	
	public static final Namespace DAISY_NAMESPACE_NS = Namespace.getNamespace("daisy",DAISY_NAMESPACE);

}
