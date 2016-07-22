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
 * BarChart presents a bar chart.
 */
public interface BarChart extends Component
{
	/**
	 * Set the property reference for the data items to chart.
	 * 
	 * @param data
	 *        The data items to chart.
	 * @return self.
	 */
	BarChart setData(PropertyReference data);

	/**
	 * Set the height of the chart in pixels
	 * 
	 * @param height
	 *        Height in pixels.
	 * @return self.
	 */
	BarChart setHeight(int height);

	/**
	 * Set the property reference for the data index to mark.
	 * 
	 * @param index
	 *        The data index to mark.
	 * @return self.
	 */
	BarChart setMarkIndex(PropertyReference index);

	/**
	 * Set the title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	BarChart setTitle(String selector, PropertyReference... references);

	/**
	 * Set the width of the chart, in pixels
	 * 
	 * @param width
	 *        Width in pixels.
	 * @return self.
	 */
	BarChart setWidth(int width);
}
