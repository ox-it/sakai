package org.sakaiproject.sitestats.tool.wicket.pages;

import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsDates;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.util.Tools;
import org.sakaiproject.sitestats.tool.wicket.components.IndicatingAjaxFallbackButton;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.components.UserTrackingResultsPanel;
import org.sakaiproject.sitestats.api.UserModel;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableToolIdListModel;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableUserIdListModel;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.FormattedText;

/**
 * @author plukasew
 */
public class UserTrackingPage extends BasePage
{
	private final String siteId;
	private final User user = new UserModel();
	private String tool = ReportManager.WHAT_EVENTS_ALLTOOLS;	// cannot be made final, though your IDE may tell you so; is bound to the tool filter drop down choice
	private Date startDate, endDate;
	private Button searchButton;
	private WebMarkupContainer lastJobRunContainer;

	public UserTrackingPage()
	{
		this(new PageParameters());
	}

	public UserTrackingPage(final PageParameters params)
	{
		siteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();

		if (!Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId))
		{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		Form form = new Form("form");
		add(form);
		add(new Menus("menu", siteId));

		lastJobRunContainer = new WebMarkupContainer("lastJobRunContainer");
		lastJobRunContainer.setOutputMarkupId(true);
		add(lastJobRunContainer);
		lastJobRunContainer.add(new LastJobRun("lastJobRun", siteId));

		IChoiceRenderer<UserModel> userChoiceRenderer = new ChoiceRenderer<UserModel>()
		{
			@Override
			public Object getDisplayValue(UserModel user)
			{
				// Short circuit if user is null
				if (user == null)
				{
					return new ResourceModel("user_unknown").getObject();
				}

				// String representation of 'select user' option
				if (ReportManager.WHO_NONE.equals(user.getId()))
				{
					return new ResourceModel("de_select_user").getObject();
				}

				return FormattedText.escapeHtml(user.getDisplayValue(), true);
			}

			@Override
			public String getIdValue(UserModel user, int index)
			{
				return user.getId();
			}
		};

		DropDownChoice<UserModel> userFilter = new DropDownChoice<>("userFilter", new PropertyModel<UserModel>(this, "user"),
				new LoadableUserIdListModel(siteId), userChoiceRenderer);
		userFilter.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				if (ReportManager.WHO_NONE.equals(user.getId()))
				{
					searchButton.setEnabled(false);
					target.add(searchButton);
				}
				else
				{
					searchButton.setEnabled(true);
					target.add(searchButton);
				}
			}
		});
		userFilter.setLabel(new ResourceModel("de_userFilter"));
		form.add(new SimpleFormComponentLabel("userFilterLabel", userFilter));
		form.add(userFilter);

		IChoiceRenderer<String> toolChoiceRenderer = new ChoiceRenderer<String>()
		{
			@Override
			public Object getDisplayValue(String toolId)
			{
				return Locator.getFacade().getEventRegistryService().getToolName(toolId);
			}

			@Override
			public String getIdValue(String toolId, int index)
			{
				return toolId;
			}
		};
		DropDownChoice<String> eventFilterByTool = new DropDownChoice<>("eventFilter",
				new PropertyModel<String>(this, "tool"), new LoadableToolIdListModel(siteId), toolChoiceRenderer);
		eventFilterByTool.setLabel(new ResourceModel("de_eventFilter"));
		form.add(new SimpleFormComponentLabel("eventFilterLabel", eventFilterByTool));
		form.add(eventFilterByTool);

		startDate = StatsDates.clearTimeFromDate(new Date()).getTime();
		DateTimeField startDateField = new DateTimeField("startDate", new PropertyModel<>(this, "startDate"))
		{
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel)
			{
				return new DateTextField(id, dateFieldModel, new StyleDateConverter("M-", true));
			}
		};
		startDateField.setLabel(new ResourceModel("de_startDate"));
		form.add(new SimpleFormComponentLabel("startDateLabel", startDateField));
		IModel<String> timeZoneModel = new Model<>("(" + TimeZone.getDefault().getDisplayName() + ")");
		form.add(new Label("startTimeZone", timeZoneModel));
		form.add(startDateField);

		endDate = StatsDates.nextDay(new Date());
		DateTimeField endDateField = new DateTimeField("endDate", new PropertyModel<>(this, "endDate"))
		{
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel)
			{
				return new DateTextField(id, dateFieldModel, new StyleDateConverter("M-", true));
			}
		};
		endDateField.setLabel(new ResourceModel("de_endDate"));
		form.add(new SimpleFormComponentLabel("endDateLabel", endDateField));
		form.add(new Label("endTimeZone", timeZoneModel));
		form.add(endDateField);

		final UserTrackingResultsPanel resultsPanel = new UserTrackingResultsPanel("results", TrackingParams.EMPTY_PARAMS);
		resultsPanel.setOutputMarkupPlaceholderTag(true);
		resultsPanel.setVisible(false);
		add(resultsPanel);

		searchButton = new IndicatingAjaxFallbackButton("search", form)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form)
			{
				// run search
				PrefsData pd = Locator.getFacade().getStatsManager().getPreferences(siteId, false);
				TrackingParams params = new TrackingParams(siteId, Tools.getEventsForToolFilter(tool, siteId, pd, true),
						Collections.singletonList(user.getId()), startDate, endDate);
				resultsPanel.setTrackingParams(params);
				resultsPanel.setVisible(true);
				target.add(resultsPanel);
				lastJobRunContainer.replace(new LastJobRun("lastJobRun", siteId));
				target.add(lastJobRunContainer);
				target.appendJavaScript(BasePage.NO_SCROLLBAR);
			}
		};
		searchButton.setEnabled(false);
		form.add(searchButton);
	}	// onInitialize()
}
