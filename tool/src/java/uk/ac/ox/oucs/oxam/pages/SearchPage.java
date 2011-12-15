package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.oucs.oxam.components.StatelessDataView;
import uk.ac.ox.oucs.oxam.components.StatelessSimplePagingNavigator;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.Paper;

public class SearchPage extends WebPage {

	private final static Log LOG = LogFactory.getLog(SearchPage.class);
	
	@SpringBean(name="solrServer")
	private SolrServer solr;
	
	@SpringBean
	private ExamPaperService examPaperService;

	private TextField<String> query;
	
	private String[] filters;

	// query string, facet restrictions (parsed), current page, 
	// AbstractPageableView to manage this, getItemModels() needs to call through to Solr.

	class SolrProvider implements IDataProvider<SolrDocument>
	{
		private static final long serialVersionUID = 1L;
		private int first = 0;
		private int count = 10;
		private transient QueryResponse response;

		public void detach() {
		}

		public Iterator<SolrDocument> iterator(int first, int count) {
			this.first = first;
			this.count = count;
			doSearch();
			return response.getResults().iterator();
		}

		public int size() {
			if (response == null) {
				doSearch();
			}
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
					
					setQuery(query.getValue().length() > 0?ClientUtils.escapeQueryChars(query.getValue()):"*:*").
					setStart(first).
					setRows(count).
					setFacet(true).
					setFacetMinCount(1).
					setFacetLimit(10).
					//setSortField("year", ORDER.desc).
					addFacetField("exam_code", "paper_code", "year", "term");
			
			if (filters != null) {
				String[] escapedFilters = new String[filters.length];
				for(int i = 0; i < filters.length && i < escapedFilters.length; i++) {
					escapedFilters[i] = ClientUtils.escapeQueryChars(filters[i]);
				}
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

	}
	
	interface Resolver<T> {
		Map<String, T> lookup(List<String> values);
		String display(T value);
	}
	
	public SearchPage(final PageParameters p) {
		query = new TextField<String>("query", new Model<String>(p.getString("query")));
		// Need to parse the filter Query.
		filters = p.getStringArray("filter");

		SolrProvider provider = new SolrProvider();

		DataView<SolrDocument> dataView = new StatelessDataView<SolrDocument>("results", provider, p)  {
			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(final Item<SolrDocument> item)
			{
				SolrDocument document = item.getModelObject();
				ExternalLink link = new ExternalLink("paper_file", document.getFieldValue("paper_file").toString());
				item.add(link);
				link.add(new Label("paper_code", document.getFieldValue("paper_code").toString()));
				link.add(new Label("paper_title", document.getFieldValue("paper_title").toString()));
				item.add(new Label("year", document.getFieldValue("year").toString()));
				item.add(new Label("term", document.getFieldValue("term").toString()));
				item.add(new Label("exam_code", document.getFieldValue("exam_code").toString()));
				item.add(new Label("exam_title", document.getFieldValue("exam_title").toString()));
			}
		};
		dataView.setItemsPerPage(20);
		add(dataView);
		add(new StatelessSimplePagingNavigator("resultsNavigation", SearchPage.class, p, dataView, 10));
		add(new NavigatorLabel("resultsLabel", dataView));
		
		// TODO Refactor out 
		ListView<String> filterList = new ListView<String>("filters", (List<String>)((filters == null)?Collections.emptyList():Arrays.asList(filters))) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item) {
				if (item.getModelObject() == null) {
					LOG.warn("Shouldn't happen");
					return;
				}
				item.add(new Label("name", item.getModelObject()));
				PageParameters linkParams = new PageParameters(p);
				ArrayList<String> reduced = new ArrayList<String>(filters.length);
				String[] filters = linkParams.getStringArray("filter");
				for (String filter: filters) {
					if (!filter.equals(item.getModelObject()))
						reduced.add(filter);
				}
				linkParams.put("filter", reduced.toArray(new String[]{}));
				item.add(new BookmarkablePageLink<Void>("link", SearchPage.class, linkParams));
			}
			
		};
		add(filterList);
		
		add(provider.getFacet("paper_code_facet", "paper_code", new Resolver<Paper>() {

			public Map<String, Paper> lookup(List<String> values) {
				return examPaperService.getLatestPapers(values.toArray(new String[]{}));
			}

			public String display(Paper value) {
				return value.getTitle();
			}

			
		}, p));
		add(provider.getFacet("exam_code_facet", "exam_code", new Resolver<Exam>() {

			public Map<String, Exam> lookup(List<String> values) {
				return examPaperService.getLatestExams(values.toArray(new String[]{}));
			}

			public String display(Exam value) {
				return value.getTitle();
			}
			
		}, p));
		add(provider.getFacet("year_facet", "year", null, p));
		add(provider.getFacet("term_facet", "term", null, p));

		setStatelessHint(true);
		final StatelessForm<Void> form = new StatelessForm<Void>("searchForm") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getMethod() {
				return "post"; // Must be post to clear out the parameters from paging.
			}

		};
		form.add(query);
		form.add(new Button("search") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onSubmit() {
				PageParameters p = new PageParameters();
				p.put("query", query.getValue());
				setResponsePage(SearchPage.class, p);
			}
		});
		form.add(new Button("cancel") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onSubmit() {
				setResponsePage(SearchPage.class, new PageParameters());
			}
		});
		add(form);
	}

}
