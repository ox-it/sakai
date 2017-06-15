package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.tool.component.table.GbSakaiPagerContainer;
import org.sakaiproject.gradebookng.tool.component.table.IndicatingAjaxDropDownChoice;
import org.sakaiproject.gradebookng.tool.component.table.SakaiDataTable;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;

/**
 *
 * @author plukasew
 */
public class GbBaseGradesDisplayToolbar extends Panel
{
	protected final SakaiDataTable table;
	protected boolean showGroupFilter;
	protected List<GbGroup> groups;
	protected GbRole role;
	protected List<PermissionDefinition> permissions;
	
	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService bus;
	
	public GbBaseGradesDisplayToolbar(String id, SakaiDataTable table, List<GbGroup> groups)
	{
		super(id);
		this.table = table;
		showGroupFilter = true;
		this.groups = groups;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		IGradesPage page = (IGradesPage) getPage();
		final GradebookUiSettings settings = page.getUiSettings();
		
		// section and group dropdown
		if(showGroupFilter() || settings.isContextAnonymous())
		{
			handleShowGroupFilter();
		}
		else
		{
			add(new Label("groupFilterOnlyOne", groups.isEmpty() ? Model.of("") : Model.of(groups.get(0).getTitle())));
		}

		final IndicatingAjaxDropDownChoice<GbGroup> groupFilter = new IndicatingAjaxDropDownChoice<GbGroup>("groupFilter",
				new Model<GbGroup>(), groups,
				new ChoiceRenderer<GbGroup>()
				{
					@Override
					public Object getDisplayValue(final GbGroup g)
					{
						return g.getTitle();
					}

					@Override
					public String getIdValue(final GbGroup g, final int index)
					{
						return g.getId();
					}
				});
		groupFilter.setVisible(showGroupFilter && !settings.isContextAnonymous());

		groupFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				final GbGroup selected = (GbGroup) groupFilter.getDefaultModelObject();

				// store selected group (null ok)
				final GradebookUiSettings settings = page.getUiSettings();
				settings.setGroupFilter(selected);
				page.setUiSettings(settings);

				// refresh
				page.redrawForGroupChange(target);
			}

		});

		if (!groups.isEmpty())
		{
			// set selected group, or first item in list
			groupFilter.setModelObject((settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
		}
		groupFilter.setNullValid(false);
		groupFilter.setVisible(showGroupFilter);

		add(groupFilter);
		
		add(new GbSakaiPagerContainer("gradebookPager", table));
	}
	
	protected boolean showGroupFilter()
	{
		showGroupFilter = groups.size() > 1;
		return showGroupFilter;
	}
	
	protected void handleShowGroupFilter()
	{
		add(new EmptyPanel("groupFilterOnlyOne").setVisible(false));
	}
}
