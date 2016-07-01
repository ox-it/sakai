/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 Etudes, Inc.
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

package org.etudes.util.api;

/**
 * Provide additional mastery advice (based on some other set of information).
 */
public interface MasteryAdvisor
{
	/**
	 * See if the user has failed to master this item
	 * 
	 * @param toolId
	 *        The tool id for the application (such as "sakai.mneme").
	 * @param context
	 *        The context in which the object lives.
	 * @param id
	 *        The object's id.
	 * @param userId
	 *        The user id.
	 * @return TRUE if the item is subject to mastery, has been completed, and is below the mastery level, FALSE otherwise.
	 */
	Boolean failedToMaster(String toolId, String context, String id, String userId);

	/**
	 * Get the mastery level percent (0..100) that applies to this item, or null if the item is not subject to mastery.
	 * 
	 * @param toolId
	 *        The tool id for the application (such as "sakai.mneme").
	 * @param context
	 *        The context in which the object lives.
	 * @param id
	 *        The object's id.
	 * @param userId
	 *        The user id.
	 * @return The mastery level percent (0..100), or null if there is none.
	 */
	Integer masteryLevelPercent(String toolId, String context, String id, String userId);
}
