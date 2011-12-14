package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

/**
 * 
 * @see http://letsgetdugg.com/2009/05/27/wicket-stateless-pagination/
 *
 */
public class StatelessSimplePagingNavigator extends SimplePagingNavigator {
	private static final long serialVersionUID = 1L;
	private Class clazz;
	private PageParameters pp;
	private IPageable pageable;
	private String anchor;

	@Override
	protected boolean getStatelessHint() {
		return true;
	}

	public StatelessSimplePagingNavigator(final String id, final Class clazz,
			final PageParameters pp, final IPageable pageable,
			final int viewsize) {
		this(id, clazz, pp, pageable, viewsize, false);
	}

	public StatelessSimplePagingNavigator(final String id, final Class clazz,
			final PageParameters pp, final IPageable pageable,
			final int viewsize, final boolean anchorSelf) {
		super(id, pageable, viewsize, anchorSelf);
		this.clazz = clazz;
		this.pp = pp;
		this.pageable = pageable;
	}

	public StatelessSimplePagingNavigator(final String id, final Class clazz,
			final PageParameters pp, final IPageable pageable,
			final int viewsize, final String anchor) {
		super(id, pageable, viewsize, false);
		this.clazz = clazz;
		this.pp = pp;
		this.pageable = pageable;
		this.anchor = anchor;
	}

	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		addOrReplace(newStatelessPagingNavigationLink("next", getPageableId(),
				getCurrentPage(), 1));
		addOrReplace(newStatelessPagingNavigationLink("prev", getPageableId(),
				getCurrentPage(), -1));
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
				Component c = (Component) pageable;
				// PageParameters p1 = new PageParameters();
				// p1.putAll(pp);
				//pp.put(c.getId(), String.valueOf(pageIndex));
				BookmarkablePageLink<Object> lnk = new BookmarkablePageLink<Object>(
						id, clazz, pp) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onComponentTag(final ComponentTag arg0) {
						super.onComponentTag(arg0);
						if (getAnchor() != null) {
							if (arg0.getString("href") != null) {
								String href = arg0.getString("href").toString();
								String atag = anchor.contains("#") ? anchor
										: "#" + anchor;
								arg0.put("href", href + atag);
							}
						}
					}

					@Override
					public boolean isEnabled() {
						return (getCurrentPage() != pageIndex);
					}
				};
				lnk.setParameter(c.getId(), pageIndex);
				if (isAnchorSelf()) {
					lnk.setAnchor(StatelessSimplePagingNavigator.this);
				}
				return lnk;
			}
		};
		pg.setViewSize(getViewsize());
		return pg;
	}

	@Override
	protected Link<Object> newPagingNavigationIncrementLink(final String id,
			final IPageable pageable, final int increment) {
		Component c = (Component) pageable;
		final int page = (pp.containsKey(c.getId()))?pp.getInt(c.getId()):0;
		//pp.put(c.getId(), String.valueOf(page + increment));
		BookmarkablePageLink<Object> link = new BookmarkablePageLink<Object>(id, clazz, pp) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (page + increment) >= 0;
			}
		};
		link.setParameter(c.getId(), page + increment);
		return link;
	}

	protected Link<Object> newStatelessPagingNavigationLink(final String id,
			final String pageableId, final int currentPage, final int increment) {
		// PageParameters p1 = new PageParameters();
		// p1.putAll(pp);
		//pp.put(pageableId, String.valueOf(currentPage + increment));
		BookmarkablePageLink link = new BookmarkablePageLink<Object>(id, clazz, pp) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (currentPage + increment) < getPageCount()
						&& (currentPage + increment) >= 0;
			}
		};
		link.setParameter(pageableId, currentPage + increment);
		return link;
	}

	@Override
	protected Link<Object> newPagingNavigationLink(final String id,
			final IPageable pageable, final int pageNumber) {
		Component c = (Component) pageable;
		// PageParameters p1 = new PageParameters();
		// p1.putAll(pp);
		//pp.put(c.getId(), String.valueOf(pageNumber));
		BookmarkablePageLink link = new BookmarkablePageLink(id, clazz, pp);
		return link;
	}

	private int getCurrentPage() {
		return pageable.getCurrentPage();
	}

	private int getPageCount() {
		return pageable.getPageCount();
	}

	private String getPageableId() {
		return ((Component)pageable).getId();
	}


}