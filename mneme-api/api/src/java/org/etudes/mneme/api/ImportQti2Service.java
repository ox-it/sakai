/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-api/api/src/java/org/etudes/mneme/api/ImportQti2Service.java $
 * $Id: ImportQti2Service.java 6527 2013-12-06 23:01:08Z rashmim $
 ***********************************************************************************
 *
 * Copyright (c) 2013 Etudes, Inc.
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

import org.w3c.dom.Document;

/**
 * ImportService to bring in assessments and questions from QTI version 2 format
 */
public interface ImportQti2Service
{
	/**
	 * Import the QTI format 2 questions into a new pool in the context
	 * 
	 * @param qti
	 *        The DOM containing the QTI information.
	 * @param context
	 *        The context where the new pool will live.
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission to create pools and questions.
	 */
	void importPool(Document qti, String context, String unzipBackUpLocation) throws AssessmentPermissionException;
}
