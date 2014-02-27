/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008, 2014 The Regents of the University of Michigan & Foothill College, ETUDES Project
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

package org.etudes.mneme.api;

/**
 * Ent is even smaller than Entity - just something with an id and description.
 */
public interface Ent
{
	/**
	 * Access the description.
	 * 
	 * @return The description.
	 */
	String getDescription();

	/**
	 * Access the id.
	 * 
	 * @return The id;
	 */
	String getId();
	
	/**
	 * @return if the Ent is marked
	 */
	Boolean getMarked();
	
	/**
	 * Mark the End
	 * @param marked The marking.
	 */
	void setMarked(Boolean marked);
	
	/**
	 * Access the term id.
	 * 
	 * @return The term id;
	 */
	public long getTermId();
	
	/**
	 * Access the term desc.
	 * 
	 * @return The term desc;
	 */
	public String getTermDescription();
}
