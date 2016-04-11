/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.entity.api;

import java.util.List;

/**
 * <p>
 * Services which implement EntityTransferrer declare themselves as willing and able to transfer/copy their entities
 * from one context to another. If you implement this interface you must be registered with the EntityManager.
 * </p>
 * @see org.sakaiproject.entity.api.EntityManager
 */
public interface EntityTransferrer
{
	/**
	 * transfer a copy of Entites from the source context into the destination context
	 * 
	 * @param fromContext
	 *        The source context
	 * @param toContext
	 *        The destination context
	 * @param ids
	 *        when null, all entities will be imported; otherwise, only entities with those ids will be imported
	 */
	void transferCopyEntities(String fromContext, String toContext, List<String> ids);

	/**
	 * Provide the string array of tool ids, for tools that we claim as manipulating our entities.
	 * 
	 * @return A array of tool IDs we will transfer entities for. Eg: sakai.resources.
	 */
	String[] myToolIds();
	
	/**
	 * transfer a copy of Entites from the source context into the destination context
	 * 
	 * @param fromContext
	 *        The source context
	 * @param toContext
	 *        The destination context
	 * @param ids
	 *        when null, all entities will be imported; otherwise, only entities with those ids will be imported
	 * @param cleanup
	 *        If true then service should attempt to remove the entities from the fromContext.
	 */
	void transferCopyEntities(String fromContext, String toContext, List<String> ids, boolean cleanup);
}
