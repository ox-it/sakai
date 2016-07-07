/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/GradeImportSet.java $
 * $Id: GradeImportSet.java 9578 2014-12-18 03:27:47Z ggolden $
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

import java.util.ArrayList;
import java.util.List;

import org.etudes.mneme.api.Assessment;

public class GradeImportSet
{
	public static final String ATTR_NAME = "mneme:GradeImportSet";

	protected Assessment assessment = null;
	protected String assessmentTitle = null;
	protected Float points = null;
	protected List<GradeImport> rows = new ArrayList<GradeImport>();
	protected Boolean selected = Boolean.TRUE;

	public Assessment getAssessment()
	{
		return this.assessment;
	}

	public String getAssessmentTitle()
	{
		return this.assessmentTitle;
	}

	public Float getPoints()
	{
		return this.points;
	}

	public List<GradeImport> getRows()
	{
		return this.rows;
	}

	public Boolean getSelected()
	{
		return this.selected;
	}

	public void setSelected(Boolean selected)
	{
		this.selected = selected;
	}
}
