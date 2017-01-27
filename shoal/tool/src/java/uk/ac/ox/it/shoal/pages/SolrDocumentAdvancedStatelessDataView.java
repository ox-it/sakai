package uk.ac.ox.it.shoal.pages;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import uk.ac.ox.it.shoal.components.AdvancedIDataProvider;
import uk.ac.ox.it.shoal.components.AdvancedStatelessDataView;

/**
 * This renders a list of results from Solr.
 */
class SolrDocumentAdvancedStatelessDataView extends AdvancedStatelessDataView<SolrDocument> {
    private static final long serialVersionUID = 1L;

    public SolrDocumentAdvancedStatelessDataView(String items, AdvancedIDataProvider<SolrDocument> provider, int itemsPerPage, PageParameters pp) {
        super(items, provider, itemsPerPage, pp);
    }

    @Override
    public void populateItem(final Item<SolrDocument> item) {
        SolrDocument document = item.getModelObject();
        String id = document.getFieldValue("id").toString();
        Link link = new BookmarkablePageLink<Void>("url", DisplayPage.class, new PageParameters().add("id", id));
        item.add(link);
        link.add(new Label("title", document.getFieldValue("title").toString()));

        ResourceReference icon = new PackageResourceReference(getClass(), "placeholder.png");
        String url = (String) document.getFieldValue("thumbnail");
        if (url != null) {
            icon = new UrlResourceReference(Url.parse(url));
        }
        Image image = new Image("thumbnail", icon);
        image.setSizes();
        item.add(image);
        // So we get <br>s and <p>
        MultiLineLabel description = new MultiLineLabel("description", document.getFieldValue("description").toString());
        item.add(description);

    }
}
