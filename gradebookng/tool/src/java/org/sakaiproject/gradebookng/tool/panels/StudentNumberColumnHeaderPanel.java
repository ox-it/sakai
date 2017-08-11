package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.tool.component.SakaiAjaxButton;
import org.sakaiproject.gradebookng.tool.component.table.columns.GbColumnSortToggleLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;

/**
 *
 * @author plukasew
 */
public class StudentNumberColumnHeaderPanel extends Panel
{	
	public StudentNumberColumnHeaderPanel(final String id)
	{
		super(id);
	}
	
	@Override
	public void onInitialize()
	{
		super.onInitialize();
		
		final IGradesPage gradebookPage = (IGradesPage) getPage();
		
		// title
		final GbColumnSortToggleLink title = new GbColumnSortToggleLink("title")
		{
			@Override
			public SortDirection getSort(GradebookUiSettings settings)
			{
				return settings.getStudentNumberSortOrder();
			}
			
			@Override
			public void setSort(GradebookUiSettings settings, SortDirection value)
			{
				settings.setStudentNumberSortOrder(value);
			}
		};
		
		title.add(new AttributeModifier("title", new ResourceModel("column.header.studentNumber")));
		title.add(new Label("label", new ResourceModel("column.header.studentNumber")));
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		SortDirection sort = settings.getStudentNumberSortOrder();
		if (sort != null)
		{
			title.add(new AttributeModifier("class", "gb-sort-" + sort.toString().toLowerCase()));
		}
		
		add(title);
		
		final Form<String> form = new Form<>("studentNumberFilterForm", Model.of(settings.getStudentNumberFilter()));
		add(form);
		
		final TextField<String> filterTextField = new TextField<>("studentNumberFilter", form.getModel());
		form.add(filterTextField);
		
		final SakaiAjaxButton clear = new SakaiAjaxButton("studentNumberFilterClear")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form)
			{
				// clear the student number filter
				final GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setStudentNumberFilter("");

				// save settings
				gradebookPage.setUiSettings(settings);
				
				// OWLTODO: refresh the provider here...eventually? will need a refactor of GradebookPage first...
				// gradebookPage.refresh();
				// target.add(gradebookPage.get("form"));
				
				// refresh
				gradebookPage.resetPaging();
				gradebookPage.redrawSpreadsheet(target);
				
			}
		};
		form.add(clear);
		
		final SakaiAjaxButton submit = new SakaiAjaxButton("studentNumberFilterButton")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form)
			{
				Object mo = form.getModelObject();
				String filterText = StringUtils.trimToEmpty((String) form.getModelObject());
				// set the student number filter
				final GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setStudentNumberFilter(filterText);

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				gradebookPage.resetPaging();
				gradebookPage.redrawSpreadsheet(target);
			}
		};
		form.add(submit);
	}
}
