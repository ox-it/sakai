/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-api/api/src/java/org/etudes/mneme/api/MatchQuestion.java $
 * $Id: MatchQuestion.java 3635 2012-12-02 21:26:23Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2011 Etudes, Inc.
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

/**
 * MatchQuestionImpl handles questions for the match question type.
 */
public interface MatchQuestion extends Question
{

	public class MatchChoice
	{
		protected String choice = null;
		protected String match = null;

		public MatchChoice(String match, String choice)
		{
			this.match = match;
			this.choice = choice;
		}
		
		public String getChoice()
		{
			return this.choice;
		}
		
		public String getMatch()
		{
			return this.match;
		}
	}
	
	/**
	 * Access the distractor's string value.
	 * 
	 * @return The distractor's string value, or null if not defined.
	 */
	public String getDistractor();
	
	/**
	 * Access the pairs as an entity (MatchChoice) list in as-authored order.
	 * 
	 * @return The pairs as an entity (MatchChoice) list in as-authored order.
	 */
	public List<MatchChoice> getMatchPairs();
	
	/**
	 * Set the distractor's string value.
	 * 
	 * @param distractor
	 *        The distractor's string value.
	 */
	public void setDistractor(String distractor);
	
	/**
	 * Set the pairs as an entity (MatchChoice) list in as-authored order.
	 * 
	 * @param pairs
	 *        Pairs of match choice questions.
	 */
	public void setMatchPairs(List<MatchChoice> pairs);
}
