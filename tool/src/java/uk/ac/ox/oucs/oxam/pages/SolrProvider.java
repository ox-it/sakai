package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.FacetParams;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

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
			// This happens if the user attempts to go beyond the last item (hacking the URL)
			throw new IllegalArgumentException("The first item has changed since the time we first searched.");
		}
		if (count > count) {
			throw new IllegalArgumentException("The count has increase since we first searched.");
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
				setFacetLimit(10).
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
	
	public <T> ListView<Count> getFacet(String id, final String facet, final Resolver<T> resolver, final PageParameters pp) {
		if (response == null) {
			doSearch();
		}
		final Map<String, T> displayValues;
		List<Count> values = response.getFacetField(facet).getValues();
		if (resolver != null && values != null) {
			ArrayList<String> lookups = new ArrayList<String>();
			for (Count value: values) {
				lookups.add(value.getName());
			}
			 displayValues = resolver.lookup(lookups);
		} else {
			displayValues = Collections.emptyMap();
		}
		
		return new ListView<Count>(id, values) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<Count> item) {
				Count count = item.getModelObject();
				String displayValue = (displayValues.containsKey((count.getName())))?
						resolver.display(displayValues.get(count.getName())):
						count.getName();
				
				// TODO BookmarkablePageLink needs a nice way to add params which doesn't do crazy stuff.
				PageParameters linkParams = new PageParameters(pp);
				String[] filters = linkParams.getStringArray("filter");
				linkParams.remove("items"); // Reset to first page.
				String filterQuery = facet+ ":"+count.getName();
				if (filters != null) {
					if (!Arrays.asList(filters).contains(filterQuery)) {
						linkParams.add("filter", filterQuery);
					}
				} else {
					linkParams.add("filter", filterQuery);
				}
				BookmarkablePageLink<T> link = new BookmarkablePageLink<T>("link", SearchPage.class, linkParams);
				link.add(new Label("name", displayValue));
				
				link.add(new Label("count", Long.toString(count.getCount())));
				item.add(link);
				
			}
		};
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public void setCount(int count) {
		this.count = count;
	}

}