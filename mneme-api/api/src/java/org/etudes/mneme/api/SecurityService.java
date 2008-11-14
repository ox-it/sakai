/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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

import java.util.Set;

/**
 * SecurityService provides extended security support for Mneme.
 */
public interface SecurityService
{
	/**
	 * Check the security for this user doing this function within this context.
	 * 
	 * @param userId
	 *        the user id.
	 * @param function
	 *        the function.
	 * @param context
	 *        The context.
	 * @param ref
	 *        The entity reference.
	 * @return true if the user has permission, false if not.
	 */
	boolean checkSecurity(String userId, String function, String context);

	/**
	 * Get the list of user ids who can perform this function in this context.
	 * 
	 * @param function
	 *        the function.
	 * @param context
	 *        The context.
	 * @return The list of user ids who can perform this function in this context.
	 */
	Set<String> getUsersIsAllowed(String function, String context);

	/**
	 * Check security and throw if not satisfied
	 * 
	 * @param userId
	 *        the user id.
	 * @param function
	 *        the function.
	 * @param context
	 *        The context.
	 * @param ref
	 *        The entity reference.
	 * @throws AssessmentPermissionException
	 *         if security is not satisfied.
	 */
	void secure(String userId, String function, String context) throws AssessmentPermissionException;
}
