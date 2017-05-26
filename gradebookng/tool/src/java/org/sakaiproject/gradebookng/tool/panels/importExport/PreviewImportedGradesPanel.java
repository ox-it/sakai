package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;

/**
 * This panel provides a table previewing the grades that will be imported for the current item in the {@link ImportWizardModel}.
 * 
 * @author bjones86
 */
public class PreviewImportedGradesPanel extends Panel
{
    IModel<ImportWizardModel> model;

    public PreviewImportedGradesPanel( final String id, final IModel<ImportWizardModel> importWizardModel )
    {
        super( id, importWizardModel );
        model = importWizardModel;
    }

    @Override
    public void onInitialize()
    {
        super.onInitialize();
        final ImportWizardModel importWizardModel = model.getObject();
        final ProcessedGradeItem processedGradeItem = importWizardModel.getItemsToCreate().get( importWizardModel.getStep() - 1 );

        // Create the accordion header
        final Label previewGradesHeader = new Label( "previewGradesHeader", new StringResourceModel( "importExport.selection.previewGrades.header",
                null, new Object[] {processedGradeItem.getItemTitle()} ) );

        // Add the accordion collapse/expand behaviour
        final WebMarkupContainer previewGradesPanel = new WebMarkupContainer( "previewGradesPanel" );
        previewGradesPanel.add( new AjaxEventBehavior( "shown.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                previewGradesPanel.add( new AttributeModifier( "class", "panel-collapse collapse in" ) );
            }
        });
        previewGradesPanel.add( new AjaxEventBehavior( "hidden.bs.collapse" )
        {
            @Override
            protected void onEvent( final AjaxRequestTarget ajaxRequestTarget )
            {
                previewGradesPanel.add( new AttributeModifier( "class", "panel-collapse collapse" ) );
            }
        });

        // Create and populate the list of grades that will be imported for the current item
        final WebMarkupContainer previewGradesContainer = new WebMarkupContainer( "previewGradesContainer" );
        final ListView<ProcessedGradeItemDetail> previewGrades = new ListView<ProcessedGradeItemDetail>( "previewGrades", processedGradeItem.getProcessedGradeItemDetails() )
        {
            @Override
            protected void populateItem( final ListItem<ProcessedGradeItemDetail> item )
            {
                final ProcessedGradeItemDetail details = item.getModelObject();
                final GbUser user = details.getUser();
                item.add( new Label( "studentID", user.getDisplayId() ) );
                item.add( new Label( "studentName", user.getDisplayName() ) );
                item.add( new Label( "studentGrade", details.getGrade() ) );
            }
        };

        // Add the components to the page
        previewGradesContainer.add( previewGrades );
        previewGradesPanel.add( previewGradesContainer );
        add( previewGradesHeader );
        add( previewGradesPanel );
    }
}
