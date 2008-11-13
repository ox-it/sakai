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
 * DistributionChart ...
 */
public interface DistributionChart extends Component
{
	/**
	 * Set the clumping, the number of percent points per bar, of the chart.
	 * 
	 * @param clump
	 *        The number of percent points per bar.
	 * @return self.
	 */
	DistributionChart setClump(int clump);

	/**
	 * Set the property reference for the data items to chart.
	 * 
	 * @param data
	 *        The data items to chart.
	 * @return self.
	 */
	DistributionChart setData(PropertyReference data);

	/**
	 * Set the height of the chart in pixels
	 * 
	 * @param height
	 *        Height in pixels.
	 * @return self.
	 */
	DistributionChart setHeight(int height);

	/**
	 * Set the property reference for the data value to mark.
	 * 
	 * @param mark
	 *        The data value to mark.
	 * @return self.
	 */
	DistributionChart setMark(PropertyReference mark);

	/**
	 * Set the property reference for the maximum data value.
	 * 
	 * @param max
	 *        The max data value.
	 * @return self.
	 */
	DistributionChart setMax(PropertyReference max);

	/**
	 * Set the title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	DistributionChart setTitle(String selector, PropertyReference... references);

	/**
	 * Set the width of the chart, in pixels.
	 * 
	 * @param width
	 *        Width in pixels.
	 * @return self.
	 */
	DistributionChart setWidth(int width);
}
