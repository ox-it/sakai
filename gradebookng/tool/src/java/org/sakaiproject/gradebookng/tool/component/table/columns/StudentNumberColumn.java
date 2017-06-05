package org.sakaiproject.gradebookng.tool.component.table.columns;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.tool.panels.StudentNumberColumnHeaderPanel;

/**
 *
 * @author plukasew
 */
public class StudentNumberColumn extends AbstractColumn
{
	public static final String STUDENT_NUM_COL_CSS_CLASS = "gb-student-number-cell";
	
	public StudentNumberColumn()
	{
		super(Model.of(""));
	}
	
	@Override
	public Component getHeader(final String componentId)
	{
		return new StudentNumberColumnHeaderPanel(componentId);
	}

	@Override
	public void populateItem(final Item cellItem, final String componentId, final IModel rowModel)
	{
		final GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();

		final String studentNumber = StringUtils.defaultIfBlank(studentGradeInfo.getStudent().getStudentNumber(), "-");

		cellItem.add(new Label(componentId, studentNumber));
		cellItem.add(new AttributeModifier("data-studentUuid", studentGradeInfo.getStudent().getUserUuid()));
		cellItem.add(new AttributeModifier("abbr", studentNumber));
		cellItem.add(new AttributeModifier("aria-label", studentNumber));
	}

	@Override
	public String getCssClass() {
		return STUDENT_NUM_COL_CSS_CLASS;
	}
}
