/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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
 * Section is a container within a user interface.<br />
 * The section title is rendered, along with the components added to the section container.<br />
 * A section may be declared to reference a Collection of entities so that the section is repeated, in sequence, for each entity.
 */
public interface Section extends Container
{
	/** separator styles. */
	enum Separator
	{
		line, none, space
	};

	/**
	 * Set the section link anchor (internal page address)
	 * 
	 * @param selection
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	Section setAnchor(String selection, PropertyReference... references);

	/**
	 * Set the section contents as originally collapsed, click in the title to expand.
	 * 
	 * @param setting
	 *        if true, start out collapsed and be expandable.
	 * @return self.
	 */
	Section setCollapsed(boolean setting);

	/**
	 * Set the decision to include each entity (if the EntityReference is set to a Collection)
	 * 
	 * @param inclusionDecision
	 *        The decision for inclusion of each entity.
	 * @return self.
	 */
	Section setEntityIncluded(Decision inclusionDecision);

	/**
	 * Set a reference to an object to have the focus while rendering the children of this section.
	 * 
	 * @param reference
	 *        The reference to an entity to focus on.
	 * @return self.
	 */
	Section setFocus(PropertyReference reference);

	/**
	 * Set a reference to an array of Collection of entities to iterate over.<br />
	 * The section will be repeated for each entity. Each repeat will set additional entries in the context.
	 * 
	 * @param reference
	 *        The reference to an array or collection to iterate over.
	 * @param name
	 *        The context name for the current iteration item.
	 * @param empty
	 *        A message to display if the iterator is empty.
	 * @return self.
	 */
	Section setIterator(PropertyReference reference, String name, Message empty);

	/**
	 * Set the maximum height for the section. Any content larger will scroll.
	 * 
	 * @param maxHeight
	 *        The maximum height (0 will disable).
	 * @return self.
	 */
	Section setMaxHeight(int maxHeight);

	/**
	 * Set the section title message.
	 * 
	 * @param selection
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	Section setTitle(String selection, PropertyReference... references);

	/**
	 * Set the decision to highlight the title.
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to highlight the title.
	 * @return self.
	 */
	Section setTitleHighlighted(Decision... decision);

	/**
	 * Set the decision to include the title.
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to include the title.
	 * @return self.
	 */
	Section setTitleIncluded(Decision... decision);

	/**
	 * Set the treatment.
	 * 
	 * @param treatment
	 *        The section treatment.
	 * @return self.
	 */
	Section setTreatment(String treatment);
}
