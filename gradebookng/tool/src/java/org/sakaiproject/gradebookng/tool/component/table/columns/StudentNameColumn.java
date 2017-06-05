package org.sakaiproject.gradebookng.tool.component.table.columns;

import java.util.HashMap;
import java.util.Map;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.gradebookng.tool.panels.StudentNameCellPanel;
import org.sakaiproject.gradebookng.tool.panels.StudentNameColumnHeaderPanel;

/**
 *
 * @author plukasew
 */
public class StudentNameColumn extends AbstractColumn
{
	public static final String STUDENT_COL_CSS_CLASS = "gb-student-cell";
	
	private final IGradesPage page;
	
	public StudentNameColumn(IGradesPage page)
	{
		super(Model.of("studentNameColumn"));
		this.page = page;
	}
	
	@Override
	public Component getHeader(final String componentId)
	{
		return new StudentNameColumnHeaderPanel(componentId, Model.of(page.getUiSettings().getNameSortOrder()));
	}

	@Override
	public void populateItem(final Item cellItem, final String componentId, final IModel rowModel)
	{
		final GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();

		final Map<String, Object> modelData = new HashMap<>();
		GbUser student = studentGradeInfo.getStudent();
		modelData.put("userId", student.getUserUuid());
		modelData.put("eid", student.getEid());
		modelData.put("firstName", student.getFirstName());
		modelData.put("lastName", student.getLastName());
		modelData.put("displayName", student.getDisplayName());
		modelData.put("nameSortOrder", page.getUiSettings().getNameSortOrder());

		cellItem.add(new StudentNameCellPanel(componentId, Model.ofMap(modelData)));
		cellItem.add(new AttributeModifier("data-studentUuid", student.getUserUuid()));
		cellItem.add(new AttributeModifier("abbr", student.getDisplayName()));
		cellItem.add(new AttributeModifier("aria-label", student.getDisplayName()));

		// TODO may need a subclass of Item that does the onComponentTag
		// override and then tag.setName("th");
	}

	@Override
	public String getCssClass()
	{
		return STUDENT_COL_CSS_CLASS;
	}
}
