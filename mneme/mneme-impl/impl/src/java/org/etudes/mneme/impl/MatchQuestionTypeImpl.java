/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/MatchQuestionTypeImpl.java $
 * $Id: MatchQuestionTypeImpl.java 3635 2012-12-02 21:26:23Z ggolden $
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

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.List;

import org.etudes.mneme.api.MatchQuestion;
import org.etudes.mneme.api.MatchQuestion.MatchChoice;
import org.etudes.mneme.impl.MatchQuestionImpl.MatchQuestionPair;

/**
 * MatchQuestionImpl handles questions for the true false question type.
 */
public class MatchQuestionTypeImpl extends QuestionImpl implements MatchQuestion
{
	/**
	 * {@inheritDoc}
	 */
	public String getDistractor()
	{
		return ((MatchQuestionImpl) getTypeSpecificQuestion()).getDistractor();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MatchChoice> getMatchPairs()
	{
		List<MatchQuestionPair> mqpairList = ((MatchQuestionImpl) getTypeSpecificQuestion()).getPairs();
		List<MatchChoice> pairs = new ArrayList();

		if (mqpairList != null && mqpairList.size() > 0)
		{
			pairs = new ArrayList<MatchChoice>(mqpairList.size());

			for (MatchQuestionPair mqpair : mqpairList)
			{
				pairs.add(new MatchChoice(mqpair.getMatch(), mqpair.getChoice()));
			}
		}

		return pairs;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDistractor(String distractor)
	{
		((MatchQuestionImpl) getTypeSpecificQuestion()).setDistractor(distractor);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMatchPairs(List<MatchChoice> pairs)
	{
		for (MatchChoice mchoice : pairs)
		{
			((MatchQuestionImpl) getTypeSpecificQuestion()).addPair(mchoice.getChoice(), mchoice.getMatch());
		}
	}
}
