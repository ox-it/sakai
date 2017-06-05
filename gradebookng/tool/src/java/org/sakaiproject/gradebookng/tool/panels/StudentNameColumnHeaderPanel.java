package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;

/**
 *
 * Header panel for the student name/eid
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<GbStudentNameSortOrder> model;

	public StudentNameColumnHeaderPanel(final String id, final IModel<GbStudentNameSortOrder> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final IGradesPage gradebookPage = (IGradesPage) getPage();

		// setup model
		final GbStudentNameSortOrder sortType = this.model.getObject();

		// title
		final GbAjaxLink<String> title = new GbAjaxLink<String>("title") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				// toggle the sort direction on each click
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				if (settings.isContextAnonymous())
				{
					SortDirection anonDir = settings.getAnonIdSortOrder();
					if (anonDir == null)
					{
						settings.setAnonIdSortOrder(SortDirection.getDefault());
					}
					else
					{
						
						settings.setAnonIdSortOrder(anonDir.toggle());
					}
				}
				else
				{
					// if null, set a default sort, otherwise toggle, save, refresh.
					if (settings.getStudentSortOrder() == null) {
						settings.setStudentSortOrder(SortDirection.getDefault());
					} else {
						final SortDirection sortOrder = settings.getStudentSortOrder();
						settings.setStudentSortOrder(sortOrder.toggle());
					}
				}

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				//setResponsePage(GradebookPage.class);
				gradebookPage.redrawSpreadsheet(target);
			}
		};

		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		title.add(new AttributeModifier("title", new ResourceModel("column.header.students")));
		title.add(new Label("label", new ResourceModel("column.header.students")));
		if (settings != null && settings.getStudentSortOrder() != null) {
			title.add(
				new AttributeModifier("class", "gb-sort-" + settings.getStudentSortOrder().toString().toLowerCase()));
		}
		add(title);
		
		final Form<String> form = new Form<>("studentFilterForm", Model.of(settings.getStudentFilter()));
		boolean isContextAnonymous = settings.isContextAnonymous();
		form.setVisible(!isContextAnonymous);
		add(form);
		
		final TextField<String> filterTextField = new TextField<>("studentFilter", form.getModel());
		form.add(filterTextField);
		
		final GbAjaxButton clear = new GbAjaxButton("studentFilterClear")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form)
			{
				// clear the student number filter
				final GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setStudentFilter("");

				// save settings
				gradebookPage.setUiSettings(settings);
				
				// OWLTODO: refresh the provider here...eventually? will need a refactor of GradebookPage first...
				// gradebookPage.refresh();
				// target.add(gradebookPage.get("form"));
				
				// refresh
				//setResponsePage(GradebookPage.class);
				gradebookPage.redrawSpreadsheet(target);
				
			}
		};
		form.add(clear);
		
		final GbAjaxButton submit = new GbAjaxButton("studentFilterButton")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form)
			{
				String filterText = StringUtils.trimToEmpty((String) form.getModelObject());
				// set the student number filter
				final GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setStudentFilter(filterText);

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				//setResponsePage(GradebookPage.class);
				gradebookPage.redrawSpreadsheet(target);
			}
		};
		form.add(submit);

		// sort by first/last name link
		final GbAjaxLink<GbStudentNameSortOrder> sortByName = new GbAjaxLink<GbStudentNameSortOrder>("sortByName", this.model) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				// get current sort
				final GbStudentNameSortOrder currentSort = StudentNameColumnHeaderPanel.this.model.getObject();

				// get next
				final GbStudentNameSortOrder newSort = currentSort.toggle();

				// set the sort
				final IGradesPage gradebookPage = (IGradesPage) getPage();
				final GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setNameSortOrder(newSort);

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				//setResponsePage(GradebookPage.class);
				gradebookPage.redrawSpreadsheet(target);

			}
		};

		// the label changes depending on the state so we wrap it in a model
		final IModel<String> sortByNameModel = new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {

				// shows the label opposite to the current sort type
				if (settings.isContextAnonymous())
				{
					return getString("sortbyname.option.anonymous");
				}
				else if (sortType == GbStudentNameSortOrder.FIRST_NAME) {
					return getString("sortbyname.option.last");
				} else {
					return getString("sortbyname.option.first");
				}
			}
		};

		sortByName.add(new Label("sortByNameLabel", sortByNameModel));
		add(sortByName);
	}
}
