package uk.ac.ox.oucs.oxam.pages;

import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import uk.ac.ox.oucs.oxam.components.AdvancedIDataProvider;
import uk.ac.ox.oucs.oxam.pages.SearchPage.Resolver;

class SolrProvider implements AdvancedIDataProvider<SolrDocument>
{

	private SolrServer solr;
	private String query;
	private String[] filters;

	public SolrProvider(SolrServer solr) {
		this.solr = solr;
	}
	
	public SolrProvider(SolrServer solr, String query, String[] filters) {
		this.solr = solr;
		this.query = query;
		this.filters = filters;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String[] getFilters() {
		return filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
	}


	private static final long serialVersionUID = 1L;
	private int first = 0;
	private int count = 10;
	private transient QueryResponse response;

	public void detach() {
	}

	public Iterator<SolrDocument> iterator(int first, int count) {
		if (first != this.first){
			// This happens if the user attempts to go beyond the last item (hacking the URL), or adds some extra
			// query search params without going back to the first page.
			throw new IllegalArgumentException("The first item has changed since the time we first searched.");
		}
		if (count > count) {
			throw new IllegalArgumentException("The count has increased since we first searched.");
		}
		doSearch();
		return response.getResults().iterator();
	}

	public int size() {
		doSearch();
		return (int) response.getResults().getNumFound();
	}

	public IModel<SolrDocument> model(SolrDocument object) {
		return new CompoundPropertyModel<SolrDocument>(object);
	}

	private void doSearch() {
		if (response != null) {
			return;
		}
		SolrQuery solrQuery = new SolrQuery().
				
				setQuery(query.length() > 0?query:"*:*").
				setStart(first).
				setRows(count).
				setFacet(true).
				//setFacetSort(FacetParams.FACET_SORT_INDEX).
				setFacetMinCount(1).
				setFacetLimit(SolrFacet.FACET_LIMIT+1). // So we know if we have too many.
				setSortField("sort_year", ORDER.desc).
				addFacetField("exam_code", "paper_code", "academic_year", "term");
		
		if (filters != null) {
			solrQuery.setFilterQueries(filters);
		}

		try {
			response = solr.query(solrQuery);
		} catch (SolrServerException sse) {
			// TODO Better exception and handling.
			throw new RuntimeException("Search Failed");
		}
	}
	
	public <T> Panel getFacet(String id, final String facet, final Resolver<T> resolver, final PageParameters pp) {
		if (response == null) {
			doSearch();
		}
		List<Count> values = response.getFacetField(facet).getValues();
		ResourceModel title = getFacetTitle(facet);
		Panel panel = (values == null)?new EmptyPanel(id):new SolrFacet<T>(id, facet, title, values, resolver, pp);
		return panel;
	}

	protected ResourceModel getFacetTitle(String facet) {
		return new ResourceModel("label.facet."+facet);
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public void setCount(int count) {
		this.count = count;
	}

}