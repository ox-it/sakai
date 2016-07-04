package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * @see http://letsgetdugg.com/2009/05/27/wicket-stateless-pagination/
 *
 */
public class SimplePagingNavigator extends PagingNavigator {
	private static final long serialVersionUID = 1L;

	private int viewsize = 0;
	private boolean anchorSelf = false;

	public SimplePagingNavigator(final String id, final IPageable pageable,
			final int viewsize) {
		this(id, pageable, viewsize, false);
	}

	public SimplePagingNavigator(final String id, final IPageable pageable,
			final int viewsize, final boolean anchorSelf) {
		super(id, pageable);
		setOutputMarkupId(true);
		this.setViewsize(viewsize);
		this.setAnchorSelf(anchorSelf);
	}

	@Override
	protected void onBeforeRender() {
		if (get("navigation") != null) {
			remove("navigation");
		}
		if (get("prev") != null) {
			remove("prev");
		}
		if (get("next") != null) {
			remove("next");
		}
		super.onBeforeRender();
		if (get("first") != null) {
			remove("first");
		}
		if (get("last") != null) {
			remove("last");
		}
		if (getViewsize() != 0) {
			getPagingNavigation().setViewSize(getViewsize());
		}
	}

	@Override
	protected PagingNavigation newNavigation(final IPageable pageable,
			final IPagingLabelProvider labelProvider) {
		PagingNavigation pg = new PagingNavigation("navigation", pageable,
				labelProvider) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Link newPagingNavigationLink(final String id,
					final IPageable pageable, final int pageIndex) {
				Link lnk = (Link) super.newPagingNavigationLink(id, pageable,
						pageIndex);
				if (isAnchorSelf()) {
					lnk.setAnchor(SimplePagingNavigator.this);
				}
				return lnk;
			}
		};
		pg.setViewSize(getViewsize());
		return pg;
	}

	@Override
	public boolean isVisible() {
		if (getPageable() != null) {
			return (getPageable().getPageCount() > 1);
		}
		return true;
	}

	public void setAnchorSelf(final boolean anchorSelf) {
		this.anchorSelf = anchorSelf;
	}

	public boolean isAnchorSelf() {
		return anchorSelf;
	}

	public void setViewsize(final int viewsize) {
		this.viewsize = viewsize;
	}

	public int getViewsize() {
		return viewsize;
	}

}
