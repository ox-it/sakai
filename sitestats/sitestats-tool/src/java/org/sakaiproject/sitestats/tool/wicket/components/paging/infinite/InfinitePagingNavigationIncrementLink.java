package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.sakaiproject.sitestats.tool.wicket.pages.BasePage;

/**
 * @author plukasew
 */
public class InfinitePagingNavigationIncrementLink<T> extends Link<T> implements IAjaxLink, IAjaxIndicatorAware
{
	protected final InfinitePagingDataTable table;
	private final boolean increment;
	private final AjaxIndicatorAppender indicator = new AjaxIndicatorAppender();

	public InfinitePagingNavigationIncrementLink(final String id, final InfinitePagingDataTable table, final boolean increment)
	{
		super(id);
		this.increment = increment;
		this.table = table;

		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(new AjaxInfinitePagingNavigationBehavior(this, "click"));
		add(indicator);
	}

	@Override
	public void onClick()
	{
		onClick(null);
	}

	@Override
	public void onClick(AjaxRequestTarget target)
	{
		if (increment)
		{
			table.nextPage();
		}
		else
		{
			table.prevPage();
		}

		if (target != null)
		{
			target.add(table);
			target.appendJavaScript(BasePage.NO_SCROLLBAR);
		}
	}

	@Override
	public void onConfigure()
	{
		setEnabled(increment && table.hasNextPage() || !increment && table.hasPrevPage());
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.remove("onclick");
	}

	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return indicator.getMarkupId();
	}

	public static class AjaxInfinitePagingNavigationBehavior extends AjaxEventBehavior
	{
		private final IAjaxLink owner;

		public AjaxInfinitePagingNavigationBehavior(IAjaxLink owner, String event)
		{
			super(event);
			this.owner = owner;
		}

		@Override
		protected void onEvent(AjaxRequestTarget target)
		{
			owner.onClick(target);
		}

		@Override
		protected void onComponentTag(ComponentTag tag)
		{
			if (getComponent().isEnabledInHierarchy())
			{
				super.onComponentTag(tag);
			}
		}
	}
}
