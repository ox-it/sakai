package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * 
 * @see http://letsgetdugg.com/2009/05/27/wicket-stateless-pagination/
 *
 * @param <T>
 */
public abstract class StatelessDataView<T> extends DataView<T> {
	private static final long serialVersionUID = 1L;
	private PageParameters pp;

	@Override
	protected boolean getStatelessHint() {
		return true;
	}

	protected int getPageNumber(final String param) {
		String numResult = param;
		if (numResult.contains(".wicket-")) {
			numResult = numResult.substring(0, numResult.indexOf(".wicket-"));
		}
		return Integer.valueOf(numResult);
	}

	public StatelessDataView(final String id, final IDataProvider<T> dataProvider,
			final PageParameters pp) {
		super(id, dataProvider);
		this.pp = pp;
	}

	public StatelessDataView(final String id, final IDataProvider<T> dataProvider,
			final int itemsPerPage, final PageParameters pp) {
		super(id, dataProvider, itemsPerPage);
		this.pp = pp;
	}
	
	@Override
	public void onInitialize() {
		// This needs to happen before things like the PagingNavigator
		String id = getId();
		// This is here so that we can call setItemPerPage.
		if (pp.getString(id) != null) {
			int pageNum = getPageNumber(pp.getString(id));
			if (pageNum != -1 && pageNum >= 0 && pageNum <= getPageCount()) {
				setCurrentPage(getPageNumber(pp.getString(id)));
			}
		}
		super.onInitialize();
	}

}