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

import java.util.Collection;
import java.util.List;

/**
 * A Grid holds objects in columns and rows.
 */
public interface Grid
{
	/**
	 * Access the list of rows.
	 * 
	 * @return
	 */
	public List<GridRow> getRows();

	/**
	 * Load the grid, starting at the top and across the columns then down the rows, with the objects.
	 * 
	 * @param objects
	 *        The object to load into the grid.
	 */
	public void load(Collection<? extends Object> objects);

	/**
	 * Set the width of the grid, the number of columms.
	 * 
	 * @param width
	 *        The number of columns for the grid.
	 */
	public void setWidth(int width);
}
