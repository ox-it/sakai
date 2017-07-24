package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.gradebookng.tool.util.GbUtils;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Panel that is rendered for each student's course grade
 */
public class CourseGradeItemCellPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private static final String PARENT_ID = "cells";

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Map<String, Object>> model;
	
	private final boolean hasMenu;
	
	private final CourseGradeFormatter.FormatterConfig config = new CourseGradeFormatter.FormatterConfig();
	private CourseGradeFormatter courseGradeFormatter;

	public CourseGradeItemCellPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
		hasMenu = true;
	}
	
	public CourseGradeItemCellPanel(final String id, final IModel<Map<String, Object>> model, boolean hasMenu)
	{
		super(id, model);
		this.model = model;
		this.hasMenu = hasMenu;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		GbUtils.getParentCellFor(this, PARENT_ID).ifPresent(p -> p.setOutputMarkupId(true));

		// unpack model

		// Available options:
		// courseGradeDisplay
		// studentUuid
		// currentUserUuid
		// currentUserRole
		// gradebook
		// showPoints
		// showOverride
		// courseGradeVisible
		final Map<String, Object> modelData = this.model.getObject();
		
		final String studentUuid = (String) modelData.get("studentUuid");

		final GbRole role = (GbRole) modelData.get("currentUserRole");
		final Gradebook gradebook = (Gradebook) modelData.get("gradebook");
		final boolean courseGradeVisible = (boolean) modelData.get("courseGradeVisible");
		final boolean showPoints = (boolean) modelData.get("showPoints");
		final boolean showOverride = (boolean) modelData.get("showOverride");
		final boolean showLetterGrade = (boolean) modelData.get("showLetterGrade");

		final boolean hasCourseGradeOverride = (boolean) modelData.get("hasCourseGradeOverride");

		if (showOverride && hasCourseGradeOverride)
		{
			GbUtils.getParentCellFor(this, PARENT_ID).ifPresent(p -> p.add(new AttributeAppender("class", " gb-cg-override")));
		}
		
		GbCourseGrade gbcg = (GbCourseGrade) modelData.get("courseGrade");
		config.isCourseGradeVisible = courseGradeVisible;
		config.showPoints = showPoints;
		config.showOverride = showOverride;
		config.showLetterGrade = showLetterGrade;
		courseGradeFormatter = new CourseGradeFormatter(gradebook, role, config);
		final String courseGradeDisplay = courseGradeFormatter.format(gbcg.getCourseGrade());
		
		// the model map contains a lot of additional info we need for the course grade label, this is passed through

		final IGradesPage gradebookPage = (IGradesPage) getPage();

		// course grade label
		final Label courseGradeLabel = new Label("courseGrade", Model.of(courseGradeDisplay)) {

			@Override
			public void onEvent(final IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof ScoreChangedEvent) {
					final ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();

					// ensure event is for this student (which may not be applicable if we have no student)
					// TODO is this check ever not satisfied now that this has been refactroed?
					if (StringUtils.equals(studentUuid, scoreChangedEvent.getStudentUuid())) {

						final String newCourseGradeDisplay = refreshCourseGrade(studentUuid);

						// if course grade has changed, then refresh it
						if (!newCourseGradeDisplay.equals(getDefaultModelObject())) {
							setDefaultModel(Model.of(newCourseGradeDisplay));

							AjaxRequestTarget target = scoreChangedEvent.getTarget();
							target.add(this);
							GbUtils.getParentCellFor(this, PARENT_ID).ifPresent(target::add);
							target.appendJavaScript(String.format("$('#%s').closest('td').addClass('gb-score-dynamically-updated');", this.getMarkupId()));
						}
					}
				}
			}

		};
		courseGradeLabel.setOutputMarkupId(true);
		courseGradeLabel.setEscapeModelStrings(false);
		add(courseGradeLabel);

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return hasMenu && role == GbRole.INSTRUCTOR;
			}
		};
		menu.add(new GbAjaxLink("courseGradeOverride", Model.of(studentUuid))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				window.setContent(new CourseGradeOverridePanel(window.getContentId(), getModel(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		menu.add(new GbAjaxLink<String>("courseGradeOverrideLog", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				CourseGradeOverrideLogPanel.ModelData data = new CourseGradeOverrideLogPanel.ModelData();
				data.studentUuid = getDefaultModelObjectAsString();
				window.setContent(new CourseGradeOverrideLogPanel(window.getContentId(), Model.of(data), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		
		add(new GbAjaxLink("courseGradeOverrideLink", Model.of(studentUuid))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				window.setContent(new CourseGradeOverridePanel(window.getContentId(), getModel(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		}.setVisible(!hasMenu && showOverride));
		
		add(menu);
	}

	/**
	 *
	 * Helper to refresh the course grade
	 *
	 * @param studentUuid
	 * @param gradebook
	 * @param role
	 * @param courseGradeVisible
	 * @param showPoints
	 * @param showOverride
	 * @return
	 */
	private String refreshCourseGrade(final String studentUuid)
	{

		final CourseGrade courseGrade = this.businessService.getCourseGrade(studentUuid);
		GbCourseGrade gbcg = new GbCourseGrade(courseGrade);
		if (gbcg.getOverride().isPresent())
		{
			GbUtils.getParentCellFor(this, PARENT_ID).ifPresent(p -> p.add(new AttributeModifier("class", "gb-cg-override")
			{
				@Override
				protected String newValue(String currentValue, String value)
				{
					return currentValue.replaceAll(value, "") + " " + value;
				}
			}));
		}
		else
		{
			GbUtils.getParentCellFor(this, PARENT_ID).ifPresent(p -> p.add(new AttributeModifier("class", "gb-cg-override")
			{
				@Override
				protected String newValue(String currentValue, String value)
				{
					return currentValue.replaceAll(value, "");
				}
			}));
		}

		return courseGradeFormatter.format(courseGrade);
	}

}