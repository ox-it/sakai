package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.Paper;

public class SearchPage extends WebPage {

	private final static Log LOG = LogFactory.getLog(SearchPage.class);
	
	@SpringBean(name="solrServer")
	private SolrServer solr;
	
	@SpringBean
	private ExamPaperService examPaperService;

	TextField<String> query;
	
	String[] filters;

	/**
	 * This interface is for looking up facet results to transform them to nicer values.
	 * @author buckett
	 *
	 * @param <T>
	 */
	interface Resolver<T> {
		/**
		 * Resolve all the values, this is so we can load all the data we need in one go.
		 * @param values The values to lookup.
		 * @return
		 */
		Map<String, T> lookup(List<String> values);
		
		/**
		 * Display one of the resolved values.
		 */
		String display(T value);
	}
	
	public SearchPage(final PageParameters p) {
		query = new TextField<String>("query", new Model<String>(p.getString("query")));
		// Need to parse the filter Query.
		filters = (p.getStringArray("filter")==null)?new String[]{}:p.getStringArray("filter");
		
		
		String[] escapedFilters = new String[filters.length];
		for(int i = 0; i < filters.length && i < escapedFilters.length; i++) {
			String[] parts = filters[i].split(":");
			if (parts != null && parts.length == 2) {
				escapedFilters[i] = parts[0]+ ":"+ ClientUtils.escapeQueryChars(parts[1]);
			}
		}
		
		String escapedQuery = ClientUtils.escapeQueryChars(query.getValue());

		SolrProvider provider = new SolrProvider(solr, escapedQuery, escapedFilters);

		SolrExamResults results = new SolrExamResults("results", SearchPage.class, provider, p);
		add(results);
		
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
				linkParams.remove("items");
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
		add(provider.getFacet("year_facet", "academic_year", null, p));
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
