/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-api/api/src/java/org/etudes/mneme/api/ImporteCollegeTextService.java $
 * $Id: ImporteCollegeTextService.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2012 Etudes, Inc.
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
 * ImportTextService provides support for import questions from a text format into Mneme.
 */
public interface ImporteCollegeTextService
{
	/**
	 * Parse out questions from the eCollege text, and import them into the pool.
	 * 
	 * @param context
	 *        The context in which the pool and questions live.
	 * @param pool
	 *        The Pool to hold the new questions. If null, a new pool is created.
	 * @param text
	 *        The text containing the questions.
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission to create questions.
	 */
	void importQuestions(String context, Pool pool, String text) throws AssessmentPermissionException;
}
