package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanelContent;
import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.sakaiproject.gradebookng.tool.component.SakaiAjaxButton;

/**
 * Importer has detected that items need to be created so extract the data and wrap the 'AddOrEditGradeItemPanelContent' panel
 */
@Slf4j
public class CreateGradeItemStep extends Panel {

    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;

    private final String panelId;
    private final IModel<ImportWizardModel> model;

    PreviewImportedGradesPanel previewGradesPanel;

    public CreateGradeItemStep(final String id, final IModel<ImportWizardModel> importWizardModel) {
        super(id);
        panelId = id;
        model = importWizardModel;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        //unpack model
        final ImportWizardModel importWizardModel = this.model.getObject();

        final int step = importWizardModel.getStep();

        // original data
        final ProcessedGradeItem processedGradeItem = importWizardModel.getItemsToCreate().get(step - 1);

        // setup new assignment for populating
        Assignment assignmentFromModel = importWizardModel.getAssignmentsToCreate().get(processedGradeItem);
        boolean useSpreadsheetData = assignmentFromModel == null;
        // if using spreadsheet data, we'll create a blank assignment and fill the fields accordingly; otherwise, the assignment is already in the wizard (Ie. back button)
        final Assignment assignment = useSpreadsheetData ? new Assignment() : assignmentFromModel;
        if (useSpreadsheetData)
        {
            assignment.setName(StringUtils.trim(processedGradeItem.getItemTitle()));
            if(StringUtils.isNotBlank(processedGradeItem.getItemPointValue())) {
                assignment.setPoints(Double.parseDouble(processedGradeItem.getItemPointValue()));
            }
        }

        final Model<Assignment> assignmentModel = new Model<>(assignment);

        @SuppressWarnings("unchecked")
        final Form<Assignment> form = new Form("form", assignmentModel);
        add(form);
		
        final SakaiAjaxButton nextButton = new SakaiAjaxButton("nextbutton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                final Assignment a = (Assignment) form.getDefaultModel().getObject();
				
                //add to model
                importWizardModel.getAssignmentsToCreate().put(processedGradeItem, a);

                log.debug("Assignment: {}", assignment);

                // sync up the assignment data so we can present it for confirmation
                processedGradeItem.setAssignmentTitle(a.getName());
                processedGradeItem.setAssignmentPoints(a.getPoints());

                //Figure out if there are more steps
                //If so, go to the next step (ie do it all over again)
                Component newPanel;
                if (step < importWizardModel.getTotalSteps()) {
                    importWizardModel.setStep(step+1);
                    newPanel = new CreateGradeItemStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
                } else {
                    //If not, continue on in the wizard
                    newPanel = new GradeImportConfirmationStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
                }

                // clear any previous errors
                final ImportExportPage page = (ImportExportPage) getPage();
                page.clearFeedback();
                page.updateFeedback(target);

                // AJAX the new panel into place
                newPanel.setOutputMarkupId(true);
                WebMarkupContainer container = page.container;
                container.addOrReplace(newPanel);
                target.add(newPanel);
            }
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				final ImportExportPage page = (ImportExportPage) getPage();
				page.updateFeedback(target);
			}
					
        };
        form.add(nextButton);

        final SakaiAjaxButton backButton = new SakaiAjaxButton("backbutton") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {

                // clear any previous errors
                final ImportExportPage page = (ImportExportPage) getPage();
                page.clearFeedback();
                page.updateFeedback(target);

                // Create the previous panel
                Component previousPanel;
                if (step > 1) {
                    importWizardModel.setStep(step-1);
                    previousPanel = new CreateGradeItemStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
                }
                else {
                    // Reload everything. Rationale: final step can have partial success and partial failure. If content was imported from the spreadsheet, the item selection page should reflect this when we return to it
                    ImportGradesHelper.setupImportWizardModelForSelectionStep(page, CreateGradeItemStep.this, importWizardModel, businessService, target);

                    previousPanel = new GradeItemImportSelectionStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
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

        //wrap the form create panel
        form.add(new Label("createItemHeader", new StringResourceModel("importExport.createItem.heading", this, null, step, importWizardModel.getTotalSteps())));
        AddOrEditGradeItemPanelContent newItemPanel = new AddOrEditGradeItemPanelContent("subComponents", assignmentModel);
        newItemPanel.lockAnonymousToValue(importWizardModel.isContextAnonymous());
        form.add(newItemPanel);

        previewGradesPanel = new PreviewImportedGradesPanel("previewGradesPanel", model);
        form.add(previewGradesPanel);
    }
}
