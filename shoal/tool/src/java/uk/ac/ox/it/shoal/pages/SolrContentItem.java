package uk.ac.ox.it.shoal.pages;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import uk.ac.ox.it.shoal.components.AdvancedIDataProvider;

/**
 * Just renders the set of results without any surrounding facets and ordering.
 */
public class SolrContentItem extends Panel {

    public SolrContentItem(String id, AdvancedIDataProvider<SolrDocument> provider, PageParameters pp, int itemsPerPage) {
        super(id);
        SolrDocumentAdvancedStatelessDataView dataView = new SolrDocumentAdvancedStatelessDataView("items", provider, itemsPerPage, pp);
        add(dataView);
    }
}
