package uk.ac.ox.it.shoal.pages;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import uk.ac.ox.it.shoal.components.AdvancedIDataProvider;
import uk.ac.ox.it.shoal.components.EmptyMessage;
import uk.ac.ox.it.shoal.components.StatelessSimplePagingNavigator;

/**
 * Created by buckett on 16/12/2016.
 */
public class SolrContentResults extends Panel {
    private static final long serialVersionUID = 1L;
    private final PageParameters pp;
    private DataView<SolrDocument> dataView;
    private StatelessSimplePagingNavigator<SimpleSearchPage> pager;
    private NavigatorLabel pagerLabel;
    private EmptyMessage emptyMessage;

    public SolrContentResults(AdvancedIDataProvider<SolrDocument> provider, PageParameters pp, Class clazz) {
        super("results");
        this.pp = pp;
        dataView = new SolrDocumentAdvancedStatelessDataView("items", provider, 5, pp);
        pager = new StatelessSimplePagingNavigator("resultsNavigation", clazz, pp, dataView, 5);
        pagerLabel = new NavigatorLabel("resultsLabel", dataView) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return dataView.getRowCount() != 0;
            }
        };

        // When we have no matches.
        emptyMessage = new EmptyMessage("emptyMessage", dataView, new ResourceModel("no.results.found"));
        add(dataView);
        add(pager);
        add(pagerLabel);
        add(emptyMessage);
    }
}
