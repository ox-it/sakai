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

import java.util.List;

/**
 * Container is the base class of all components that contain collections of other components.<br />
 * add() is called to populate the components that are contained withing.<br />
 * The components that are contained are rendered inside this container.
 */
public interface Container extends Component
{
	/**
	 * Add a component to the container.
	 * 
	 * @param component
	 *        the component to add.
	 */
	Container add(Component component);

	/**
	 * Find the contained components with this id.
	 * 
	 * @param id
	 *        The container id.
	 * @return The contained components with this id, or an empty list not found.
	 */
	List<Component> findComponents(String id);

	/**
	 * Access the contained components.
	 * 
	 * @return The contained components.
	 */
	List<Component> getContained();
}
