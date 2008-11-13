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
 * DatePropertyReference handles Date objects formatted in the standard way.
 */
public interface DatePropertyReference extends PropertyReference
{
	/**
	 * Set to use the java "SHORT" format, instead of the default "MEDIUM" format.
	 * 
	 * @return self.
	 */
	DatePropertyReference setShort();

	/**
	 * Set the format to be two lines: date on top, time below (assumes HTML formatting)
	 * 
	 * @return self.
	 */
	DatePropertyReference setTwoLine();
}
