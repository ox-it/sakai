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

import java.util.Date;

/**
 * Expiration models information about the time a submission or assessment expires and can no loger be worked upon.
 */
public interface Expiration
{
	/**
	 * The possible expiration causes.
	 */
	enum Cause
	{
		closedDate, timeLimit
	}

	/**
	 * Access the cause of the expiration.
	 * 
	 * @return The cause of the expiration.
	 */
	Cause getCause();

	/**
	 * Access the duration, in ms, till expiration.
	 * 
	 * @return The duration, in ms, till expiration.
	 */
	Long getDuration();

	/**
	 * Access the time limit that is set for this submission.
	 * 
	 * @return The time limit that is set for this submission, or null if none.
	 */
	Long getLimit();

	/**
	 * Access the Time that marks the expiration date.
	 * 
	 * @return The Time that marks the expiration date, or null if none.
	 */
	Date getTime();
}
