package uk.ac.ox.oucs.oxam.pages;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;

import uk.ac.ox.oucs.oxam.components.AdvancedIDataProvider;
import uk.ac.ox.oucs.oxam.components.AdvancedStatelessDataView;
import uk.ac.ox.oucs.oxam.components.StatelessSimplePagingNavigator;

class SolrExamResults extends Panel {

	private static final long serialVersionUID = 1L;
	private DataView<SolrDocument> dataView;
	private StatelessSimplePagingNavigator pager;
	private NavigatorLabel pagerLabel;

	public SolrExamResults(String id,Class clazz, AdvancedIDataProvider<SolrDocument> provider, PageParameters pp) {
		// TODO need to deal with no matches.
		
		super(id);
		dataView = new AdvancedStatelessDataView<SolrDocument>("items", provider, pp)  {
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
		pager = new StatelessSimplePagingNavigator("resultsNavigation", clazz, pp, dataView, 10);
		pagerLabel = new NavigatorLabel("resultsLabel", dataView);
		
		add(dataView);
		add(pager);
		add(pagerLabel);
	}
	
}