package org.sakaiproject.sitestats.tool.wicket.models;

import org.sakaiproject.sitestats.api.UserModel;
import java.util.List;
import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.sitestats.tool.facade.Locator;

/**
 * @author plukasew, bjones86
 */
public class LoadableUserIdListModel extends LoadableDetachableModel<List<UserModel>>
{
	private final String siteId;

	public LoadableUserIdListModel(String siteId)
	{
		this.siteId = siteId;
	}

	@Override
	protected List<UserModel> load()
	{
		return Locator.getFacade().getDetailedEventsManager().getUsersForTracking(siteId);
	}
}
