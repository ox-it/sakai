package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
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

	private final String yes = MessageHelper.getString("importExport.confirmation.yes");
	private final String no = MessageHelper.getString("importExport.confirmation.no");

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

		final List<ProcessedGradeItem> itemsToUpdate = importWizardModel.getItemsToUpdate();
		final List<ProcessedGradeItem> itemsToModify = importWizardModel.getItemsToModify();
		final Map<ProcessedGradeItem, Assignment> assignmentsToCreate = importWizardModel.getAssignmentsToCreate();

		final Form<?> form = new Form("form");
		add(form);

		// back button
		final SakaiAjaxButton backButton = new SakaiAjaxButton("backbutton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();
				page.updateFeedback(target);

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

				Set<ProcessedGradeItem> errorColumns = new HashSet<>();

				// Create new GB items
				Iterator<Map.Entry<ProcessedGradeItem, Assignment>> itAssignments = assignmentsToCreate.entrySet().iterator();
				while(itAssignments.hasNext()) {
					Map.Entry<ProcessedGradeItem, Assignment> entry = itAssignments.next();
					Assignment assignment = entry.getValue();

					Long assignmentId = null;
					try {
						assignmentId = GradeImportConfirmationStep.this.businessService.addAssignment(assignment);
						success(MessageHelper.getString("notification.addgradeitem.success", assignment.getName()));

						// set the processedGradeItem's itemId so we can later save scores from the spreadsheet
						ProcessedGradeItem pgi = entry.getKey();
						pgi.setItemId(assignmentId);

						// since it's new, add this item to the list of items that have grades that need to be written
						itemsToSave.add(pgi);

						// remove the item from the wizard so that we can't edit it again if there are validation errors on other items and we use the back button
						importWizardModel.getItemsToCreate().remove(pgi);
						importWizardModel.setStep(importWizardModel.getStep() - 1);
						importWizardModel.setTotalSteps(importWizardModel.getTotalSteps() - 1);
						itAssignments.remove();
					} catch (final AssignmentHasIllegalPointsException e) {
						error(new ResourceModel("error.addgradeitem.points").getObject());
						errors = true;
						errorColumns.add(entry.getKey());
					} catch (final ConflictingAssignmentNameException e) {
						String title = assignment.getName();
						if (!StringUtils.isBlank(title))
						{
							error(MessageHelper.getString("error.addgradeitem.title.duplicate", title));
						}
						else
						{
							error(new ResourceModel("error.addgradeitem.title").getObject());
						}
						errors = true;
						errorColumns.add(entry.getKey());
					} catch (final ConflictingExternalIdException e) {
						error(new ResourceModel("error.addgradeitem.exception").getObject());
						errors = true;
						errorColumns.add(entry.getKey());
					} catch (final Exception e) {
						error(new ResourceModel("error.addgradeitem.exception").getObject());
						errors = true;
						errorColumns.add(entry.getKey());
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
						errorColumns.add(item);
					}

					assignmentMap.put(StringUtils.trim(assignment.getName()), assignment.getId());
				}

				// add/update the data

				itemsToSave.addAll(itemsToUpdate);
				itemsToSave.addAll(itemsToModify);
				itemsToSave.removeAll(errorColumns);

				final Gradebook gradebook = businessService.getGradebook();
				for (ProcessedGradeItem processedGradeItem : itemsToSave) {
					log.debug("Processing item: {}", processedGradeItem);

					//get data

					// if its an update/modify, this will get the id
					Long assignmentId = processedGradeItem.getItemId();

					// a newly created assignment will have a null ID here and need a lookup from the map to get the ID
					if (assignmentId == null) {
						//if assignment title was modified, we need to use that instead
						final String assignmentTitle = StringUtils.trim((processedGradeItem.getAssignmentTitle() != null) ? processedGradeItem.getAssignmentTitle() : processedGradeItem.getItemTitle());

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

				final ImportExportPage page = (ImportExportPage) getPage();
				if (!errors) {
					// Clear any previous errors
					page.clearFeedback();
					getSession().success(getString("importExport.confirmation.success"));
					setResponsePage(GradebookPage.class);
				} else {
					// Present errors to the user
					page.updateFeedback(target);
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
		final boolean hasItemsToCreate = !assignmentsToCreate.isEmpty();
		final WebMarkupContainer gradesCreateContainer = new WebMarkupContainer("grades_create_container") {

			@Override
			public boolean isVisible() {
				return hasItemsToCreate;
			}
		};
		add(gradesCreateContainer);

		if (hasItemsToCreate) {
			final ListView<Assignment> createList = makeAssignmentsToCreateListView("grades_create", new ArrayList<>(assignmentsToCreate.values()));
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

	/**
	 * Helper to create a listview for what needs to be shown wrt new assignments
	 * @param markupId wicket markup id
	 * @param itemList list of Assignments populated by the item creation steps
	 */
	private ListView<Assignment> makeAssignmentsToCreateListView(final String markupId, final List<Assignment> itemList) {
		final ListView<Assignment> rval = new ListView<Assignment>(markupId, itemList) {
			@Override
			protected void populateItem(final ListItem<Assignment> item) {
				final Assignment assignment = item.getModelObject();

				String extraCredit = assignment.isExtraCredit() ? yes : no;
				String dueDate = FormatHelper.formatDate(assignment.getDueDate(), "");
				String releaseToStudents = assignment.isReleased() ? yes : no;
				String includeInCourseGrades = assignment.isCounted() ? yes : no;

				item.add(new Label("itemTitle", assignment.getName()));
				item.add(new Label("itemPointValue", assignment.getPoints()));
				item.add(new Label("extraCredit", extraCredit));
				item.add(new Label("dueDate", dueDate));
				item.add(new Label("releaseToStudents", releaseToStudents));
				item.add(new Label("includeInCourseGrades", includeInCourseGrades));
			}
		};

		return rval;
	}
}
