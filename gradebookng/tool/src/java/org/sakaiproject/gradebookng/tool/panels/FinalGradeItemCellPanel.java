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
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FinalGradeFormatter;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.behavior.RevertScoreBehavior;
import org.sakaiproject.gradebookng.tool.behavior.ScoreChangeBehavior;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.component.table.columns.FinalGradeColumn;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.pages.CourseGradesPage;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.gradebookng.tool.panels.FinalGradeItemPopoverPanel.FinalGradePopoverData;
import org.sakaiproject.gradebookng.tool.util.GbUtils;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
/**
 *
 * @author plukasew
 */
public class FinalGradeItemCellPanel extends Panel
{
	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	private static final String PARENT_ID = "cells";
	
	TextField<String> gradeField;
	private String originalGrade;
	
	String baseGradeStyle = FinalGradeColumn.CSS_CLASS;
	GradeItemCellPanel.GradeCellSaveStyle gradeSaveStyle;
	
	private final IModel<? extends GbStudentGradeInfo> model;
	
	final List<FinalGradeCellNotification> notifications = new ArrayList<>();
	
	IGradesPage page;
	
	public enum FinalGradeCellNotification implements Serializable
	{
		ERROR("grade.notifications.haserror"),
		INVALID("finalgrades.notifications.invalid");
		
		private String key;
		
		FinalGradeCellNotification(final String messageKey)
		{
			key = messageKey;
		}
		
		public String getMessageKey()
		{
			return key;
		}
	}
	
	public FinalGradeItemCellPanel(final String id, final IModel<? extends GbStudentGradeInfo> model)
	{
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize()
	{
		super.onInitialize();
		
		page = (IGradesPage) getPage();
		
		GbCourseGrade gbCourseGrade = model.getObject().getCourseGrade();
		final String studentUuid = model.getObject().getStudent().getUserUuid();
		final String displayGrade = formatGrade(gbCourseGrade);
		
		gradeField = new TextField<String>("editableFinalGrade", Model.of(displayGrade))
		{
			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				
				GbUtils.getParentCellFor(this, PARENT_ID)
						.ifPresent(p -> p.add(new AttributeModifier("data-studentuuid", studentUuid)).setOutputMarkupId(true));
			}
		};
		gradeField.setOutputMarkupId(true);
		add(gradeField);
		
		String cellMarkupId = GbUtils.getParentCellFor(this, PARENT_ID).map(p -> p.getMarkupId(true)).orElse("");
		gradeField.add(new ScoreChangeBehavior(studentUuid, cellMarkupId)
		{
			@Override
			public void onBind()
			{
				super.onBind();
				originalGrade = gradeField.getDefaultModelObjectAsString();
			}
			
			@Override
			protected void onUpdate(final AjaxRequestTarget target)
			{
				GbStopWatch stopwatch = new GbStopWatch("FinalGradeItemCellPanel");
				
				String newGrade = StringUtils.trimToNull(gradeField.getValue());
				
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
					markWarning(gradeField);
					target.add(page.updateLiveGradingMessage(getString("feedback.error")));
				}
				else
				{
					String saveGrade = newGrade;
					if (StringUtils.isNotBlank(newGrade))
					{
						// does the entered grade match the rounded final grade anyway? If so, remove the override
						CourseGrade originalCourseGrade = businessService.getCourseGrade(studentUuid);
						GbCourseGrade ogbcg = new GbCourseGrade(originalCourseGrade);
						String formatted = ogbcg.getCalculatedGrade().map(c -> String.valueOf(FinalGradeFormatter.round(c))).orElse("");
						if (newGrade.equals(formatted))
						{
							saveGrade = null;
						}
					}
					// save
					final boolean success = businessService.updateCourseGrade(studentUuid, saveGrade);

					gradeField.setDefaultModelObject(newGrade); // OWLTODO: only set this on success?
					
					if (success)
					{
						markSuccessful(gradeField);
						originalGrade = newGrade;
						CourseGrade newCourseGrade = businessService.getCourseGrade(studentUuid);
						GbCourseGrade newGbCourseGrade = new GbCourseGrade(newCourseGrade);
						model.getObject().setCourseGrade(newGbCourseGrade);
						String displayGrade = formatGrade(newGbCourseGrade);
						gradeField.setModelObject(displayGrade);
						target.add(page.updateLiveGradingMessage(getString("feedback.saved")));
						((CourseGradesPage) page).redrawForGradeChange(target);
						// trigger async event that score has been updated and now displayed
						target.appendJavaScript(String.format("$('#%s').trigger('scoreupdated.sakai')", gradeField.getMarkupId()));
					}
					else
					{
						markError(gradeField);
						originalGrade = newGrade;
						target.add(page.updateLiveGradingMessage(getString("feedback.error")));
					}
				}
				
				refreshNotifications();
				styleGradeCell(gradeField);
				
				// refresh the components we need
				target.addChildren(getPage(), FeedbackPanel.class);
				GbUtils.getParentCellFor(getComponent(), PARENT_ID).ifPresent(target::add);
				target.add(gradeField);
				
				stopwatch.time("ScoreChange onUpdate");
			}
		});
		
		gradeField.add(new AjaxEventBehavior("scoreupdated.sakai")
		{
			@Override
			protected void onEvent(final AjaxRequestTarget target)
			{
				// OWLTODO: we don't do this anymore, remove the whole event?
				//send(getPage(), Broadcast.BREADTH, new ScoreChangedEvent(studentUuid, -1L, target));

				//target.appendJavaScript(String.format("sakai.gradebookng.spreadsheet.refreshCourseGradeForStudent('%s')", studentUuid));
			}
		});
		
		gradeField.add(new RevertScoreBehavior(studentUuid, cellMarkupId)
		{
			@Override
			protected void onEvent(final AjaxRequestTarget target)
			{	
				// reset score
				gradeField.setDefaultModelObject(originalGrade);
				
				// reset styles and flags
				baseGradeStyle = FinalGradeColumn.CSS_CLASS;
				gradeSaveStyle = null;
				styleGradeCell(gradeField);
				notifications.clear();

				// apply any applicable flags
				refreshNotifications();

				// tell the javascript to refresh the cell
				GbUtils.getParentCellFor(getComponent(), PARENT_ID).ifPresent(target::add);
				target.add(page.updateLiveGradingMessage(getString("feedback.saved")));
			}
		});
		
		refreshNotifications();
		styleGradeCell(this);
		
		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu");
		menu.add(new GbAjaxLink<String>("finalGradeOverrideLog", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = page.getUpdateCourseGradeDisplayWindow();
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				CourseGradeOverrideLogPanel.ModelData data = new CourseGradeOverrideLogPanel.ModelData();
				data.studentUuid = getDefaultModelObjectAsString();
				window.setContent(new CourseGradeOverrideLogPanel(window.getContentId(), Model.of(data), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		add(menu);
	}
	
	protected String formatGrade(GbCourseGrade gbcg)
	{
		return FinalGradeFormatter.format(gbcg);
	}
	
	/**
	 * Builder for styling the cell. Aware of the cell 'save style' as well as if it has overrides and adds styles accordingly
	 *
	 * @param gradeField the cell to style
	 */
	private void styleGradeCell(final Component gradeField) {

		final ArrayList<String> cssClasses = new ArrayList<>();
		cssClasses.add(baseGradeStyle); // always
		if (model.getObject().getCourseGrade().getOverride().isPresent())
		{
			cssClasses.add("gb-cg-override");
		}
		if (gradeSaveStyle != null)
		{
			cssClasses.add(gradeSaveStyle.getCss()); // the particular style for this cell that has been computed previously
		}

		// replace the cell styles with the new set
		GbUtils.getParentCellFor(gradeField, PARENT_ID)
				.ifPresent(p -> p.add(AttributeModifier.replace("class", StringUtils.join(cssClasses, " "))));
	}
	
	private void markSuccessful(final Component gradeField)
	{
		gradeSaveStyle = GradeItemCellPanel.GradeCellSaveStyle.SUCCESS;
		styleGradeCell(gradeField);
	}

	private void markError(final Component gradeField)
	{
		gradeSaveStyle = GradeItemCellPanel.GradeCellSaveStyle.ERROR;
		styleGradeCell(gradeField);
		notifications.add(FinalGradeCellNotification.ERROR);
	}

	private void markWarning(final Component gradeField)
	{
		gradeSaveStyle = GradeItemCellPanel.GradeCellSaveStyle.WARNING;
		styleGradeCell(gradeField);
		notifications.add(FinalGradeCellNotification.INVALID);
	}
	
	private void refreshNotifications()
	{
		final WebMarkupContainer warningNotification = new WebMarkupContainer("warningNotification");
		final WebMarkupContainer errorNotification = new WebMarkupContainer("errorNotification");

		warningNotification.setVisible(false);
		errorNotification.setVisible(false);

		if (!notifications.isEmpty())
		{
			if (notifications.contains(FinalGradeCellNotification.ERROR))
			{
				errorNotification.setVisible(true);
				addPopover(errorNotification, notifications);
			}
			else if (notifications.contains(FinalGradeCellNotification.INVALID))
			{
				warningNotification.setVisible(true);
				addPopover(warningNotification, notifications);
			}
		}

		addOrReplace(warningNotification);
		addOrReplace(errorNotification);
	}
	
	private void addPopover(final Component component, final List<FinalGradeCellNotification> notifications)
	{
		FinalGradePopoverData data = new FinalGradePopoverData();
		data.notifications = notifications;
		data.studentUuid = model.getObject().getStudent().getUserUuid();
		FinalGradeItemPopoverPanel popover = new FinalGradeItemPopoverPanel("popover", Model.of(data));
		final String popoverString = ComponentRenderer.renderComponent(popover).toString();

		component.add(new AttributeModifier("data-toggle", "popover"));
		component.add(new AttributeModifier("data-trigger", "manual"));
		component.add(new AttributeModifier("data-placement", "bottom"));
		component.add(new AttributeModifier("data-html", "true"));
		component.add(new AttributeModifier("data-container", "#gradebookGrades"));
		component.add(new AttributeModifier("data-content", popoverString));
		component.add(new AttributeModifier("tabindex", "0"));
	}
}
