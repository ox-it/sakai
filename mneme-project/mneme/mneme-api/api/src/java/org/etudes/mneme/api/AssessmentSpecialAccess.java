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

package org.etudes.mneme.api;

import java.util.List;

/**
 * AssessmentSpecialAccess holds details of special access for select users to an assessment.
 */
public interface AssessmentSpecialAccess
{
	/**
	 * Add a special access to the assessment.
	 * 
	 * @return The new special access.
	 */
	AssessmentAccess addAccess();

	/**
	 * Find the special access that applies to this user id, creating one if one is not found.
	 * 
	 * @param userId
	 *        The user id to look for.
	 * @return The special access for this userId, perhaps new.
	 */
	AssessmentAccess assureUserAccess(String userId);

	/**
	 * Make sure any undefined user ids or users without permissions are removed.
	 */
	void assureValidUsers();

	/**
	 * Remove all special access.
	 */
	void clear();

	/**
	 * Access the special access defined for the assessment.
	 * 
	 * @return A List of the special access defined for the assessment.
	 */
	List<AssessmentAccess> getAccess();

	/**
	 * Find the special access with this id.
	 * 
	 * @param id
	 *        The access id.
	 * @return The special access with this id, or null if there is not one.
	 */
	AssessmentAccess getAccess(String id);

	/**
	 * Check if there is any special access defined
	 * 
	 * @return TRUE if special access is defined, FALSE if not.
	 */
	Boolean getIsDefined();

	/**
	 * @return TRUE if all the definitions are valid, FALSE if any are invalid.
	 */
	Boolean getIsValid();

	/**
	 * Access the special access defined for the assessment, sorted by user sort display.
	 * 
	 * @return A List of the special access defined for the assessment, sorted by user sort display.
	 */
	List<AssessmentAccess> getOrderedAccess();

	/**
	 * Find the special access that applies to this user id.
	 * 
	 * @param userId
	 *        The user id to look for.
	 * @return The special access for this userId, or null if there is not one.
	 */
	AssessmentAccess getUserAccess(String userId);

	/**
	 * Remove this special access.
	 * 
	 * @param access
	 *        The special access to remove.
	 */
	void removeAccess(AssessmentAccess access);
}
