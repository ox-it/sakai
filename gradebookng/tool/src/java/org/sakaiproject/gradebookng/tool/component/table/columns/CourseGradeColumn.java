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
import static org.sakaiproject.gradebookng.tool.pages.GradebookPage.COURSE_GRADE_COL_CSS_CLASS;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeItemCellPanel;

/**
 *
 * @author plukasew
 */
public class CourseGradeColumn extends AbstractColumn
{
	private final IGradesPage page;
	private final boolean courseGradeVisible;
	
	public CourseGradeColumn(final IGradesPage page, boolean isCourseGradeVisibleToCurrentUser)
	{
		super(Model.of(""));
		this.page = page;
		courseGradeVisible = isCourseGradeVisibleToCurrentUser;
	}
	
	@Override
	public Component getHeader(final String componentId) {
		return new CourseGradeColumnHeaderPanel(componentId, Model.of(page.getUiSettings().getShowPoints()));
	}

	@Override
	public String getCssClass() {
		final String cssClass = COURSE_GRADE_COL_CSS_CLASS;
		if (page.getUiSettings().getShowPoints()) {
			return cssClass + " points";
		} else {
			return cssClass;
		}
	}

	@Override
	public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
		final GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();

		cellItem.add(new AttributeModifier("tabindex", 0));

		// setup model
		// TODO we may not need to pass everything into this panel since we now use a display string
		// however we do requre that the label can receive events and update itself, although this could be recalculated for each
		// event
		final Map<String, Object> modelData = new HashMap<>();
		modelData.put("courseGradeDisplay", studentGradeInfo.getCourseGrade().getDisplayString());
		modelData.put("hasCourseGradeOverride", studentGradeInfo.getCourseGrade().getCourseGrade().getEnteredGrade() != null);
		modelData.put("studentUuid", studentGradeInfo.getStudent().getUserUuid());
		modelData.put("currentUserUuid", page.getCurrentUserUuid());
		modelData.put("currentUserRole", page.getCurrentUserRole());
		modelData.put("gradebook", page.getGradebook());
		modelData.put("showPoints", page.getUiSettings().getShowPoints());
		modelData.put("showOverride", false);
		modelData.put("showLetterGrade", false);
		modelData.put("courseGradeVisible", courseGradeVisible);
		
		cellItem.add(new CourseGradeItemCellPanel(componentId, Model.ofMap(modelData)));
		cellItem.setOutputMarkupId(true);
		
		// OWLTODO: remove editable course grade code/panel
		/*CourseGradeItemEditableCellPanel.EditableCourseGradeModelData modelData;
		modelData = new CourseGradeItemEditableCellPanel.EditableCourseGradeModelData();
		modelData.setGbCourseGrade(studentGradeInfo.getCourseGrade());
		modelData.setStudentUuid(studentGradeInfo.getStudentUuid());
		modelData.setShowPoints(page.getUiSettings().getShowPoints());

		cellItem.add(new CourseGradeItemEditableCellPanel(componentId, Model.of(modelData)));
		cellItem.setOutputMarkupId(true);*/
	}
}
