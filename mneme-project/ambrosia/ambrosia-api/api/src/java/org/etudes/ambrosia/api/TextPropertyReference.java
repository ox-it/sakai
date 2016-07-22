/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.ambrosia.api;

/**
 * TextPropertyReference handles plain text String selector values.
 */
public interface TextPropertyReference extends PropertyReference
{
	/**
	 * Set the max length to show - anything longer will be truncated. If truncated, "..." is appended.
	 * 
	 * @param maxChars
	 *        The maximum number of characters to show.
	 * @return self.
	 */
	TextPropertyReference setMaxLength(int maxChars);

	/**
	 * Set this to strip any html formatting and show just plain text.
	 * 
	 * @return self.
	 */
	TextPropertyReference setStripHtml();
}
