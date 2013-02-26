/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2012 Etudes, Inc.
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

/**
 * <p>
 * SubmissionCompletionStatus enumerates different possible ways a submission was finalized.
 * </p>
 */
public enum SubmissionCompletionStatus
{
	autoComplete("A"), evaluationNonSubmit("E"), unknown(null), userFinished("U");

	public static SubmissionCompletionStatus getFromEncoding(String encoding)
	{
		if (encoding != null)
		{
			if (encoding.equals(autoComplete.getEncoding()))
			{
				return autoComplete;
			}
			else if (encoding.equals(evaluationNonSubmit.getEncoding()))
			{
				return evaluationNonSubmit;
			}
			else if (encoding.equals(userFinished.getEncoding()))
			{
				return userFinished;
			}
			else
			{
				return unknown;
			}
		}
		else
		{
			return unknown;
		}
	}

	private String encoding = null;

	private SubmissionCompletionStatus(String encoding)
	{
		this.encoding = encoding;
	}

	public String getEncoding()
	{
		return this.encoding;
	}
}
