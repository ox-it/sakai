/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/EssayQuestionTypeImpl.java $
 * $Id: EssayQuestionTypeImpl.java 3635 2012-12-02 21:26:23Z ggolden $
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

import org.etudes.mneme.api.EssayQuestion;


/**
 * EssayQuestionImpl handles questions for the essay question type
 */
public class EssayQuestionTypeImpl extends QuestionImpl implements EssayQuestion
{
	/**
	 * {@inheritDoc}
	 */
	public String getModelAnswer()
	{
		return ((EssayQuestionImpl) getTypeSpecificQuestion()).getModelAnswer();
	}

	/**
	 * {@inheritDoc}
	 */
	public EssaySubmissionType getSubmissionType()
	{
		switch (((EssayQuestionImpl) getTypeSpecificQuestion()).getSubmissionType())
		{
			case attachments:
			{
				return EssaySubmissionType.attachments;
			}
			case both:
			{
				return EssaySubmissionType.both;
			}
			case inline:
			{
				return EssaySubmissionType.inline;
			}
			case none:
			{
				return EssaySubmissionType.none;
			}
		}
		return EssaySubmissionType.none;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setModelAnswer(String modelAnswer)
	{
		((EssayQuestionImpl) getTypeSpecificQuestion()).setModelAnswer(modelAnswer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSubmissionType(EssaySubmissionType setting)
	{
		switch (setting)
		{
			case attachments:
			{
				((EssayQuestionImpl) getTypeSpecificQuestion()).setSubmissionType(EssayQuestionImpl.SubmissionType.attachments);
				break;
			}
			case both:
			{
				((EssayQuestionImpl) getTypeSpecificQuestion()).setSubmissionType(EssayQuestionImpl.SubmissionType.both);
				break;
			}
			case inline:
			{
				((EssayQuestionImpl) getTypeSpecificQuestion()).setSubmissionType(EssayQuestionImpl.SubmissionType.inline);
				break;
			}
			case none:
			{
				((EssayQuestionImpl) getTypeSpecificQuestion()).setSubmissionType(EssayQuestionImpl.SubmissionType.none);
				break;
			}
		}
	}
}
