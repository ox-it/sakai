/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2014 Etudes, Inc.
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

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * MnemeTransferService provides support for import into Mneme from other Mneme sites.
 */
public interface MnemeTransferService
{
	/**
	 * Distribute a (FCE) assessment from one site to another. The assessment will end up published, and it's pools (in the distributed to site) removed.
	 * 
	 * @param source
	 *        The source assessment to distribute.
	 * @param toContext
	 *        The site to distribute to.
	 * @param title
	 *        The title to use for the new assessment.
	 * @param openDate
	 *        The open date to use for the new assessment.
	 * @param dueDate
	 *        The due date to use for the new assessment.
	 * @param email
	 *        The results email address(es) to use for the new assessment.
	 * @param notify
	 *        The notify on open setting to use for the new assessment.
	 * @return The new assessment.
	 */
	Assessment distribute(Assessment source, String toContext, String title, Date openDate, Date dueDate, String email, Boolean notify);

	/**
	 * Import items from one site to another, possibly limited to a set of assessments and their dependencies (pools, questions)
	 * 
	 * @param fromContext
	 *        The source context.
	 * @param toContext
	 *        The destination context.
	 * @param assessmentsToImport
	 *        The set of assessment ids to import - if null, import all from the fromContext.
	 * @return The imported assessments.
	 */
	List<Assessment> importFromSite(String fromContext, String toContext, Set<String> assessmentsToImport);

}
