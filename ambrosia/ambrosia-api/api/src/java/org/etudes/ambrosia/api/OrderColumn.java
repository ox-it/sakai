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
 * OrderColumn is a column of an EntityList that presents a control to allow the end-user to re-order the entities.
 */
public interface OrderColumn extends EntityListColumn
{
	/**
	 * Set the property reference for encoding / decoding.
	 * 
	 * @param propertyReference
	 *        The property reference for encoding / decoding.
	 */
	OrderColumn setProperty(PropertyReference propertyReference);

	/**
	 * Set the property reference for the value encoded into the column. This will be reported as the value of a row.
	 * 
	 * @param propertyReference
	 *        The property reference for the value encoded into the column.
	 * @return self.
	 */
	OrderColumn setValueProperty(PropertyReference propertyReference);
}
