package uk.ac.ox.it.shoal.components;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @link http://letsgetdugg.com/2009/05/27/wicket-stateless-pagination/
 */
public class StatelessSimplePagingNavigator<T extends Page> extends SimplePagingNavigator {
    private static final long serialVersionUID = 1L;
    // The page we're building links to.
    private Class<T> clazz;
    private PageParameters pp;
    private IPageable pageable;

    @Override
    protected boolean getStatelessHint() {
        return true;
    }

    public StatelessSimplePagingNavigator(final String id, final Class<T> clazz,
                                          final PageParameters pp, final IPageable pageable,
                                          final int viewsize) {
        this(id, clazz, pp, pageable, viewsize, false);
    }

    public StatelessSimplePagingNavigator(final String id, final Class<T> clazz,
                                          final PageParameters pp, final IPageable pageable,
                                          final int viewsize, final boolean anchorSelf) {
        super(id, pageable, viewsize, anchorSelf);
        this.clazz = clazz;
        this.pp = pp;
        this.pageable = pageable;
    }

    @Override
    protected PagingNavigation newNavigation(final String id, final IPageable pageable,
                                             final IPagingLabelProvider labelProvider) {
        PagingNavigation pg = new PagingNavigation(id, pageable, labelProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Link<Void> newPagingNavigationLink(final String id,
                                                         final IPageable pageable, final long nextPage) {
                return newPageLink(id, pageable, nextPage);
            }
        };
        pg.setViewSize(getViewSize());
        return pg;
    }

    @Override
    protected Link<Void> newPagingNavigationIncrementLink(final String id,
                                                          final IPageable pageable, final int increment) {
        // Don't want to go before zero
        final long nextPage = constrainPageNumber(Math.max(0, pageable.getCurrentPage() + increment));
        return newPageLink(id, pageable, nextPage);
    }

    @Override
    protected Link<Void> newPagingNavigationLink(final String id,
                                                 final IPageable pageable, final int pageNumber) {
        final long nextPage = constrainPageNumber(pageNumber);
        return newPageLink(id, pageable, nextPage);
    }

    private BookmarkablePageLink<Void> newPageLink(final String id, final IPageable pageable, final long nextPage) {
        BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(id, clazz, new PageParameters(pp)) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return nextPage != pageable.getCurrentPage();
            }
        };
        link.getPageParameters().set("items", nextPage);
        return link;
    }

    /**
     * Allows the link to cull the page number to the valid range before it is retrieved from the
     * link
     *
     * @param pageNumber The page number, negative numbers are relative to the end of the list.
     * @return constrained page number
     */
    protected long constrainPageNumber(long pageNumber) {
        long idx = pageNumber;
        if (idx < 0) {
            idx = pageable.getPageCount() + idx;
        }
        if (idx > (pageable.getPageCount() - 1)) {
            idx = pageable.getPageCount() - 1;
        }
        if (idx < 0) {
            idx = 0;
        }
        return idx;
    }

}