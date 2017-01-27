package uk.ac.ox.it.shoal.components;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * 
 * @link http://letsgetdugg.com/2009/05/27/wicket-stateless-pagination/
 *
 * @param <T>
 */
public abstract class StatelessDataView<T> extends DataView<T> {
	private static final long serialVersionUID = 1L;
	protected PageParameters pp;

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
		String s = pp.get(id).toString();
		if (s != null) {
			int pageNum = getPageNumber(s);
			if (pageNum != -1 && pageNum >= 0 && pageNum <= getPageCount()) {
				setCurrentPage(getPageNumber(s));
			}
		}
		super.onInitialize();
	}

}