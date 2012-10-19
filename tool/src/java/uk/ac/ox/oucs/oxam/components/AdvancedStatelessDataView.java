package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.PageParameters;

/**
 * This tells the provider in advance what should be the first item and how many to get.
 * This class breaks setCurrentPage(int) as it's final and I can't override it.
 * 
 * @see AdvancedIDataProvider
 * @author buckett
 *
 * @param <T>
 */
public abstract class AdvancedStatelessDataView<T> extends StatelessDataView<T> {

	private static final long serialVersionUID = 1L;
	private AdvancedIDataProvider<T> dataProvider;

	public AdvancedStatelessDataView(String id, AdvancedIDataProvider<T> dataProvider,
			int itemsPerPage, PageParameters pp) {
		super(id, dataProvider, pp);
		this.dataProvider = dataProvider;
		setItemsPerPage(itemsPerPage);
		
		// This all needs to happen early so that when the search is done the page is set correctly.
		
		// This is here so that we can call setItemPerPage.
		if (pp.getString(id) != null) {
			int pageNum = getPageNumber(pp.getString(id));
			if (pageNum != -1 && pageNum >= 0) {
				// This is the crucial bit, tell the provider in advance which page we are on.
				getDataProvider().setFirst(pageNum * internalGetRowsPerPage());
				if (pageNum <= getPageCount()) {
					setCurrentPage(getPageNumber(pp.getString(id)));
				}
			}
		}
	}
	
	public AdvancedIDataProvider<T> getDataProvider() {
		return dataProvider;
	}
	
	public void setItemsPerPage(int count) {
		super.setItemsPerPage(count);
		dataProvider.setCount(count);
	}

}
