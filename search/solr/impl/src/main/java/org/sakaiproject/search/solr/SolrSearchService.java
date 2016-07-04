package org.sakaiproject.search.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.SolrPing;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.*;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.notification.SearchNotificationAction;
import org.sakaiproject.search.producer.ContentProducerFactory;
import org.sakaiproject.search.response.filter.SearchItemFilter;
import org.sakaiproject.search.solr.response.SolrSearchList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Search service using Solr to execute search queries.
 * <p>
 * There is some dummy implementation for certain methods as what was relevant in the default implementation of search
 * isn't when there is an external solr server to administrate.
 * </p>
 *
 * @author Colin Hebert
 */
public class SolrSearchService implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SolrSearchService.class);
    private SolrServer solrServer;
    private NotificationEdit notification;
    private SearchIndexBuilder searchIndexBuilder;
    private ContentProducerFactory contentProducerFactory;
    private List<String> triggerFunctions;
    private NotificationService notificationService;
    /**
     * Filter applied to search results.
     * <p>
     * By default the filter will let everything go through without any filtering.
     * </p>
     */
    private SearchItemFilter searchItemFilter = new SearchItemFilter() {
        @Override
        public SearchResult filter(SearchResult result) {
            return result;
        }
    };

    /**
     * Initialises the search service (as long is {@link #isEnabled()} is true) to capture and process event affecting
     * the search index.
     */
    public void init() {
        if (!isEnabled()) {
            logger.info("Search is not enabled. Set search.enable to 'true' in Sakai.properties");
            return;
        }

        logger.debug("Register a notification to trigger indexation on new elements");
        // register a transient notification for resources
        notification = notificationService.addTransientNotification();

        // add all the functions that are registered to trigger search index modification
        notification.setFunction(SearchService.EVENT_TRIGGER_SEARCH);
        for (String function : triggerFunctions) {
            notification.addFunction(function);
        }

        // set the filter to any site related resource
        notification.setResourceFilter("/");

        // set the action
        notification.setAction(new SearchNotificationAction(searchIndexBuilder));
    }

    @Override
    public SearchList search(String searchTerms, List<String> siteIds, int searchStart, int searchEnd)
            throws InvalidSearchQueryException {
        return search(searchTerms, siteIds, searchStart, searchEnd, null, null);
    }

    @Override
    public SearchList search(String searchTerms, List<String> siteIds, int start, int end,
                             String filterName, String sorterName)
            throws InvalidSearchQueryException {
        try {
            SolrQuery query = new SolrQuery();

            query.setStart(start);
            query.setRows(end - start);

            query.setHighlight(true);
            query.setParam("hl.useFastVectorHighlighter", true);
            query.setParam("hl.mergeContiguous", true);
            query.setParam("hl.fl", SearchService.FIELD_CONTENTS);
            query.setParam("hl.formatter", "html");

            if (siteIds != null && !siteIds.isEmpty())
                query.addFilterQuery(createSitesFilterQuery(siteIds));

            logger.debug("Searching with Solr '{}'", searchTerms);

            query.setQuery(searchTerms);
            QueryResponse rsp = solrServer.query(query);
            return new SolrSearchList(rsp, start, searchItemFilter, contentProducerFactory);
        } catch (SolrServerException e) {
            throw new InvalidSearchQueryException("Failed to parse Query", e);
        }
    }

    /**
     * Creates a solr filter query based on a list of site ids.
     *
     * @param siteIds sites ID to add to the filter
     * @return a filter query allowing to search only in the given sites
     */
    private String createSitesFilterQuery(List<String> siteIds) {
        StringBuilder sb = new StringBuilder();
        sb.append('+').append(SearchService.FIELD_SITEID).append(":");
        sb.append('(');
        for (Iterator<String> siteIdsIterator = siteIds.iterator(); siteIdsIterator.hasNext(); ) {
            sb.append('"').append(siteIdsIterator.next()).append('"');
            if (siteIdsIterator.hasNext())
                sb.append(" OR ");
        }
        sb.append(')');
        logger.debug("Create filter query {}", sb);
        return sb.toString();
    }

    @Override
    public void registerFunction(String function) {
        logger.info("Register {} as a trigger for the search service", function);
        notification.addFunction(function);
    }

    @Override
    public void reload() {
    }

    @Override
    public void refreshInstance() {
        searchIndexBuilder.refreshIndex();
    }

    @Override
    public void rebuildInstance() {
        searchIndexBuilder.rebuildIndex();
    }

    @Override
    public void refreshSite(String currentSiteId) {
        searchIndexBuilder.refreshIndex(currentSiteId);
    }

    @Override
    public void rebuildSite(String currentSiteId) {
        searchIndexBuilder.rebuildIndex(currentSiteId);
    }

    @Override
    public String getStatus() {
        try {
            logger.debug("Obtaining search server status");
            return String.valueOf(new SolrPing().process(solrServer).getStatus());
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    @Override
    public int getNDocs() {
        try {
            logger.debug("Obtaining the number of documents available on the server");
            QueryResponse rsp = solrServer.query(new SolrQuery().setRows(0).setQuery("*:*"));
            return (int) rsp.getResults().getNumFound();
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPendingDocs() {
        return searchIndexBuilder.getPendingDocuments();
    }

    @Override
    public List<SearchBuilderItem> getAllSearchItems() {
        return Collections.emptyList();
    }

    @Override
    public List<SearchBuilderItem> getSiteMasterSearchItems() {
        return Collections.emptyList();
    }

    @Override
    public List<SearchBuilderItem> getGlobalMasterSearchItems() {
        return Collections.emptyList();
    }

    @Override
    public SearchStatus getSearchStatus() {
        return new SearchStatus() {

            @Override
            public String getLastLoad() {
                return "";
            }

            @Override
            public String getLoadTime() {
                return "";
            }

            @Override
            public String getCurrentWorker() {
                return "";
            }

            @Override
            public String getCurrentWorkerETC() {
                return "";
            }

            @Override
            public List<Object[]> getWorkerNodes() {
                return Collections.emptyList();
            }

            @Override
            public String getNDocuments() {
                return String.valueOf(SolrSearchService.this.getNDocs());
            }

            @Override
            public String getPDocuments() {
                return String.valueOf(SolrSearchService.this.getPendingDocs());
            }
        };
    }

    @Override
    public boolean removeWorkerLock() {
        return true;
    }

    @Override
    public List<Object[]> getSegmentInfo() {
        return Collections.emptyList();
    }

    @Override
    public void forceReload() {
    }

    @Override
    public TermFrequency getTerms(int documentId) throws IOException {
        throw new UnsupportedOperationException("Solr can't use documentId");
    }

    @Override
    public String searchXML(Map parameterMap) {
        throw new UnsupportedOperationException("Local search must be done against the solr server directly");
    }

    @Override
    public boolean isEnabled() {
        return ServerConfigurationService.getBoolean("search.enable", false);
    }

    @Override
    public String getDigestStoragePath() {
        return null;
    }

    @Override
    public String[] getSearchSuggestions(String searchString, String currentSite, boolean allMySites) {
        logger.debug("Search a suggestion for '{}'", searchString);
        try {
            ModifiableSolrParams params = new ModifiableSolrParams();
            params.set("qt", "/spell");
            params.set("q", searchString);
            params.set("spellcheck", true);
            params.set("spellcheck.collate", true);

            QueryResponse response = solrServer.query(params);
            SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();
            if (spellCheckResponse == null || !spellCheckResponse.isCorrectlySpelled())
                return new String[]{};
            else {
                List<SpellCheckResponse.Collation> collatedResults = spellCheckResponse.getCollatedResults();
                List<String> suggestions = new ArrayList<String>(collatedResults.size());
                for (SpellCheckResponse.Collation collation : collatedResults) {
                    suggestions.add(collation.getCollationQueryString());
                }
                return suggestions.toArray(new String[]{});
            }
        } catch (SolrServerException e) {
            logger.warn("Failed to obtain a suggestion", e);
            return null;
        }
    }

    @Override
    public String getSearchSuggestion(String searchString) {
        String[] suggestions = getSearchSuggestions(searchString, null, true);
        return (suggestions != null && suggestions.length > 0)?suggestions[0]:null;
    }

    //-------------------------------------------------------------------------------------------
    // As far as I know, this implementation isn't diagnosable, so this is a dummy implementation
    //-------------------------------------------------------------------------------------------
    @Override
    public void enableDiagnostics() {
    }

    @Override
    public void disableDiagnostics() {
    }

    @Override
    public boolean hasDiagnostics() {
        return false;
    }

    @Override
    public boolean isSearchServer() {
        return true;
    }

    //--------------------------
    // Search services Accessors
    //--------------------------
    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder) {
        this.searchIndexBuilder = searchIndexBuilder;
    }

    public void setSearchItemFilter(SearchItemFilter searchItemFilter) {
        this.searchItemFilter = searchItemFilter;
    }

    public void setTriggerFunctions(List<String> triggerFunctions) {
        this.triggerFunctions = triggerFunctions;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setContentProducerFactory(ContentProducerFactory contentProducerFactory) {
        this.contentProducerFactory = contentProducerFactory;
    }
}
