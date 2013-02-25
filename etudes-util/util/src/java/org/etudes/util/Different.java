/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/etudes-util/trunk/etudes-util/util/src/java/org/etudes/util/Different.java $
 * $Id: Different.java 3628 2012-12-02 21:03:40Z ggolden $
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

package org.etudes.util;

/**
 * Utility class to compute difference considering nulls.
 */
public class Different
{
	/**
	 * Compare two objects for differences, either may be null
	 * 
	 * @param a
	 *        One object.
	 * @param b
	 *        The other object.
	 * @return true if the object are different, false if they are the same.
	 */
	public static boolean different(Object a, Object b)
	{
		// if both null, they are the same
		if ((a == null) && (b == null)) return false;

		// if either are null (they both are not), they are different
		if ((a == null) || (b == null)) return true;

		// now we know neither are null, so compare
		return (!a.equals(b));
	}
}
