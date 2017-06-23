package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.component.SakaiAjaxButton;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.tool.gradebook.Gradebook;


/**
 * Confirmation page for what is going to be imported
 */
@Slf4j
public class GradeImportConfirmationStep extends Panel {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private final String panelId;
	private final IModel<ImportWizardModel> model;

	public GradeImportConfirmationStep(final String id, final IModel<ImportWizardModel> importWizardModel) {
		super(id);
		panelId = id;
		model = importWizardModel;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final ImportWizardModel importWizardModel = this.model.getObject();

		final List<ProcessedGradeItem> itemsToCreate = importWizardModel.getItemsToCreate();
		final List<ProcessedGradeItem> itemsToUpdate = importWizardModel.getItemsToUpdate();
		final List<ProcessedGradeItem> itemsToModify = importWizardModel.getItemsToModify();
		final List<Assignment> assignmentsToCreate = importWizardModel.getAssignmentsToCreate();

		final Form<?> form = new Form("form");
		add(form);

		// back button
		final SakaiAjaxButton backButton = new SakaiAjaxButton("backbutton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();
				target.add(page.feedbackPanel);

				// Create the previous panel
				Component previousPanel;
				if (assignmentsToCreate.size() > 0) {
					previousPanel = new CreateGradeItemStep(GradeImportConfirmationStep.this.panelId, Model.of(importWizardModel));
				} else {
					previousPanel = new GradeItemImportSelectionStep(GradeImportConfirmationStep.this.panelId, Model.of(importWizardModel));
				}

				// AJAX the previous panel into place
				previousPanel.setOutputMarkupId(true);
				WebMarkupContainer container = page.container;
				container.addOrReplace(previousPanel);
				target.add(container);
			}
		};
		backButton.setDefaultFormProcessing(false);
		form.add(backButton);

		// finish button
		final SakaiAjaxButton finishButton = new SakaiAjaxButton("finishbutton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {

				boolean errors = false;
				final Map<String, Long> assignmentMap = new HashMap<>();
				final List<ProcessedGradeItem> itemsToSave = new ArrayList<>();

				// Create new GB items
				for (Assignment assignment : assignmentsToCreate) {

					Long assignmentId = null;
					try {
						assignmentId = GradeImportConfirmationStep.this.businessService.addAssignment(assignment);
					} catch (final AssignmentHasIllegalPointsException e) {
						error(new ResourceModel("error.addgradeitem.points").getObject());
						errors = true;
					} catch (final ConflictingAssignmentNameException e) {
						error(new ResourceModel("error.addgradeitem.title").getObject());
						errors = true;
					} catch (final ConflictingExternalIdException e) {
						error(new ResourceModel("error.addgradeitem.exception").getObject());
						errors = true;
					} catch (final Exception e) {
						error(new ResourceModel("error.addgradeitem.exception").getObject());
						errors = true;
					}

					assignmentMap.put(StringUtils.trim(assignment.getName()), assignmentId);
				}

				//Modify any that need modification
				for (ProcessedGradeItem item : itemsToModify) {

					final Double points = NumberUtils.toDouble(item.getItemPointValue());
					final Assignment assignment = businessService.getAssignment(item.getItemTitle());
					assignment.setPoints(points);

					final boolean updated = businessService.updateAssignment(assignment);
					if(!updated) {
						error(MessageHelper.getString("importExport.error.pointsmodification", assignment.getName()));
						errors = true;
					}

					assignmentMap.put(StringUtils.trim(assignment.getName()), assignment.getId());
				}

				// add/update the data
				if (!errors) {

					itemsToSave.addAll(itemsToUpdate);
					itemsToSave.addAll(itemsToCreate);
					itemsToSave.addAll(itemsToModify);

					final Gradebook gradebook = businessService.getGradebook();
					for (ProcessedGradeItem processedGradeItem : itemsToSave) {
						log.debug("Processing item: {}", processedGradeItem);

						//get data
						// if its an update/modify, this will get the id
						Long assignmentId = processedGradeItem.getItemId();

						//if assignment title was modified, we need to use that instead
						final String assignmentTitle = StringUtils.trim((processedGradeItem.getAssignmentTitle() != null) ? processedGradeItem.getAssignmentTitle() : processedGradeItem.getItemTitle());

						// a newly created assignment will have a null ID here and need a lookup from the map to get the ID
						if (assignmentId == null) {
							assignmentId = assignmentMap.get(assignmentTitle);
						}
						//TODO if assignmentId is still null, there will be a problem

						// Get the assignment
						final Assignment assignment = businessService.getAssignment(assignmentId);

						final List<ProcessedGradeItemDetail> processedGradeItemDetails = processedGradeItem.getProcessedGradeItemDetails();
						List<GradeDefinition> gradeDefList = new ArrayList<>();
						for (ProcessedGradeItemDetail processedGradeItemDetail : processedGradeItemDetails) {
							GradeDefinition gradeDef = new GradeDefinition();
							gradeDef.setStudentUid(processedGradeItemDetail.getUser().getUserUuid());
							gradeDef.setGrade(processedGradeItemDetail.getGrade());
							gradeDef.setGradeComment(processedGradeItemDetail.getComment());
							gradeDefList.add(gradeDef);
						}

						final GradeSaveResponse saveResponse = businessService.saveGradesAndCommentsForImport(gradebook, assignment, gradeDefList);
						switch (saveResponse) {
							case OK:
								break;
							case ERROR:
								error(new ResourceModel("importExport.error.grade").getObject());
								errors = true;
								break;
							default:
								break;
						}
					}
				}

				final ImportExportPage page = (ImportExportPage) getPage();
				if (!errors) {
					// Clear any previous errors
					page.clearFeedback();
					getSession().success(getString("importExport.confirmation.success"));
					setResponsePage(GradebookPage.class);
				} else {
					// Present errors to the user
					target.add(page.feedbackPanel);
				}
			}
		};
		form.add(finishButton);

		// render items to be updated
		final boolean hasItemsToUpdate = !itemsToUpdate.isEmpty();
		final WebMarkupContainer gradesUpdateContainer = new WebMarkupContainer("grades_update_container") {

			@Override
			public boolean isVisible() {
				return hasItemsToUpdate;
			}
		};
		add(gradesUpdateContainer);

		if (hasItemsToUpdate) {
			final ListView<ProcessedGradeItem> updateList = makeListView("grades_update", itemsToUpdate);
			updateList.setReuseItems(true);
			gradesUpdateContainer.add(updateList);
		}

		// render items to be created
		final boolean hasItemsToCreate = !itemsToCreate.isEmpty();
		final WebMarkupContainer gradesCreateContainer = new WebMarkupContainer("grades_create_container") {

			@Override
			public boolean isVisible() {
				return hasItemsToCreate;
			}
		};
		add(gradesCreateContainer);

		if (hasItemsToCreate) {
			final ListView<ProcessedGradeItem> createList = makeListView("grades_create", itemsToCreate);
			createList.setReuseItems(true);
			gradesCreateContainer.add(createList);
		}

		// render items to be created
		final boolean hasItemsToModify = !itemsToModify.isEmpty();
		final WebMarkupContainer gradesModifyContainer = new WebMarkupContainer("grades_modify_container") {

			@Override
			public boolean isVisible() {
				return hasItemsToModify;
			}
		};
		add(gradesModifyContainer);

		if (hasItemsToModify) {
			final ListView<ProcessedGradeItem> modifyList = makeListView("grades_modify", itemsToModify);
			modifyList.setReuseItems(true);
			gradesModifyContainer.add(modifyList);
		}
	}

	/**
	 * Helper to create a listview for what needs to be shown
	 * @param markupId wicket markup id
	 * @param itemList ist of stuff
	 * @return
	 */
	private ListView<ProcessedGradeItem> makeListView(final String markupId, final List<ProcessedGradeItem> itemList) {

		final ListView<ProcessedGradeItem> rval = new ListView<ProcessedGradeItem>(markupId, itemList) {

			@Override
			protected void populateItem(final ListItem<ProcessedGradeItem> item) {

				final ProcessedGradeItem gradeItem = item.getModelObject();

				// ensure we display the edited data if we have it (won't exist for an update)
				final String assignmentTitle = gradeItem.getAssignmentTitle();
				final Double assignmentPoints = gradeItem.getAssignmentPoints();

				item.add(new Label("itemTitle", (assignmentTitle != null) ? assignmentTitle : gradeItem.getItemTitle()));
				item.add(new Label("itemPointValue", (assignmentPoints != null) ? assignmentPoints : gradeItem.getItemPointValue()));

				//if comment and it's being updated, add additional row
				if (gradeItem.getType() == ProcessedGradeItem.Type.COMMENT && gradeItem.getCommentStatus().getStatusCode() != ProcessedGradeItemStatus.STATUS_NA) {

					item.add(new Behavior() {

						@Override
						public void afterRender(final Component component) {
							super.afterRender(component);
							component.getResponse().write("<tr class=\"comment\"><td class=\"item_title\" colspan=\"2\"><span>" + getString("importExport.commentname") + "</span></td></tr>");
						}
					});
				}
			}
		};

		return rval;
	}
}
