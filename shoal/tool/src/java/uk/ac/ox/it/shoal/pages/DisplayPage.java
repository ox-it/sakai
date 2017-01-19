package uk.ac.ox.it.shoal.pages;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.*;

/**
 * Page to display an item fully.
 * This pulls all it's data from Solr
 */
public class DisplayPage extends SakaiPage {

    @SpringBean(name = "solrServer")
    private SolrServer solr;

    public DisplayPage(final PageParameters pp) throws SolrServerException {
        String id = pp.get("id").toString();
        setStatelessHint(true);
        // TODO Need to cope with ID being empty.
        SolrQuery params = new SolrQuery("id:"+ ClientUtils.escapeQueryChars(id));
        QueryResponse query = solr.query(params);
        SolrDocumentList results = query.getResults();
        if (results.isEmpty())  {
            //TODO Handle this
        } else {
            if (results.size() > 1) {
                // TODO Warn about multiple matches
            }
            SolrDocument document = results.get(0);
            String title = document.getFieldValue("title").toString();
            String url = document.getFieldValue("url").toString();
            WebMarkupContainer form = new WebMarkupContainer("preview");
            form.add(new AttributeModifier("action", url));
            add(form);

            add(new Label("title", title));
            String description = document.getFieldValue("description").toString();
            String thumbnail = (String) document.getOrDefault("thumbnail", "http://users.ox.ac.uk/~buckett/placeholder.png");
            add(new MultiLineLabel("description", description));
            ResourceReference icon = new UrlResourceReference(Url.parse(thumbnail));
            Image image = new Image("thumbnail", icon);
            image.setSizes();
            add(image);

            List<Metadata> metadata = new ArrayList<>();
            Metadata subject = newMetadata(document, "subject", "subject", "field.label.subject");
            addIfNotNull(metadata, subject);
            Metadata level = newMetadata(document, "level", "level", "field.label.level");
            addIfNotNull(metadata, level);
            Metadata purpose = newMetadata(document, "purpose", "purpose", "field.label.purpose");
            addIfNotNull(metadata, purpose);
            Metadata interactivity = newMetadata(document, "interactivity", "interactivity", "field.label.interactivity");
            addIfNotNull(metadata, interactivity);
            Metadata type = newMetadata(document, "type", "type", "field.label.type");
            addIfNotNull(metadata, type);
            Metadata author = newMetadata(document, "author", "author", "field.label.author");
            addIfNotNull(metadata, author);
            Metadata added = newMetadata(document, "added", null, "field.label.added");
            addIfNotNull(metadata, added);
            Metadata permission = newMetadata(document, "permission", null, "field.label.permission");
            addIfNotNull(metadata, permission);

            add(new ListView<Metadata>("metadata", metadata){
                @Override
                protected void populateItem(ListItem<Metadata> item) {
                    Metadata objectMetadata = item.getModel().getObject();
                    item.add(new Label("label", new ResourceModel(objectMetadata.key)));
                    RepeatingView links = new RepeatingView("span");
                    objectMetadata.links.forEach(v -> {
                        // This is so that we get some whitespace between links.
                        WebMarkupContainer span = new WebMarkupContainer(links.newChildId());
                        BookmarkablePageLink<String> bookmark = new BookmarkablePageLink<>("link", SimpleSearchPage.class, v.pp);
                        bookmark.setBody(new Model<>(v.label));
                        bookmark.setRenderBodyOnly(v.pp == null);
                        span.add(bookmark);
                        links.add(span);
                    });
                    item.add(links);
                }
            });

        }

    }

    private static void addIfNotNull(List<Metadata> metadata, Metadata subject) {
        if (subject != null) {
            metadata.add(subject);
        }
    }

    private Metadata newMetadata(SolrDocument document, String documentKey, String facetKey, String resourceKey) {
        Collection<Object> fieldValues = document.getFieldValues(documentKey);
        if (fieldValues == null) {
            return null;
        }
        Collection<Link> links = new ArrayList<>();
        fieldValues.forEach(v -> {
            String value = null;
            if (v instanceof String) {
                value = (String)v;
            } else if (v instanceof Date) {
                PrettyTime prettyTime = new PrettyTime();
                value = prettyTime.format((Date)v);
            }
            PageParameters filter = null;
            if (facetKey != null) {
                filter = new PageParameters()
                        .add("filter", facetKey + ":" + value)
                        .add("query", "");
            }
            links.add(new Link(value, filter));
        });
        return new Metadata(resourceKey, links);
    }

    /**
     * This holds one the the metadata rows for the table.
     */
    static class Metadata {
        String key;
        Collection<Link> links;

        Metadata(String key, Collection<Link> links) {
            this.key = key;
            this.links = links;
        }
    }

    /**
     * This holds one of the values for the table.
     */
    static class Link {
        String label;
        PageParameters pp;

        public Link(String label, PageParameters pp) {
            this.label = label;
            this.pp = pp;
        }
    }

}
