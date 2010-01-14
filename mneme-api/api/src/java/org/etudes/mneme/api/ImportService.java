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

package org.etudes.mneme.api;

import java.util.List;

/**
 * ImportService provides support for import into Mneme.
 */
public interface ImportService
{
	/**
	 * The the Assignments in this context that can be imported as pools.
	 * 
	 * @param context
	 *        The context.
	 * @return A list of Assignments "Ent"s (id and description).
	 */
	List<Ent> getAssignments(String context);

	/**
	 * The the sites where this user has Assignment authoring permissions.
	 * 
	 * @param userId
	 *        The user id.
	 * @return A list of Assignment site "Ent"s (id and description).
	 */
	List<Ent> getAssignmentSites(String userId);

	/**
	 * @return TRUE if we will offer import from assignments support, FALSE if not.
	 */
	Boolean getOfferAssignment();

	/**
	 * @return TRUE if we will offer import from samigo support, FALSE if not.
	 */
	Boolean getOfferSamigo();

	/**
	 * The the Samigo assessments in this context that can be imported as pools.
	 * 
	 * @param context
	 *        The context.
	 * @return A list of Samigo assessment "Ent"s (id and description).
	 */
	List<Ent> getSamigoAssessments(String context);

	/**
	 * The the samigo pools available to this user.
	 * 
	 * @param userId
	 *        The user id.
	 * @return A list of Samigo pool "Ent"s (id and description).
	 */
	List<Ent> getSamigoPools(String userId);

	/**
	 * The the sites where this user has Samigo authoring permissions.
	 * 
	 * @param userId
	 *        The user id.
	 * @return A list of Samigo site "Ent"s (id and description).
	 */
	List<Ent> getSamigoSites(String userId);

	/**
	 * Import the Samigo assessment with this id into this context as a pool.
	 * 
	 * @param id
	 *        The id of the Samigo assessment to import.
	 * @param context
	 *        The context where the new pool will live.
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission to create pools and questions.
	 */
	void importAssessment(String id, String context) throws AssessmentPermissionException;

	/**
	 * Import the Assignment with this id into this context as a pool.
	 * 
	 * @param id
	 *        The id of the Assignment to import.
	 * @param context
	 *        The context where the new pool will live.
	 * @param draftSource
	 *        if true, the source Assignment will be changed to draft and removed from the gradebook
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission to create pools and questions.
	 */
	void importAssignment(String id, String context, boolean draftSource) throws AssessmentPermissionException;

	/**
	 * Import the Samigo pool with this id into this context
	 * 
	 * @param id
	 *        The id of the Samigo pool to import.
	 * @param context
	 *        The context where the new pool will live.
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission to create pools and questions.
	 */
	void importPool(String id, String context) throws AssessmentPermissionException;
}
