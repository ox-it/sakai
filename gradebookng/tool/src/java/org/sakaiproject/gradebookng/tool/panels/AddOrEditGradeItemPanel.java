package org.sakaiproject.gradebookng.tool.panels;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.component.SakaiAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * The panel for the add and edit grade item window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddOrEditGradeItemPanel extends Panel {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Long> model;
	private final ModalWindow window;

	/**
	 * How this panel is rendered
	 */
	enum Mode {
		ADD,
		EDIT;
	}

	Mode mode;

	public AddOrEditGradeItemPanel(final String id, final ModalWindow window, final IModel<Long> model) {
		super(id);
		this.model = model;
		this.window = window;

		// determine mode
		if (model != null) {
			mode = Mode.EDIT;
		} else {
			mode = Mode.ADD;
		}

		// setup the backing object
		Assignment assignment;

		if (mode == Mode.EDIT) {
			final Long assignmentId = model.getObject();
			assignment = businessService.getAssignment(assignmentId);

			// TODO if we are in edit mode and don't have an assignment, need to error here

		} else {
			// Mode.ADD
			assignment = new Assignment();
			// Default released to true
			assignment.setReleased(true);
			// If no categories, then default counted to true
			final Gradebook gradebook = businessService.getGradebook();
			assignment.setCounted(GradebookService.CATEGORY_TYPE_NO_CATEGORY == gradebook.getCategory_type());
		}

		// form model
		final Model<Assignment> formModel = new Model<>(assignment);

		// form
		final Form<Assignment> form = new Form<>("addOrEditGradeItemForm", formModel);

		final SakaiAjaxButton submit = new SakaiAjaxButton("submit", form) {

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final Assignment assignment = (Assignment) form.getModelObject();

				boolean validated = true;

				// PRE VALIDATION
				// 1. if category selected and drop/keep highest/lowest selected for that category,
				// ensure points match the already established maximum for the category.
				if (assignment.getCategoryId() != null) {
					final List<CategoryDefinition> categories = AddOrEditGradeItemPanel.this.businessService.getGradebookCategories();
					final CategoryDefinition category = categories
							.stream()
							.filter(c -> (Objects.equals(c.getId(), assignment.getCategoryId()))
									&& (c.getDropHighest() > 0 || c.getKeepHighest() > 0 || c.getDrop_lowest() > 0))
							.filter(c -> (c.getDropHighest() > 0 || c.getKeepHighest() > 0 || c.getDrop_lowest() > 0))
							.findFirst()
							.orElse(null);

					if (category != null) {
						final Assignment mismatched = category.getAssignmentList()
								.stream()
								.filter(a -> Double.compare(a.getPoints(), assignment.getPoints()) != 0)
								.findFirst()
								.orElse(null);
						if (mismatched != null) {
							validated = false;
							error(MessageFormat.format(getString("error.addeditgradeitem.categorypoints"), mismatched.getPoints()));
							target.addChildren(form, FeedbackPanel.class);
						}
					}
				}

				// 2. names cannot start with * or #
				if(validated && StringUtils.startsWithAny(assignment.getName(), new String[]{"*", "#"})) {
					validated = false;
					error(getString("error.addeditgradeitem.titlecharacters"));
					target.addChildren(form, FeedbackPanel.class);
				}

				// OK
				if (validated) {
					final IGradesPage gradebookPage = (IGradesPage) getPage();
					final BasePage basePage = (BasePage) getPage();
					Long assignmentId = null;
					if (AddOrEditGradeItemPanel.this.mode == Mode.EDIT) {

						final boolean success = AddOrEditGradeItemPanel.this.businessService.updateAssignment(assignment);
						assignmentId = assignment.getId();

						if (success) {

							// refresh
							AddOrEditGradeItemPanel.this.window.close(target);
							gradebookPage.setFocusedAssignmentID(assignmentId);
							gradebookPage.addOrReplaceTable(null);
							gradebookPage.redrawSpreadsheet(target);

							// display feedback
							basePage.success(MessageFormat.format(getString("message.edititem.success"), assignment.getName()));
							target.add(basePage.feedbackPanel);
						} else {
							error(new ResourceModel("message.edititem.error").getObject());
							target.addChildren(form, FeedbackPanel.class);
						}

					} else {

						boolean success = true;
						try {
							assignmentId = AddOrEditGradeItemPanel.this.businessService.addAssignment(assignment);
						} catch (final AssignmentHasIllegalPointsException e) {
							error(new ResourceModel("error.addgradeitem.points").getObject());
							success = false;
						} catch (final ConflictingAssignmentNameException e) {
							error(new ResourceModel("error.addgradeitem.title").getObject());
							success = false;
						} catch (final ConflictingExternalIdException e) {
							error(new ResourceModel("error.addgradeitem.exception").getObject());
							success = false;
						} catch (final Exception e) {
							error(new ResourceModel("error.addgradeitem.exception").getObject());
							success = false;
						}
						if (success) {

							// refresh
							AddOrEditGradeItemPanel.this.window.close(target);
							gradebookPage.setFocusedAssignmentID(assignmentId);
							gradebookPage.addOrReplaceTable(null);
							gradebookPage.redrawSpreadsheet(target);

							// display feedback
							basePage.success(MessageFormat.format(getString("notification.addgradeitem.success"), assignment.getName()));
							target.add(basePage.feedbackPanel);
							//setResponsePage(getPage().getPageClass(), new PageParameters().add(GradebookPage.CREATED_ASSIGNMENT_ID_PARAM, assignmentId));
						} else {
							target.addChildren(form, FeedbackPanel.class);
						}
					}
				}
			}

			@Override
			protected void onError(final AjaxRequestTarget target, final Form<?> form) {
				target.addChildren(form, FeedbackPanel.class);
			}
		};

		// submit button label
		submit.add(new Label("submitLabel", getSubmitButtonLabel()));
		form.add(submit);

		// add the common components
		form.add(new AddOrEditGradeItemPanelContent("subComponents", formModel));

		// feedback panel
		form.add(new GbFeedbackPanel("addGradeFeedback"));

		// cancel button
		final SakaiAjaxButton cancel = new SakaiAjaxButton("cancel") {

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				AddOrEditGradeItemPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		add(form);
	}

	/**
	 * Helper to get the model for the button
	 *
	 * @return
	 */
	private ResourceModel getSubmitButtonLabel() {
		if (mode == Mode.EDIT) {
			return new ResourceModel("button.savechanges");
		} else {
			return new ResourceModel("button.create");
		}
	}
}
