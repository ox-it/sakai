/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
 * UiEntityList presents a multi-column multi-item listing of entites from the model.
 */
public interface EntityList extends Component
{
	/** rendering styles. */
	enum Style
	{
		flat, form
	};

	/**
	 * Add a column to display some selector of each entity.
	 * 
	 * @param column
	 *        A column to display some selector of each entity.
	 */
	EntityList addColumn(EntityListColumn column);

	/**
	 * Add an entity action related to the list.
	 * 
	 * @param action
	 *        The entity action (navigation) to add.
	 * @return self.
	 */
	EntityList addEntityAction(Component action);

	/**
	 * Add a heading, based on this decision, and showing navigation.
	 * 
	 * @param decision
	 *        The heading decision.
	 * @param navigation
	 *        The navigation to make the header clickable (optional).
	 * @return self.
	 */
	EntityList addHeading(Decision decision, Navigation navigation);

	/**
	 * Add a heading, based on this decision, and showing the message from this selector and properties.
	 * 
	 * @param decision
	 *        The heading decision.
	 * @param selector
	 *        The message selector.
	 * @param properties
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	EntityList addHeading(Decision decision, String selector, PropertyReference... properties);

	/**
	 * Set the section link anchor (internal page address)
	 * 
	 * @param selection
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	EntityList setAnchor(String selection, PropertyReference... references);

	/**
	 * Set some entity rows, those that meet the decision criteria, to have a background color.
	 * 
	 * @param decision
	 *        The criteria for entity colorization.
	 * @param color
	 *        The color value.
	 * @return self.
	 */
	EntityList setColorize(Decision decision, String color);

	/**
	 * Set the message for text to display instead of the title if there are no items in the list.
	 * 
	 * @param selector
	 *        The message selector for text to display instead of the title if there are no items in the list.
	 * @param properties
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	EntityList setEmptyTitle(String selector, PropertyReference... properties);

	/**
	 * Set the decision to include each entity.
	 * 
	 * @param inclusionDecision
	 *        The decision for inclusion of each entity.
	 * @return self.
	 */
	EntityList setEntityIncluded(Decision inclusionDecision);

	/**
	 * Set the decision to include each entity at the row level - even if excluded here, the entity will be considered for the headings.
	 * 
	 * @param inclusionDecision
	 *        The decision for inclusion of each entity.
	 * @return self.
	 */
	EntityList setEntityRowIncluded(Decision inclusionDecision);

	/**
	 * Set the background color for heading rows.
	 * 
	 * @param color
	 *        The CSS color.
	 * @return self.
	 */
	EntityList setHeadingColor(String color);

	/**
	 * Set no padding for heading rows.
	 * 
	 * @return self.
	 */
	EntityList setHeadingNoPadding();

	/**
	 * Set a reference to an array of Collection of entities to iterate over for the rows.
	 * 
	 * @param reference
	 *        The reference to an array or collection to iterate over.
	 * @param name
	 *        The context name for the current item in the iteration.
	 * @return self.
	 */
	EntityList setIterator(PropertyReference reference, String name);

	/**
	 * Set the pager for the list.
	 * 
	 * @param pager
	 *        The pager for the list.
	 */
	EntityList setPager(Pager pager);

	/**
	 * Set the style.
	 * 
	 * @param style
	 *        The style.
	 * @return self.
	 */
	EntityList setStyle(Style style);

	/**
	 * Set the list title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param properties
	 *        one or more (or an array) of PropertyReferences to form the additional values in the formatted message.
	 */
	EntityList setTitle(String selector, PropertyReference... properties);

	/**
	 * Set the decision to include the title or not.
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to include the title.
	 * @return self.
	 */
	EntityList setTitleIncluded(Decision... decision);
}
