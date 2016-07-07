/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/GradeImport.java $
 * $Id: GradeImport.java 9603 2014-12-19 01:52:27Z ggolden $
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

package org.etudes.mneme.tool;

public class GradeImport
{
	protected Boolean duplicate = Boolean.FALSE;
	protected String name;
	protected Float score;
	protected String scoreGiven;
	protected String studentId;
	protected String userId;

	public Boolean getDuplicate()
	{
		return this.duplicate;
	}

	public String getName()
	{
		return this.name;
	}

	public Float getScore()
	{
		return this.score;
	}

	public String getScoreGiven()
	{
		return this.scoreGiven;
	}

	public String getStatus()
	{
		if (this.studentId == null)
		{
			return "<i>no student ID given</i>";
		}
		if (this.scoreGiven == null)
		{
			return "<i>no score given</i>";
		}
		if (this.score == null)
		{
			return "<i>score not numeric</i>";
		}
		if (this.duplicate)
		{
			return "<i>duplicate student entry</i>";
		}
		if (this.userId == null)
		{
			return "<i>student not found</i>";
		}

		return null;
	}

	public String getStudentId()
	{
		return this.studentId;
	}

	public String getUserId()
	{
		return this.userId;
	}
}
