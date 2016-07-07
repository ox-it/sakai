/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-1393/mneme-impl/impl/src/java/org/etudes/mneme/impl/FillBlanksQuestionTypeImpl.java $
 * $Id: FillBlanksQuestionTypeImpl.java 9372 2014-11-26 19:30:29Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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

import org.etudes.mneme.api.FillInlineQuestion;

/**
 * FillInlineQuestionImpl handles questions for the essay question type
 */
public class FillInlineQuestionTypeImpl extends QuestionImpl implements FillInlineQuestion
{
	/**
	 * {@inheritDoc}
	 */
	public String getText()
	{
		return ((FillInlineQuestionImpl) getTypeSpecificQuestion()).getText();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setText(String text)
	{
		((FillInlineQuestionImpl) getTypeSpecificQuestion()).setText(text);
	}

}
