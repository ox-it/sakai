package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter.FormatterConfig;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.component.SakaiAjaxButton;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Panel for the course grade override window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class CourseGradeOverridePanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public CourseGradeOverridePanel(final String id, final IModel<String> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final String studentUuid = (String) getDefaultModelObject();

		// get the rest of the data we need
		// TODO this could all be passed in through the model if it was changed to a map, as per CourseGradeItemCellPanel...
		final GbUser studentUser = this.businessService.getUser(studentUuid);
		final String currentUserUuid = this.businessService.getCurrentUser().getId();
		final GbRole currentUserRole = this.businessService.getUserRole();
		final Gradebook gradebook = this.businessService.getGradebook();
		final boolean courseGradeVisible = this.businessService.isCourseGradeVisible(currentUserUuid);

		final CourseGrade courseGrade = this.businessService.getCourseGrade(studentUuid);

		IGradesPage gradebookPage = (IGradesPage) getPage();
		GradebookUiSettings settings = gradebookPage.getUiSettings();
		boolean isContextAnonymous = settings.isContextAnonymous();

		StringResourceModel titleModel;
		if (isContextAnonymous)
		{
			titleModel = new StringResourceModel("heading.coursegrade.anonymous", null, new Object[]{ studentUser.getAnonId(settings) });
		}
		else
		{
			titleModel = new StringResourceModel("heading.coursegrade", null, new Object[]{ studentUser.getDisplayName(), studentUser.getDisplayId() });
		}
		// heading
		CourseGradeOverridePanel.this.window.setTitle(titleModel.getString());

		// form model
		// we are only dealing with the 'entered grade' so we use this directly
		final Model<String> formModel = new Model<String>(courseGrade.getEnteredGrade());

		// form
		final Form<String> form = new Form<String>("form", formModel);

		Component studentNameHeader = new Label("studentNameHeader", new ResourceModel("column.header.coursegradeoverride.studentname"))
			.setVisible(!isContextAnonymous);
		Component studentIdHeader = new Label("studentIdHeader", new ResourceModel("column.header.coursegradeoverride.studentid"))
			.setVisible(!isContextAnonymous);
		Component anonIdHeader = new Label("anonIdHeader", new ResourceModel("column.header.coursegradeoverride.anonid"))
			.setVisible(isContextAnonymous);
		form.add(studentNameHeader);
		form.add(studentIdHeader);
		form.add(anonIdHeader);

		Component lblStudentName = new Label("studentName", studentUser.getDisplayName()).setVisible(!isContextAnonymous)
			.setVisible(!isContextAnonymous);
		Component lblStudentId = new Label("studentEid", studentUser.getDisplayId()).setVisible(!isContextAnonymous)
			.setVisible(!isContextAnonymous);
		String anonId = settings.isContextAnonymous() ? studentUser.getAnonId(settings) : "";
		Component lblAnonId = new Label("anonId", anonId).setVisible(isContextAnonymous)
			.setVisible(isContextAnonymous);
		form.add(lblStudentName);
		form.add(lblStudentId);
		form.add(lblAnonId);


		form.add(new Label("points", formatPoints(courseGrade, gradebook)));
		
		final FormatterConfig config = new FormatterConfig();
		config.isCourseGradeVisible = courseGradeVisible;
		config.showPoints = false;
		config.showOverride = false;
		config.showLetterGrade = false;
		final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(gradebook, currentUserRole, config);
		form.add(new Label("calculated", courseGradeFormatter.format(courseGrade)));

		final TextField<String> overrideField = new TextField<>("overrideGrade", formModel);
		overrideField.setOutputMarkupId(true);
		form.add(overrideField);

		final SakaiAjaxButton submit = new SakaiAjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final String newGrade = (String) form.getModelObject();

				// validate the grade entered is a valid one for the selected grading schema
				// though we allow blank grades so the override is removed
				if (StringUtils.isNotBlank(newGrade)) {
					final GradebookInformation gbInfo = CourseGradeOverridePanel.this.businessService.getGradebookSettings();

					final Map<String, Double> schema = gbInfo.getSelectedGradingScaleBottomPercents();
					if (!schema.containsKey(newGrade)) {
						error(new ResourceModel("message.addcoursegradeoverride.invalid").getObject());
						target.addChildren(form, FeedbackPanel.class);
						return;
					}
				}

				// save
				final boolean success = CourseGradeOverridePanel.this.businessService.updateCourseGrade(studentUuid, newGrade);

				if (success) {
					getSession().success(getString("message.addcoursegradeoverride.success"));
					setResponsePage(getPage().getPageClass());
				} else {
					error(new ResourceModel("message.addcoursegradeoverride.error").getObject());
					target.addChildren(form, FeedbackPanel.class);
				}

			}
		};
		form.add(submit);

		// feedback panel
		form.add(new GbFeedbackPanel("feedback"));

		// cancel button
		final SakaiAjaxButton cancel = new SakaiAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> f) {
				CourseGradeOverridePanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);
		
		// override log link
		form.add(new GbAjaxLink<String>("courseGradeOverrideLog", Model.of(studentUuid)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = gradebookPage.getGradeOverrideLogWindow();
				window.setComponentToReturnFocusTo(CourseGradeOverridePanel.this);
				CourseGradeOverrideLogPanel.ModelData data = new CourseGradeOverrideLogPanel.ModelData();
				data.studentUuid = getDefaultModelObjectAsString();
				window.setContent(new CourseGradeOverrideLogPanel(window.getContentId(), Model.of(data), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
		});
		

		// revert link
		final AjaxSubmitLink revertLink = new AjaxSubmitLink("revertOverride", form) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> f) {
				final boolean success = CourseGradeOverridePanel.this.businessService.updateCourseGrade(studentUuid, null);
				if (success) {
					getSession().success(getString("message.addcoursegradeoverride.success"));
					setResponsePage(getPage().getPageClass());
				} else {
					error(new ResourceModel("message.addcoursegradeoverride.error").getObject());
					target.addChildren(form, FeedbackPanel.class);
				}
			}

			@Override
			public boolean isVisible() {
				return StringUtils.isNotBlank(formModel.getObject());
			}
		};
		revertLink.setDefaultFormProcessing(false);
		form.add(revertLink);

		add(form);
	}

	/**
	 * Helper to format the points display
	 *
	 * @param courseGrade the {@link CourseGrade}
	 * @param gradebook the {@link Gradebook} which has the settings
	 * @return fully formatted string ready for display
	 */
	private String formatPoints(final CourseGrade courseGrade, final Gradebook gradebook) {

		String rval;
		
		// only display points if not weighted category type
		final GbCategoryType categoryType = GbCategoryType.valueOf(gradebook.getCategory_type());
		if (categoryType != GbCategoryType.WEIGHTED_CATEGORY) {

			final Double pointsEarned = courseGrade.getPointsEarned();
			final Double totalPointsPossible = courseGrade.getTotalPointsPossible();
			
			if(pointsEarned != null && totalPointsPossible != null) {
				rval = new StringResourceModel("coursegrade.display.points-first", null,
						new Object[] { pointsEarned, totalPointsPossible }).getString();
			} else {
				rval = getString("coursegrade.display.points-none");
			}
		} else {
			rval = getString("coursegrade.display.points-none");
		}
		
		return rval;

	}

}
