/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/OrderQuestionTypeImpl.java $
 * $Id: OrderQuestionTypeImpl.java 3635 2012-12-02 21:26:23Z ggolden $
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
import java.util.Set;

import org.etudes.mneme.api.OrderQuestion;
import org.etudes.mneme.impl.OrderQuestionImpl.OrderQuestionChoice;

/**
 * OrderQuestionTypeImpl handles questions for the ordering question type
 */
public class OrderQuestionTypeImpl extends QuestionImpl implements OrderQuestion
{
	/**
	 * {@inheritDoc}
	 */
	public List<String> getAnswerChoices()
	{
		List<OrderQuestionChoice> mcChoiceList = ((OrderQuestionImpl) getTypeSpecificQuestion()).getChoicesAsAuthored();
		List<String> answerChoices = new ArrayList();

		if (mcChoiceList != null && mcChoiceList.size() > 0)
		{
			answerChoices = new ArrayList<String>(mcChoiceList.size());

			for (OrderQuestionChoice mcChoice : mcChoiceList)
			{
				answerChoices.add(mcChoice.getText());
			}
		}

		return answerChoices;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnswerChoices(List<String> choices)
	{
		((OrderQuestionImpl) getTypeSpecificQuestion()).setAnswerChoices(choices);
	}

}
