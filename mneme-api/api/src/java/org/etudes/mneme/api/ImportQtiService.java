/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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

import org.w3c.dom.Document;

/**
 * ImportService provides support for import into Mneme.
 */
public interface ImportQtiService
{
	/**
	 * Import the QTI questions into a new pool in the context
	 * 
	 * @param qti
	 *        The DOM containing the QTI information.
	 * @param context
	 *        The context where the new pool will live.
	 * @throws AssessmentPermissionException
	 *         if the user does not have permission to create pools and questions.
	 */
	void importPool(Document qti, String context) throws AssessmentPermissionException;
	
	/**
	 * Import the QTI 1.0 questions from the zip file into a new pool
	 * 
	 * @param qti
	 * @param context
	 * @param unzipLocation
	 * @return
	 * @throws AssessmentPermissionException
	 */
	boolean importPool(Document qti, String context, String unzipLocation) throws AssessmentPermissionException;
}
