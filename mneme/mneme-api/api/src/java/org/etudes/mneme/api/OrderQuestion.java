/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 Etudes, Inc.
 * 
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

import java.util.List;
import java.util.Set;

/**
 * OrderQuestion handles questions for the multiple choice question type.
 */
public interface OrderQuestion extends Question
{
	/**
	 * Access the list of choices.
	 * 
	 * @return The list of choices    
	 */
	public List<String> getAnswerChoices();

	/**
	 * Set the entire set of choices to these values.
	 * 
	 * @param choices
	 *        The choice values.
	 */
	public void setAnswerChoices(List<String> choices);
	
}
