/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
 * QuestionAdminService provides an administrative function for Questions.
 */
public interface QuestionAdminService
{
	/**
	 * Find all the non-historical question ids.
	 * 
	 * @return a List of the non-historical question ids.
	 */
	List<String> findAllNonHistoricalIds();

	/**
	 * Just save - no changes to attribution, events, etc.
	 * 
	 * @param question
	 *        The question
	 * @throws AssessmentPermissionException
	 *         if the current user is not allowed to edit this question.
	 */
	void forceSave(Question question) throws AssessmentPermissionException;
}
