package uk.ac.ox.oucs.oxam.pages;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.Paper;
import uk.ac.ox.oucs.oxam.pages.FacetSort.On;
import uk.ac.ox.oucs.oxam.pages.FacetSort.Order;

public class SimpleSearchPage extends SearchPage {

	private final static Log LOG = LogFactory.getLog(SimpleSearchPage.class);
	
	@SpringBean(name="solrServer")
	private SolrServer solr;
	
	@SpringBean
	private ExamPaperService examPaperService;

	String query;
	
	String[] filters;

	private SolrProvider provider;

	/**
	 * This interface is for looking up facet results to transform them to nicer values.
	 * @author buckett
	 *
	 * @param <T>
	 */
	abstract class Resolver<T> {
		
		protected Map<String, T> cached;
		/**
		 * Resolve all the values, this is so we can load all the data we need in one go.
		 * @param values The values to lookup.
		 * @return
		 */
		abstract protected Map<String, T> lookup(List<String> values);
		
		public Map<String, T> load(List<String> values) {
			Map<String, T> latestPapers = lookup(values);
			if (cached == null) {
				cached = latestPapers;
			} else {
				cached.putAll(latestPapers);
			}
			return cached;
		}
		
		/**
		 * This looks up one values and returns it's display value.
		 * This is so that we can have a map of filters.
		 * @param value
		 * @return
		 */
		public String lookupDisplay(String value) {
			T paper = null;
			if (cached != null) {
				paper = cached.get(value);
			}
			if (paper == null) {
				load(Collections.singletonList(value));
			}
			paper = cached.get(value);
			return (paper != null)?display(paper):null;
		}		
		/**
		 * Display one of the resolved values.
		 */
		abstract String display(T value);
	}
	
	
	public SimpleSearchPage(final PageParameters p) {
		// Setup the query.
		query = p.getString("query");
		// Need to parse the filter Query.
		filters = (p.getStringArray("filter")==null)?new String[]{}:p.getStringArray("filter");
		
		String escapedQuery = escapeQuery(query);

		provider = new SolrProvider(solr, escapedQuery, filters);
		
		// Are we searching.
		boolean isSearching = query != null && query.trim().length() > 0;
		if (isSearching) {
			add(new ResultsPanel("results", p));
		} else {
			add(new Fragment("results", "instructions", this));
		}
		
		setStatelessHint(true);
		
		final StatelessForm<Void> form = new StatelessForm<Void>("searchForm") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getMethod() {
				return "post"; // Must be post to clear out the parameters from paging.
			}

		};

		// Finally put the form in.
		final TextField<String> queryField = new TextField<String>("query", new Model<String>(query));
		form.add(queryField);
		form.add(new Button("search") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onSubmit() {
				PageParameters p = new PageParameters();
				p.put("query", queryField.getValue());
				setResponsePage(SimpleSearchPage.class, p);
			}
		});
		
		form.add(new Button("cancel") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onSubmit() {
				// Come back to this page but without any parameters.
				setResponsePage(SimpleSearchPage.class, new PageParameters());
			}
		});
		add(form);
	}
	
	String escapeQuery(String query) {
		if (query != null && query.length() > 0) {
			String[] parts = query.split(" ");
			for (int i = 0; i < parts.length; i++) {
				parts[i] = ClientUtils.escapeQueryChars(parts[i]);
			}
			return StringUtils.join(parts, " ");
		} else {
			return "*:*";
		}
	}
	
	/**
	 * This panel display the simple search results.
	 * It's split off into another class so that if we're not searching we don't end up making a call to Solr.
	 * A better way might be to have all the components in the page only do the search just before rendering, as
	 * this way they could all get added, but the search only happens just before rendering. However I'm not 
	 * sure how you would handle a broken search.
	 *  
	 * @author buckett
	 *
	 */
	class ResultsPanel extends Panel {

		private static final long serialVersionUID = -1633237484843302182L;

		public ResultsPanel(String id, PageParameters p) {
			super(id);
			
			SolrExamResults<SimpleSearchPage> results = new SolrExamResults<SimpleSearchPage>("results", SimpleSearchPage.class, provider, p);
			add(results);

			// Only show the applied filters when we have some.
			WebMarkupContainer filtersBox = new WebMarkupContainer("filtersBox");
			add(filtersBox);
			ListView<?> filtersList = provider.getFilters("filters", p);
			filtersBox.add(filtersList);
			filtersBox.setVisible(filtersList.getViewSize() > 0);
			
			Resolver<Paper> paperResolver = new Resolver<Paper>() {

				protected Map<String, Paper> lookup(List<String> values) {
					return examPaperService.getLatestPapers(values.toArray(new String[]{}));
				}
				
				public String display(Paper value) {
					return value.getTitle();
				}
			};
			provider.setResolver("paper_code", paperResolver);
			Resolver<Exam> examResolver = new Resolver<Exam>() {

				public Map<String, Exam> lookup(List<String> values) {
					return examPaperService.getLatestExams(values.toArray(new String[]{}));
				}

				public String display(Exam value) {
					return value.getTitle();
				}
			};
			provider.setResolver("exam_code", examResolver);
			

			add(provider.getFacet("paper_code_facet", "paper_code", new FacetSort(On.COUNT, Order.DESC), paperResolver, p));
			add(provider.getFacet("exam_code_facet", "exam_code", new FacetSort(On.COUNT, Order.DESC), examResolver, p));
			add(provider.getFacet("year_facet", "academic_year", new FacetSort(On.VALUE, Order.DESC), null, p));
			add(provider.getFacet("term_facet", "term", new FacetSort(On.COUNT, Order.DESC), null, p));

		}

	}


}
