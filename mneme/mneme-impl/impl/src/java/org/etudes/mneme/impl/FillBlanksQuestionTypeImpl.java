/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/FillBlanksQuestionTypeImpl.java $
 * $Id: FillBlanksQuestionTypeImpl.java 9372 2014-11-26 19:30:29Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2011, 2014 Etudes, Inc.
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

import org.etudes.mneme.api.FillBlanksQuestion;

/**
 * FillBlanksQuestionImpl handles questions for the essay question type
 */
public class FillBlanksQuestionTypeImpl extends QuestionImpl implements FillBlanksQuestion
{
	/**
	 * {@inheritDoc}
	 */
	public boolean getAllowOneWord()
	{
		return Boolean.parseBoolean(((FillBlanksQuestionImpl) getTypeSpecificQuestion()).getAllowOneWord());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getAnyOrder()
	{
		return Boolean.parseBoolean(((FillBlanksQuestionImpl) getTypeSpecificQuestion()).getAnyOrder());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getAutomateScore()
	{
		return Boolean.parseBoolean(((FillBlanksQuestionImpl) getTypeSpecificQuestion()).getAutomateScore());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBlankSize()
	{
		return ((FillBlanksQuestionImpl) getTypeSpecificQuestion()).getBlankSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getCaseSensitive()
	{
		return Boolean.parseBoolean(((FillBlanksQuestionImpl) getTypeSpecificQuestion()).getCaseSensitive());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getResponseTextual()
	{
		return ((FillBlanksQuestionImpl) getTypeSpecificQuestion()).getResponseTextual();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText()
	{
		return ((FillBlanksQuestionImpl) getTypeSpecificQuestion()).getText();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAllowOneWord(boolean allowOneWord)
	{
		((FillBlanksQuestionImpl) getTypeSpecificQuestion()).setAllowOneWord(String.valueOf(allowOneWord));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnyOrder(boolean anyOrder)
	{
		((FillBlanksQuestionImpl) getTypeSpecificQuestion()).setAnyOrder(String.valueOf(anyOrder));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAutomateScore(boolean automateScore)
	{
		((FillBlanksQuestionImpl) getTypeSpecificQuestion()).setAutomateScore(String.valueOf(automateScore));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBlankSize(String blankSize)
	{
		((FillBlanksQuestionImpl) getTypeSpecificQuestion()).setBlankSize(blankSize);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCaseSensitive(boolean caseSensitive)
	{
		((FillBlanksQuestionImpl) getTypeSpecificQuestion()).setCaseSensitive(String.valueOf(caseSensitive));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setResponseTextual(String responseTextual)
	{
		((FillBlanksQuestionImpl) getTypeSpecificQuestion()).setResponseTextual(responseTextual);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setText(String text)
	{
		((FillBlanksQuestionImpl) getTypeSpecificQuestion()).setText(text);
	}
}
