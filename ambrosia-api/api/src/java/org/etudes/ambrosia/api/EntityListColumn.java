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

import java.util.List;

/**
 * UiColumn is the interface for columns added to a UiEntityList display
 */
public interface EntityListColumn
{
	/**
	 * Add a component to the column.
	 * 
	 * @param component
	 *        the component to add.
	 */
	EntityListColumn add(Component component);

	/**
	 * Add an entity action related to the column.
	 * 
	 * @param action
	 *        The entity action (navigation) to add.
	 * @return self.
	 */
	EntityListColumn addEntityAction(Component action);

	/**
	 * Add a navigation to use for the main text of the column. If multiple defined, the first enabled included non-null one wins.
	 * 
	 * @param navigation
	 *        The navigation to use for the main text of the column.
	 * @return self.
	 */
	EntityListColumn addEntityNavigation(Navigation navigation);

	/**
	 * Add a footnote to the column.
	 * 
	 * @param footnote
	 *        The footnote to add to the column.
	 */
	EntityListColumn addFootnote(Footnote footnote);

	/**
	 * Add a navigation to include in the column.
	 * 
	 * @param navigation
	 *        The navigation to add.
	 * @return self.
	 */
	EntityListColumn addNavigation(Component navigation);

	/**
	 * Check if the column entry for this entity is to be shown as an alert.
	 * 
	 * @param context
	 *        The UiContext
	 * @param focus
	 *        The entity object focus.
	 * @return true if the column is an alert, false if not.
	 */
	boolean alert(Context context, Object focus);

	/**
	 * Access the bottomed setting.
	 * 
	 * @return the bottomed setting.
	 */
	boolean getBottomed();

	/**
	 * Access the centered setting.
	 * 
	 * @return the centered setting.
	 */
	boolean getCentered();

	/**
	 * Get the text that will be used to display the contents of this column, in the final display technology format.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param entity
	 *        The entity to read.
	 * @param row
	 *        The current row number of the list.
	 * @param id
	 *        An ID value that can be used, unique to the entity list / column.
	 * @param size
	 *        The total number of entites.
	 * @return The the text for display.
	 */
	String getDisplayText(Context context, Object entity, int row, String id, int size);

	/**
	 * Access the entity actions list.
	 * 
	 * @return The entity actions defined related to this column.
	 */
	List<Component> getEntityActions();

	/**
	 * Find the first defined navigation to be included, enabled, and with a non-null destination; return the destination.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The focus.
	 * @return The effective entity navigation destination, or null if there is none.
	 */
	String getEntityNavigationDestination(Context context, Object focus);

	/**
	 * Access the submit setting on the entity navigation.
	 * 
	 * @return The submit setting on the entity navigation.
	 */
	boolean getEntityNavigationSubmit();

	/**
	 * Access the list of footnotes.
	 * 
	 * @return The list of footnotes.
	 */
	List<Footnote> getFootnotes();

	/**
	 * Check if the entity is included for display in this column.
	 * 
	 * @param context
	 *        The UiContext
	 * @return true the entity is included for display in this column, false if not.
	 */
	boolean getIsEntityIncluded(Context context, Object focus);

	/**
	 * Access the no-wrap flag - set to true if the column should be set to no-wrap.
	 * 
	 * @return true if the column should be set to no-wrap, false if not.
	 */
	boolean getIsNoWrap();

	/**
	 * Access the navigation list.
	 * 
	 * @return The navigations defined for this column.
	 */
	List<Component> getNavigations();

	/**
	 * Access the message to show if an entity is not included in the column.
	 * 
	 * @return the message to show if an entity is not included in the column, or null if not defined.
	 */
	Message getNotIncludedMsg();

	/**
	 * Get additional encoding text only once, not per entity / per row.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The entity focus of the list (not a row entity).
	 * @param id
	 *        An ID value that can be used, unique to the entity list / column.
	 * @param numRows
	 *        The number of rows that were generated.
	 * @return The additional encoding text only once, not per entity / per row.
	 */
	String getOneTimeText(Context context, Object focus, String id, int numRows);

	/**
	 * Get additional prefix text only once, not per entity / per row.
	 * 
	 * @param context
	 *        The UiContext.
	 * @param focus
	 *        The entity focus of the list (not a row entity).
	 * @param id
	 *        An ID value that can be used, unique to the entity list / column.
	 * @return The additional encoding text only once, not per entity / per row.
	 */
	String getPrefixText(Context context, Object focus, String id);

	/**
	 * Access the right-justified setting.
	 * 
	 * @return the right-justified setting.
	 */
	boolean getRight();

	/**
	 * Access the sort asc icon path.
	 * 
	 * @return The sort asc icon path.
	 */
	String getSortAscIcon();

	/**
	 * Access the sort asc icon message.
	 * 
	 * @return The sort asc icon message.
	 */
	Message getSortAscMsg();

	/**
	 * Access the sort desc icon path.
	 * 
	 * @return The sort desc icon path.
	 */
	String getSortDescIcon();

	/**
	 * Access the sort desc icon message.
	 * 
	 * @return The sort desc icon message.
	 */
	Message getSortDescMsg();

	/**
	 * Access the sort asc destination.
	 * 
	 * @return The sort asc destination.
	 */
	Destination getSortDestinationAsc();

	/**
	 * Access the sort desc destination.
	 * 
	 * @return The sort desc destination.
	 */
	Destination getSortDestinationDesc();

	/**
	 * Access the sort asc. decision (if true, sorting is asc, if false, desc).
	 * 
	 * @return The sort asc. decision.
	 */
	Decision getSortingAscDecision();

	/**
	 * Access the sorting decision (if true, this column is currently selected for sorting).
	 * 
	 * @return The sorting decision.
	 */
	Decision getSortingDecision();

	/**
	 * Access the sort is submitting flag.
	 * 
	 * @return the sort is submitting flag.
	 */
	boolean getSortSubmit();

	/**
	 * Access the column title message.
	 * 
	 * @param context
	 *        The Context.
	 * @param focus
	 *        The focus object.
	 * @param effectiveId
	 *        The effective id
	 * @return The column title message.
	 */
	String getTitle(Context context, Object focus, String effectiveId);

	/**
	 * Access the topped setting.
	 * 
	 * @return the topped setting.
	 */
	boolean getTopped();

	/**
	 * Access the column pixel width.
	 * 
	 * @return The width (in pixels) for this column, or null if no specific width is set.
	 */
	Integer getWidth();

	/**
	 * Access the column css "em" width.
	 * 
	 * @return The width (in css "em" ) for this column, or null if no specific width is set.
	 */
	Integer getWidthEm();

	/**
	 * Access the column percent width.
	 * 
	 * @return The width (in percent) for this column, or null if no specific width is set.
	 */
	Integer getWidthPercent();

	/**
	 * Check if there is a title
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The focus object.
	 * @return true if there is a title, false if not.
	 */
	boolean hasTitle(Context context, Object focus);

	/**
	 * Check if the entire column is to be included.
	 * 
	 * @param context
	 *        The UiContext
	 * @return true if the column is to be included, false if not.
	 */
	boolean included(Context context);

	/**
	 * Check if a summary is required
	 * 
	 * @return true if a summary is required, false if not.
	 */
	boolean isSummaryRequired();

	/**
	 * Render the component.
	 * 
	 * @param context
	 *        The UI context.
	 * @param focus
	 *        An optional entity that is the focus of the rendering.
	 */
	void renderSummary(Context context, Object focus);

	/**
	 * Set the decision to display the column entry for an entity as an alert.
	 * 
	 * @param alertDecision
	 *        The decision. The decision.
	 * @return self.
	 */
	EntityListColumn setAlert(Decision alertDecision);

	/**
	 * Set the column data to be bottomed.
	 * 
	 * @return self.
	 */
	EntityListColumn setBottomed();

	/**
	 * Set the column data to be centered.
	 * 
	 * @return self.
	 */
	EntityListColumn setCentered();

	/**
	 * Set the decision to include each entity in this column.
	 * 
	 * @param inclusionDecision
	 *        The decision for inclusion of each entity in this column.
	 * @param notIncludedMsg
	 *        The message to show if an entity is not included in this column (or null for a blank display).
	 * @return self.
	 */
	EntityListColumn setEntityIncluded(Decision inclusionDecision, Message notIncludedMsg);

	/**
	 * Set the decision to include the entire column.
	 * 
	 * @param decision
	 *        The decision. The decision.
	 * @return self.
	 */
	EntityListColumn setIncluded(Decision decision);

	/**
	 * Set a no wrap for this column.
	 * 
	 * @return self.
	 */
	EntityListColumn setNoWrap();

	/**
	 * Set the column data to be right justified.
	 * 
	 * @return self.
	 */
	EntityListColumn setRight();

	/**
	 * Set the two destinations for the sort headers - one for the one leading to the ascending sort, one for the one leading to the descending sort.
	 * 
	 * @param asc
	 *        The ascending sort destination.
	 * @param desc
	 *        The descending sort destination.
	 * @return self.
	 */
	EntityListColumn setSortDestination(Destination asc, Destination desc);

	/**
	 * Set the sort asc and desc icon paths (each with description).
	 * 
	 * @param ascUrl
	 *        The asc icon path.
	 * @param ascMsg
	 *        the asc description.
	 * @param descUrl
	 *        The desc icon path.
	 * @param descMsg
	 *        The desc icon description.
	 * @return self.
	 */
	EntityListColumn setSortIcons(String ascUrl, Message ascMsg, String descUrl, Message descMsg);

	/**
	 * Set the decisions that tell if this column is currently sorting, and if so, if it's asc (not desc)
	 * 
	 * @param sorting
	 *        The decision to tell if this column is currently sorting.
	 * @param aNotD
	 *        The decision to tell if the sort is asc, not desc (if false, assume desc not asc).
	 * @return self.
	 */
	EntityListColumn setSorting(Decision sorting, Decision aNotD);

	/**
	 * Set the sort to be submitting.
	 * 
	 * @return self.
	 */
	EntityListColumn setSortSubmit();

	/**
	 * Set the column title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 */
	EntityListColumn setTitle(String selector, PropertyReference... references);

	/**
	 * Set the column data to be top aligned.
	 * 
	 * @return self.
	 */
	EntityListColumn setTopped();

	/**
	 * Set a width (in pixels) for this column.
	 * 
	 * @param width
	 *        The width (in pixels), or null for no specific width.
	 * @return self.
	 */
	EntityListColumn setWidth(int width);

	/**
	 * Set a width (in css "em") for this column.
	 * 
	 * @param width
	 *        The width (in css "em"), or null for no specific width.
	 * @return self.
	 */
	EntityListColumn setWidthEm(int width);

	/**
	 * Set a width (in percent) for this column.
	 * 
	 * @param width
	 *        The width (in percent), or null for no specific width.
	 * @return self.
	 */
	EntityListColumn setWidthPercent(int width);
}
