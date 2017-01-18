package uk.ac.ox.it.shoal.components;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * @link http://letsgetdugg.com/2009/05/27/wicket-stateless-pagination/
 *
 */
public class SimplePagingNavigator extends PagingNavigator {
	private static final long serialVersionUID = 1L;

	private int viewSize = 0;
	private boolean anchorSelf = false;

	public SimplePagingNavigator(final String id, final IPageable pageable,
			final int viewSize) {
		this(id, pageable, viewSize, false);
	}

	public SimplePagingNavigator(final String id, final IPageable pageable,
								 final int viewSize, final boolean anchorSelf) {
		super(id, pageable);
		setOutputMarkupId(true);
		this.setViewSize(viewSize);
		this.setAnchorSelf(anchorSelf);
	}

	@Override
	protected PagingNavigation newNavigation(final String id, final IPageable pageable,
			final IPagingLabelProvider labelProvider) {
		PagingNavigation pg = new PagingNavigation(id, pageable,
				labelProvider) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Link newPagingNavigationLink(final String id,
					final IPageable pageable, final long pageIndex) {
				Link lnk = (Link) super.newPagingNavigationLink(id, pageable,
						pageIndex);
				if (isAnchorSelf()) {
					lnk.setAnchor(SimplePagingNavigator.this);
				}
				return lnk;
			}
		};
		pg.setViewSize(getViewSize());
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

	public void setViewSize(final int viewSize) {
		this.viewSize = viewSize;
	}

	public int getViewSize() {
		return viewSize;
	}

}
