package uk.ac.ox.it.shoal.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import uk.ac.ox.it.shoal.Tuple;
import uk.ac.ox.it.shoal.components.AdvancedIDataProvider;

/**
 * This wraps up a SOLR search into a provider which allows results to be returned.
 * @author buckett
 *
 */
public class SolrProvider implements AdvancedIDataProvider<SolrDocument>
{

	private static final long serialVersionUID = 1L;
	private static final List<Tuple<String,ORDER>> DEFAULT_ORDER;
	private static List<Tuple<String,ORDER>> orders;

	static {

		// Setup the default order to sort thing by.
		List<Tuple<String,ORDER>> order = new ArrayList<>();
		order.add(new Tuple<>("added", ORDER.desc));
		DEFAULT_ORDER = Collections.unmodifiableList(order);

		// Setup order by.
		orders = new ArrayList<>();
		orders.add(new Tuple<>("added", ORDER.desc));
		orders.add(new Tuple<>("score", ORDER.desc));
	}


	private SolrServer solr;
	private String query = "*:*";
	private List<Tuple<String,ORDER>> order;
	private ArrayList<Filter> filters;
	private Map<String, Resolver<? extends Object>>resolvers = new HashMap<>();

	private int first = 0;
	private int count = 10;
	private transient QueryResponse response;

	private SolrProvider() {
		order = DEFAULT_ORDER;
	}

	public SolrProvider(SolrServer solr) {
		this();
		this.solr = solr;
	}

	/**
	 *
	 * @param solr
	 * @param query The escaped query. The caller must escape the query string as only it
	 * know which parts of the query should be escaped.
	 * @param filters
	 */
	public SolrProvider(SolrServer solr, String query, String[] filters ) {
		this(solr);
		this.query = query;
		setFilters(filters);
	}

	public boolean hasQuery() {
		return this.query != null;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setSort(Tuple<String, ORDER> order) {
		this.order = Collections.singletonList(order);
	}

	public void setSort(List<Tuple<String,ORDER>> order) {
		// Play it safe and take a copy.
		this.order = new ArrayList<Tuple<String,ORDER>>(order);
	}

	public void setFilters(String[] filters) {
		this.filters = new ArrayList<Filter>(filters.length);
		for(int i = 0; i < filters.length; i++) {
			String[] parts = filters[i].split(":");
			if (parts.length == 2) {
				this.filters.add(new Filter(parts[0], parts[1]));
			}
		}
	}

	public void setResolver(String facet, Resolver<? extends Object> resolver) {
		this.resolvers.put(facet, resolver);
	}
	
	protected Resolver<?> getResolver(String facet) {
		return resolvers.get(facet);
	}


	public void detach() {
	}

	public Iterator<SolrDocument> iterator(long first, long count) {
		if (first != this.first){
			// This happens if the user attempts to go beyond the last item (hacking the URL), or adds some extra
			// query search params without going back to the first page.
			throw new IllegalArgumentException("The first item has changed since the time we first searched.");
		}
		if (count > this.count) {
			throw new IllegalArgumentException("The count has increased since we first searched.");
		}
		doSearch();
		return response.getResults().iterator();
	}

	public long size() {
		doSearch();
		return (int) response.getResults().getNumFound();
	}

	public IModel<SolrDocument> model(SolrDocument object) {
		return new CompoundPropertyModel<>(object);
	}

	/**
	 * @throws RuntimeException if the Solr search failed.
	 */
	private void doSearch() throws RuntimeException {
		if (response != null) {
			return;
		}
		SolrQuery solrQuery = new SolrQuery().
				
				setQuery(query).
				setStart(first).
				setRows(count).
				setFacet(true).
				setFacetMinCount(1).
				setFacetLimit(SolrFacet.FACET_LIMIT+1). // So we know if we have too many.
				addFacetField("author", "level", "purpose", "interactivity", "type", "subject");
		if (order != null) {
			for(Tuple<String,ORDER> tuple: order) {
				solrQuery.addSortField(tuple.x, tuple.y);
			}
		}
		
		if (filters != null) {
			String[] filterQuery = new String[filters.size()];
			for(int i = 0; i < filterQuery.length && i < filters.size(); i++) {
				filterQuery[i] = filters.get(i).getQuery();
			}
			solrQuery.setFilterQueries(filterQuery);
		}

		try {
			response = solr.query(solrQuery);
		} catch (SolrServerException sse) {
			throw new RuntimeException("Search Failed", sse);
		}
	}

	public <T> Panel getFacet(String id, final String facet, FacetSort sort, final Resolver<T> resolver, final PageParameters pp) {
		// TODO The doSearch should happen just before the render.
		doSearch();
		List<Count> values = response.getFacetField(facet).getValues();
		if (values == null) {
			values = Collections.emptyList();
		}
		ResourceModel title = getFacetTitle(facet);
		Panel panel = new SolrFacet<T>(id, facet, title, values, sort, resolver, pp);
		panel.setVisible(values.size() > 1 && values.size() <= SolrFacet.FACET_LIMIT);
		return panel;
	}
	
	public ListView<?> getFilters(String id, final PageParameters pp) {

		return new ListView<Filter>(id, filters){

			@Override
			protected void populateItem(ListItem<Filter> item) {
				Filter filter = item.getModelObject();
				item.add(new Label("field", filter.getFieldDisplay()));
				item.add(new Label("value", filter.getValueDisplay()));
				PageParameters linkParams = new PageParameters(pp);
				linkParams.remove("items");
				ArrayList<String> reduced = new ArrayList<String>(filters.size());
				for (StringValue filterParam: linkParams.getValues("filter")) {
					if (!filterParam.toString().equals(filter.field+":"+filter.value))
						reduced.add(filterParam.toString());
				}
				linkParams.set("filter", reduced.toArray(new String[]{}));
				item.add(new BookmarkablePageLink<Void>("link", SimpleSearchPage.class, linkParams));
			}

			@Override
			protected void onBeforeRender() {
				doSearch();
				// Only do the search if we're rendering.
				super.onBeforeRender();
			}

		};
	}

	public Panel getOrderBy(final String id, final PageParameters pp) {

	    return new SolrSort(id, pp);

	}

	public


	
	class Filter {
		String field;
		String value;
		
		Filter(String field, String value) {
			this.field = field;
			this.value = value;
		}
		
		String getQuery() {
			return field +":"+ ClientUtils.escapeQueryChars(value);
		}
		
		ResourceModel getFieldDisplay() {
			return getFacetTitle(field);
		}
		
		String getValueDisplay() {
			Resolver<?> resolver = getResolver(field);
			return (resolver != null)?resolver.lookupDisplay(value):value;
		}
		
	}

	protected ResourceModel getFacetTitle(String facet) {
		return new ResourceModel("field.label."+facet, "Unknown");
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public void setCount(int count) {
		this.count = count;
	}

}