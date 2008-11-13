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

import java.util.Set;

/**
 * PopulatingSet is a set that will populate on each get based on Entities with an id.
 */
public interface PopulatingSet
{
	/**
	 * Some object that will find objects based on the id.
	 */
	public interface Factory
	{
		Object get(String id);
	}

	/**
	 * Some object that will provide an id for the object.
	 */
	public interface Id
	{
		String getId(Object o);
	}

	/**
	 * Assure that an object with this id is in the set, going to the factory if needed.
	 * 
	 * @param id
	 *        The object's id
	 * @return The object, or null if it cannot be found or provided by the factory.
	 */
	Object assure(String id);

	/**
	 * Access the set of actual members.
	 * 
	 * @return The set.
	 */
	Set getSet();
}
