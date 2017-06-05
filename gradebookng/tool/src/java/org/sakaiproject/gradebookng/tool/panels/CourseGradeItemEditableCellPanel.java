package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
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
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.Gradebook;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

/**
 *
 * @author plukasew
 */
public class CourseGradeItemEditableCellPanel extends Panel
{
	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<EditableCourseGradeModelData> model;
	
	TextField<String> gradeCell;
	Label percAndPoints;
	private String originalGrade;
	
	String rawGrade;
	String formattedGrade;
	String displayGrade;
	
	String baseGradeStyle = GradebookPage.COURSE_GRADE_COL_CSS_CLASS;
	GradeItemCellPanel.GradeCellSaveStyle gradeSaveStyle;
	
	final List<GradeItemCellPanel.GradeCellNotification> notifications = new ArrayList<>();
	
	public CourseGradeItemEditableCellPanel(final String id, final IModel<EditableCourseGradeModelData> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize()
	{
		super.onInitialize();
		
		final EditableCourseGradeModelData modelData = model.getObject();
		final GbCourseGrade gbCourseGrade = modelData.getGbCourseGrade();
		final String studentUuid = modelData.getStudentUuid();

		// OWLTODO: relocate these businessService calls so they aren't done once per cell
		final GbRole role = businessService.getUserRole();
		final Gradebook gradebook = businessService.getGradebook();
		final boolean courseGradeVisible = true;
		final boolean showPoints = modelData.isShowPoints();
		final boolean showOverride = true;

		final IGradesPage gradebookPage = (IGradesPage) getPage();
		
		// OWLTODO: Wicket complains about CourseGradeFormatter not being serializable
		final CourseGradeFormatter formatter = new CourseGradeFormatter(gradebook, role, courseGradeVisible, showPoints, showOverride, true);
		final CourseGradeFormatter.GbEditableCourseGradeDisplay cgDisplay = formatter.formatForEditing(gbCourseGrade.getCourseGrade());
		displayGrade = cgDisplay.letterGrade;
		gradeCell = new TextField<String>("editableCourseGrade", Model.of(displayGrade))
		{
			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				
				final Component parentCell = getParentCellFor(this);
				parentCell.add(new AttributeModifier("data-studentuuid", studentUuid));
				parentCell.setOutputMarkupId(true);
			}
		};
		gradeCell.add(new AjaxFormComponentUpdatingBehavior("scorechange.sakai")
		{
			@Override
			public void onBind()
			{
				super.onBind();
				
				originalGrade = gradeCell.getDefaultModelObjectAsString();
			}
			
			@Override
			protected void onUpdate(final AjaxRequestTarget target)
			{
				final String newGrade = StringUtils.trimToNull(gradeCell.getValue());
				
				notifications.clear();
				
				// validate the grade entered is a valid one for the selected grading schema
				// though we allow blank grades so the override is removed
				boolean valid = true;
				if (newGrade != null)
				{
					final GradebookInformation gbInfo = businessService.getGradebookSettings();
					final Map<String, Double> schema = gbInfo.getSelectedGradingScaleBottomPercents();
					final List<String> unmapped = gbInfo.getSelectedGradingScaleUnmappedGrades();
					valid = schema.containsKey(newGrade) || unmapped.contains(newGrade);
				}
				
				if (!valid)
				{
					markWarning(gradeCell);
					target.add(gradebookPage.updateLiveGradingMessage(getString("feedback.error")));
				}
				else
				{
					// save
					final boolean success = businessService.updateCourseGrade(studentUuid, newGrade);

					gradeCell.setDefaultModelObject(newGrade); // OWLTODO: only set this on success?
					
					if (success)
					{
						markSuccessful(gradeCell);
						originalGrade = newGrade;
						CourseGrade newCourseGrade = businessService.getCourseGrade(studentUuid);
						model.getObject().setGbCourseGrade(new GbCourseGrade(newCourseGrade));
						CourseGradeFormatter.GbEditableCourseGradeDisplay cgDisplay = formatter.formatForEditing(newCourseGrade);
						gradeCell.setModelObject(cgDisplay.letterGrade);
						percAndPoints.setDefaultModelObject(cgDisplay.percentageAndPoints);
						target.add(gradebookPage.updateLiveGradingMessage(getString("feedback.saved")));
					}
					else
					{
						markError(gradeCell);
						originalGrade = newGrade;
						target.add(gradebookPage.updateLiveGradingMessage(getString("feedback.error")));
					}
				}
				
				refreshNotifications();
				styleGradeCell(gradeCell);
				
				// refresh the components we need
				target.addChildren(getPage(), FeedbackPanel.class);
				target.add(getParentCellFor(getComponent()));
				target.add(gradeCell);
			}
			
			@Override
			protected void updateAjaxAttributes(final AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);

				final Map<String, Object> extraParameters = attributes.getExtraParameters();
				extraParameters.put("studentUuid", studentUuid);

				final AjaxCallListener myAjaxCallListener = new AjaxCallListener()
				{
					@Override
					public CharSequence getPrecondition(final Component component) {
						return "return GradebookWicketEventProxy.updateGradeItem.handlePrecondition('"
								+ getParentCellFor(component).getMarkupId() + "', attrs);";
					}

					@Override
					public CharSequence getBeforeSendHandler(final Component component) {
						return "GradebookWicketEventProxy.updateGradeItem.handleBeforeSend('"
								+ getParentCellFor(component).getMarkupId() + "', attrs, jqXHR, settings);";
					}

					@Override
					public CharSequence getSuccessHandler(final Component component) {
						return "GradebookWicketEventProxy.updateGradeItem.handleSuccess('" + getParentCellFor(component).getMarkupId()
								+ "', attrs, jqXHR, data, textStatus);";
					}

					@Override
					public CharSequence getFailureHandler(final Component component) {
						return "GradebookWicketEventProxy.updateGradeItem.handleFailure('" + getParentCellFor(component).getMarkupId()
								+ "', attrs, jqXHR, errorMessage, textStatus);";
					}

					@Override
					public CharSequence getCompleteHandler(final Component component) {
						return "GradebookWicketEventProxy.updateGradeItem.handleComplete('" + getParentCellFor(component).getMarkupId()
								+ "', attrs, jqXHR, textStatus);";
					}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
		});
		
		gradeCell.add(new AjaxEventBehavior("revertscore.sakai")
		{
			
			@Override
			protected void onEvent(final AjaxRequestTarget target)
			{
				IGradesPage page = (IGradesPage) getPage();
				
				// reset score
				gradeCell.setDefaultModelObject(originalGrade);
				
				// reset styles and flags
				baseGradeStyle = GradebookPage.COURSE_GRADE_COL_CSS_CLASS;
				gradeSaveStyle = null;
				styleGradeCell(CourseGradeItemEditableCellPanel.this);
				notifications.clear();

				// apply any applicable flags
				refreshNotifications();

				// tell the javascript to refresh the cell
				target.add(getParentCellFor(getComponent()));
				target.add(page.updateLiveGradingMessage(getString("feedback.saved")));
			}
			
			@Override
			protected void updateAjaxAttributes(final AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				
				final Map<String, Object> extraParameters = attributes.getExtraParameters();
				extraParameters.put("studentUuid", studentUuid);

				final AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public CharSequence getCompleteHandler(final Component component) {
						return "GradebookWicketEventProxy.revertGradeItem.handleComplete('" + getParentCellFor(component).getMarkupId()
								+ "', attrs, jqXHR, textStatus);";
					}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
		});
		
		gradeCell.setOutputMarkupId(true);
		add(gradeCell);
		
		refreshNotifications();
		styleGradeCell(this);
		
		percAndPoints = new Label("editableCourseGradePercentageAndPoints", Model.of(cgDisplay.percentageAndPoints));
		percAndPoints.setEscapeModelStrings(false);
		add(percAndPoints);
		
		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				//return role == GbRole.INSTRUCTOR;
				return true; // OWLTODO fix this
			}
		};
		menu.add(new GbAjaxLink("courseGradeOverride", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
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
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new CourseGradeOverrideLogPanel(window.getContentId(), getModel(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		add(menu);
	}
	
	/**
	 * Get the parent container for the grade cell so we can style it
	 *
	 * @param gradeCell
	 * @return
	 */
	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "cells")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}
	
		/**
	 * Builder for styling the cell. Aware of the cell 'save style' as well as if it has comments and adds styles accordingly
	 *
	 * @param gradeCell the cell to style
	 */
	private void styleGradeCell(final Component gradeCell) {

		final ArrayList<String> cssClasses = new ArrayList<>();
		cssClasses.add(baseGradeStyle); // always
		if (model.getObject().getGbCourseGrade().hasOverride())
		{
			cssClasses.add("gb-cg-override");
		}
		if (gradeSaveStyle != null)
		{
			cssClasses.add(gradeSaveStyle.getCss()); // the particular style for this cell that has been computed previously
		}

		// replace the cell styles with the new set
		getParentCellFor(gradeCell).add(AttributeModifier.replace("class", StringUtils.join(cssClasses, " ")));
	}
	
	private void markSuccessful(final Component gradeCell)
	{
		gradeSaveStyle = GradeItemCellPanel.GradeCellSaveStyle.SUCCESS;
		styleGradeCell(gradeCell);
	}

	private void markError(final Component gradeCell)
	{
		gradeSaveStyle = GradeItemCellPanel.GradeCellSaveStyle.ERROR;
		styleGradeCell(gradeCell);
		notifications.add(GradeItemCellPanel.GradeCellNotification.ERROR);
	}

	private void markWarning(final Component gradeCell)
	{
		// OWLTODO: switch to course grade appropriate messaging, ie. "message.addcoursegradeoverride.invalid"
		gradeSaveStyle = GradeItemCellPanel.GradeCellSaveStyle.WARNING;
		styleGradeCell(gradeCell);
		notifications.add(GradeItemCellPanel.GradeCellNotification.INVALID);
	}
	
	private void refreshNotifications()
	{
		final WebMarkupContainer warningNotification = new WebMarkupContainer("warningNotification");
		final WebMarkupContainer errorNotification = new WebMarkupContainer("errorNotification");

		warningNotification.setVisible(false);
		errorNotification.setVisible(false);

		if (!notifications.isEmpty())
		{
			if (notifications.contains(GradeItemCellPanel.GradeCellNotification.ERROR)) {
				errorNotification.setVisible(true);
				addPopover(errorNotification, notifications);
			} else if (notifications.contains(GradeItemCellPanel.GradeCellNotification.INVALID)
					|| notifications.contains(GradeItemCellPanel.GradeCellNotification.CONCURRENT_EDIT)
					|| notifications.contains(GradeItemCellPanel.GradeCellNotification.READONLY)) {
				warningNotification.setVisible(true);
				addPopover(warningNotification, this.notifications);
			}
		}

		addOrReplace(warningNotification);
		addOrReplace(errorNotification);
	}
	
	private void addPopover(final Component component, final List<GradeItemCellPanel.GradeCellNotification> notifications)
	{
		//final Map<String, Object> modelData = this.model.getObject();
		//modelData.put("gradeable", true);
		//final GradeItemCellPopoverPanel popover = new GradeItemCellPopoverPanel("popover", Model.ofMap(modelData), notifications);
		//final String popoverString = ComponentRenderer.renderComponent(popover).toString();
		
		// OWLTODO: replace above with dedicated popover panel

		component.add(new AttributeModifier("data-toggle", "popover"));
		component.add(new AttributeModifier("data-trigger", "manual"));
		component.add(new AttributeModifier("data-placement", "bottom"));
		component.add(new AttributeModifier("data-html", "true"));
		component.add(new AttributeModifier("data-container", "#gradebookGrades"));
		//component.add(new AttributeModifier("data-content", popoverString));
		component.add(new AttributeModifier("tabindex", "0"));
	}
	
	@Getter @Setter
	public static class EditableCourseGradeModelData implements Serializable
	{
		private GbCourseGrade gbCourseGrade;
		private String studentUuid;
		private boolean showPoints;
	}
	
}
