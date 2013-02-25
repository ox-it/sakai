/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011, 2012 Etudes, Inc.
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

import java.util.List;

/**
 * Provide user lists based on certain roster selection codes.
 */
public interface RosterAdvisor
{
	enum RosterAccessType
	{
		user, alert, syllabus, melete, jforum, mneme, neverVisit
	};

	/**
	 * Create a well formatted roster access code.
	 * 
	 * @param context
	 *        The site id.
	 * @param type
	 *        The type of roster access requested.
	 * @param id
	 *        id related to the type.
	 * @return The roster access code.
	 */
	String encode(String context, RosterAccessType type, String id);

	/**
	 * Access a set of users based on a roster access code.
	 * 
	 * @param code
	 *        The roster access code.
	 * @return A List of UserId strings, empty if none found or the code is invalid.
	 */
	List<String> getUsers(String code);
}
