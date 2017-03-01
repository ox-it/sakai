package uk.ac.ox.it.shoal.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import uk.ac.ox.it.shoal.Tuple;

import java.util.Arrays;
import java.util.Collection;

public class SimpleSearchPage extends SearchPage {

    private final static Log LOG = LogFactory.getLog(SimpleSearchPage.class);

    @SpringBean()
    private SolrServer solr;


    private String query;

    private String[] filters;

    private SolrProvider provider;

    private PageParameters p;

    public SimpleSearchPage(final PageParameters p) {
        this.p = p;
        setStatelessHint(true);

        // Setup the query.
        query = p.get("query").toString("");
        // Need to parse the filter Query.
        filters = p.getValues("filter").stream().map(StringValue::toString).toArray(String[]::new);
        String order = p.get("order").toString();

        String escapedQuery = escapeQuery(query);

        provider = new SolrProvider(solr, escapedQuery, filters);

        Collection<String> sorts = Arrays.asList("added", "score");
        // Want to limit these as we don't escape them and don't want people messing with the query
        if (sorts.contains(order)) {
            provider.setSort(new Tuple<>(order, SolrQuery.ORDER.desc));
        }
        add(new ResultsPanel("results", p));

    }


    /**
     * Escape the passed string. It splits on spaces and then escapes all the parts.
     *
     * @param query The original string passed by the user.
     * @return The escaped string.
     */
    String escapeQuery(String query) {
        if (query != null && query.length() > 0) {
            String[] parts = query.split(" ");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = ClientUtils.escapeQueryChars(parts[i]);
            }
            return String.join(" ", parts);
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
     */
    class ResultsPanel extends Panel {

        private static final long serialVersionUID = -1633237484843302182L;

        public ResultsPanel(String id, PageParameters p) {
            super(id);

            final StatelessForm<Void> form = new StatelessForm<Void>("searchForm") {
                private static final long serialVersionUID = 1L;
                @Override
                protected String getMethod() {
                    return "post"; // Must be post to clear out the parameters from paging.
                }

            };

            // Finally put the form in.
            final TextField<String> queryField = new TextField<>("query", new Model<>(query));
            form.add(queryField);
            form.add(new Button("search") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    // The problem is to get here we end up doing a search, just
                    PageParameters p = new PageParameters();
                    p.set("query", queryField.getValue());
                    setResponsePage(SimpleSearchPage.class, p);
                }
            });

            // Only show reset if we have something to clear.
            form.add(new Button("cancel") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    // Come back to this page but without any parameters.
                    setResponsePage(SimpleSearchPage.class, new PageParameters());
                }
            }.setVisible(!p.isEmpty()));
            add(form);

            Class clazz = SimpleSearchPage.class;
            PageParameters pp = p;
            Panel results = new SolrContentResults(provider, pp, clazz);
            add(results);

            // Only show the applied filters when we have some.
            WebMarkupContainer filtersBox = new WebMarkupContainer("filtersBox");
            add(filtersBox);
            ListView<?> filtersList = provider.getFilters("filters", p);
            filtersBox.add(filtersList);
            filtersBox.setVisible(filtersList.getViewSize() > 0);

            add(provider.getFacet("type", "type", new FacetSort(FacetSort.On.COUNT, FacetSort.Order.DESC), null, p));
            add(provider.getFacet("author", "author", new FacetSort(FacetSort.On.COUNT, FacetSort.Order.DESC), null, p));
            add(provider.getFacet("subject", "subject", new FacetSort(FacetSort.On.COUNT, FacetSort.Order.DESC), null, p));
            add(provider.getFacet("level", "level", new FacetSort(FacetSort.On.COUNT, FacetSort.Order.DESC), null, p));
            add(provider.getFacet("purpose", "purpose", new FacetSort(FacetSort.On.COUNT, FacetSort.Order.DESC), null, p));
            add(provider.getFacet("interactivity", "interactivity", new FacetSort(FacetSort.On.COUNT, FacetSort.Order.DESC), null, p));
            add(provider.getOrderBy("order", p));

        }

    }

    class EntryPanel extends Panel {

        public EntryPanel(String id, PageParameters pp) {
            super(id);
            setStatelessHint(true);
            final StatelessForm<Void> form = new StatelessForm<Void>("searchForm") {
                private static final long serialVersionUID = 1L;
                @Override
                protected String getMethod() {
                    return "post"; // Must be post to clear out the parameters from paging.
                }

            };

            // Finally put the form in.
            final TextField<String> queryField = new TextField<>("query", new Model<>(query));
            form.add(queryField);
            form.add(new Button("search") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    // The problem is to get here we end up doing a search, just
                    PageParameters p = new PageParameters();
                    p.set("query", queryField.getValue());
                    setResponsePage(SimpleSearchPage.class, p);
                }
            });

            // Only show reset if we have something to clear.
            form.add(new Button("cancel") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    // Come back to this page but without any parameters.
                    setResponsePage(SimpleSearchPage.class, new PageParameters());
                }
            }.setVisible(!getPageParameters().isEmpty()));
            add(form);
            SolrProvider firstProvider = new SolrProvider(solr);
            SolrContentItem items = new SolrContentItem("new", firstProvider, new PageParameters(), 3);
            add(items);

            add(new BookmarkablePageLink<>("more", SimpleSearchPage.class, new PageParameters().add("query", "").add("order", "added")));
        }

    }


}
