package uk.ac.ox.oucs.oxam.pages;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class SearchPage extends WebPage {
	
	@SpringBean(name="solrServer")
	private SolrServer solr;
	private TextField<String> query;
	
	public SearchPage(final PageParameters p) {
		query = new TextField<String>("query", new Model<String>(p.getString("query")));

		
		SolrQuery solrQuery = new SolrQuery().
				setQuery(query.getValue().length() > 0?query.getValue():"*:*").
				setStart(0).
				setRows(10).
				setFacet(true).
				setFacetMinCount(1).
				setFacetLimit(10).
				addFacetField("exam_code", "paper_code", "year", "term");
		try {
			QueryResponse queryResponse = solr.query(solrQuery);
			add(new ListView<Count>("paper_code_facet", queryResponse.getFacetField("paper_code").getValues()) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<Count> item) {
					Count count = item.getModelObject();
					item.add(new Label("name", count.getName()));
					item.add(new Label("count", Long.toString(count.getCount())));
				}
			});
			
			add(new ListView<Count>("exam_code_facet", queryResponse.getFacetField("exam_code").getValues()) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<Count> item) {
					Count count = item.getModelObject();
					item.add(new Label("name", count.getName()));
					item.add(new Label("count", Long.toString(count.getCount())));
					
				}
			});
			
			add(new ListView<Count>("term_facet", queryResponse.getFacetField("term").getValues()) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<Count> item) {
					Count count = item.getModelObject();
					item.add(new Label("name", count.getName()));
					item.add(new Label("count", Long.toString(count.getCount())));					
				}
			});
			
			add(new ListView<Count>("year_facet", queryResponse.getFacetField("year").getValues()) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<Count> item) {
					Count count = item.getModelObject();
					item.add(new Label("name", count.getName()));
					item.add(new Label("count", Long.toString(count.getCount())));					
				}
			});
			
			
			
			add (new ListView<SolrDocument>("results", queryResponse.getResults()) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<SolrDocument> item) {
					SolrDocument document = item.getModelObject();
					item.add(new Label("paper_code", document.getFieldValue("paper_code").toString()));
					item.add(new Label("paper_title", document.getFieldValue("paper_title").toString()));
					item.add(new Label("year", document.getFieldValue("year").toString()));
					item.add(new Label("term", document.getFieldValue("term").toString()));
					item.add(new Label("exam_code", document.getFieldValue("exam_code").toString()));
					item.add(new Label("exam_title", document.getFieldValue("exam_title").toString()));
				}
				
			});
			//queryResponse.get
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setStatelessHint(true);
		query.setRequired(true);
		final StatelessForm<?> form = new StatelessForm("searchForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				System.out.println(query.getValue());
				p.put("query", query.getValue());
				
				setResponsePage(SearchPage.class, p);
			}
			
			@Override
			protected String getMethod() {
				return "get";
			}
			
		};
		form.add(query);
		form.setRedirect(false);
		add(form);
	}

}
