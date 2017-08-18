package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.tool.component.dropdown.SakaiSpinnerDropDownChoice;
import org.sakaiproject.gradebookng.tool.component.dropdown.SakaiSpinningSelectOnChangeBehavior;
import org.sakaiproject.gradebookng.tool.component.table.GbSakaiPagerContainer;
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
	protected Label liveGradingFeedback;
	protected final boolean hasAssignmentsAndGrades;
	
	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService bus;
	
	public GbBaseGradesDisplayToolbar(String id, SakaiDataTable table, List<GbGroup> groups, final boolean hasAssignmentsAndGrades)
	{
		super(id);
		this.table = table;
		showGroupFilter = true;
		this.groups = groups;
		this.hasAssignmentsAndGrades = hasAssignmentsAndGrades;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		IGradesPage page = (IGradesPage) getPage();
		final GradebookUiSettings settings = page.getUiSettings();
		
		liveGradingFeedback = new Label("liveGradingFeedback", getString("feedback.saved"));
		liveGradingFeedback.setVisible(hasAssignmentsAndGrades);
		liveGradingFeedback.setOutputMarkupId(true);

		// add the 'saving...' message to the DOM as the JavaScript will
		// need to be the one that displays this message (Wicket will handle
		// the 'saved' and 'error' messages when a grade is changed
		liveGradingFeedback.add(new AttributeModifier("data-saving-message", getString("feedback.saving")));
		addOrReplace(liveGradingFeedback);
		
		// section and group dropdown
		showGroupFilter = showGroupFilter();
		if(showGroupFilter || !isGroupFilterAllowed())
		{
			handleShowGroupFilter();
		}
		else
		{
			add(new Label("groupFilterOnlyOne", groups.isEmpty() ? Model.of("") : Model.of(groups.get(0).getTitle())));
		}

		final SakaiSpinnerDropDownChoice<GbGroup> groupFilter = new SakaiSpinnerDropDownChoice<GbGroup>("groupFilter",
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
				},
				new StringResourceModel("filter.groups", this, null),
				new SakaiSpinningSelectOnChangeBehavior()
				{
					@Override
					protected void onUpdate(final AjaxRequestTarget target) {

						final GbGroup selected = (GbGroup) getFormComponent().getDefaultModelObject();

						// store selected group (null ok)
						final GradebookUiSettings settings = page.getUiSettings();
						settings.setGroupFilter(selected);
						page.setUiSettings(settings);

						// refresh
						page.redrawForGroupChange(target);
					}

				});
				
		configureGroupFilter(groupFilter, settings);

		add(groupFilter);
		
		add(pagerContainer());
	}
	
	protected GbSakaiPagerContainer pagerContainer()
	{
		return new GbSakaiPagerContainer("gradebookPager", table);
	}
	
	protected void configureGroupFilter(SakaiSpinnerDropDownChoice<GbGroup> groupFilter, GradebookUiSettings settings)
	{
		if (!groups.isEmpty())
		{
			// set selected group, or first item in list
			groupFilter.setModelObject((settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
		}
		groupFilter.select.setNullValid(false);
		groupFilter.setVisible(showGroupFilter);
	}

	/**
	 * A preliminary check to determine if the group filter is visible.
	 * For instance, it should be hidden on the grades page in the anonymous context, but always displayed on final grades where isGroupFilterVisibleForced is set
	 */
	protected boolean isGroupFilterAllowed()
	{
		GradebookUiSettings settings = ((IGradesPage)getPage()).getUiSettings();
		return settings.isGroupFilterVisibilityForced() || !settings.isContextAnonymous();
	}
	
	protected boolean showGroupFilter()
	{
		if (groups.size() > 1)
		{
			return isGroupFilterAllowed();
		}
		return false;
	}
	
	protected void handleShowGroupFilter()
	{
		add(new EmptyPanel("groupFilterOnlyOne").setVisible(false));
	}
	
	public Component updateLiveGradingMessage(final String message)
	{
		liveGradingFeedback.setDefaultModel(Model.of(message));

		return liveGradingFeedback;
	}
}
