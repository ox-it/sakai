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

/**
 * AssessmentPermissionException is thrown to indicate a lack of permission for an assessment service access.
 */
public class AssessmentPermissionException extends Exception
{
	/** The function name. */
	protected String m_function = null;

	/** The resource reference. */
	protected String m_resource = null;

	/** The id of the user. */
	protected String m_user = null;

	/**
	 * Construct.
	 * 
	 * @param user
	 *        The id of the user.
	 * @param function
	 *        The function.
	 * @param resource
	 *        The resource reference.
	 */
	public AssessmentPermissionException(String user, String function, String resource)
	{
		m_user = user;
		m_function = function;
		m_resource = resource;
	}

	/**
	 * Access the function name.
	 * 
	 * @return The function name.
	 */
	public String getFunction()
	{
		return m_function;
	}

	/**
	 * Access the resource id.
	 * 
	 * @return The resource id.
	 */
	public String getResource()
	{
		return m_resource;
	}

	/**
	 * Access the id of the user.
	 * 
	 * @return The id of the user.
	 */
	public String getUser()
	{
		return m_user;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		return super.toString() + " user=" + m_user + " function=" + m_function + " resource=" + m_resource;
	}
}
