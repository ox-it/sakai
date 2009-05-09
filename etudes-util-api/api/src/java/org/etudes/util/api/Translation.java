/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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

/**
 * Translation between two strings.
 */
public interface Translation
{
	/**
	 * Get the 'from' of the translation.
	 * 
	 * @return The string to translate from.
	 */
	String getFrom();

	/**
	 * Get the 'to' of the translation.
	 * 
	 * @return The string to translate to.
	 */
	String getTo();

	/**
	 * Reverse translate a target string.
	 * 
	 * @param target
	 *        The target string.
	 * @return The target string with all "to" instances replaced with "from" translation.
	 */
	String reverseTranslate(String target);

	/**
	 * Set the 'from'.
	 * 
	 * @param from
	 *        The string to translate from.
	 */
	void setFrom(String from);

	/**
	 * Set the 'to'.
	 * 
	 * @param to
	 *        The string to translate to.
	 */
	void setTo(String to);

	/**
	 * Translate a target string.
	 * 
	 * @param target
	 *        The target string.
	 * @return The target string with all "from" instances replaced with "to" translation.
	 */
	String translate(String target);
}
