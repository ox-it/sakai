package org.sakaiproject.search.solr;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.indexing.DefaultTask;
import org.sakaiproject.search.indexing.Task;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.producer.ContentProducerFactory;
import org.sakaiproject.search.queueing.IndexQueueing;
import org.sakaiproject.search.solr.indexing.SolrTools;
import org.sakaiproject.site.api.SiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.sakaiproject.search.indexing.DefaultTask.Type.*;

/**
 * IndexBuilder in charge of adding or removing documents from the Solr index.
 *
 * @author Colin Hebert
 */
public class SolrSearchIndexBuilder implements SearchIndexBuilder {
    /**
     * Unique identifier of the search tool across sakai.
     */
    public static final String SEARCH_TOOL_ID = "sakai.search";
    private static final Logger logger = LoggerFactory.getLogger(SolrSearchIndexBuilder.class);
    private SiteService siteService;
    private SolrTools solrTools;
    private ContentProducerFactory contentProducerFactory;
    private boolean searchToolRequired;
    private boolean ignoreUserSites;
    private IndexQueueing indexQueueing;

    @Override
    public void addResource(Notification notification, Event event) {
        try {
            processEvent(event);
        } catch (Exception e) {
            // addResource is directly related to the event system, it must not throw an exception.
            logger.error("Event handling failed (this should NEVER happen)", e);
        }
    }

    /**
     * Handles an event that should affect the search index.
     *
     * @param event event affecting the index.
     */
    private void processEvent(Event event) {
        String resourceName = event.getResource();
        logger.debug("Attempt to add or remove a resource from the index '{}'", resourceName);
        // Set the resource name to empty instead of null
        if (resourceName == null)
            // TODO: Shouldn't addResource just stop there instead?
            resourceName = "";

        EntityContentProducer entityContentProducer = contentProducerFactory.getContentProducerForEvent(event);
        // If there is no matching entity content producer or no associated site, return
        if (entityContentProducer == null) {
            logger.debug("Can't find an entityContentProducer for '{}'", resourceName);
            return;
        }

        // If the indexing is only enabled on sites with search tool, check that the tool is actually enabled
        if (isOnlyIndexSearchToolSites()) {
            String siteId = entityContentProducer.getSiteId(resourceName);
            try {
                if (siteService.getSite(siteId).getToolForCommonId(SEARCH_TOOL_ID) == null) {
                    logger.debug("Impossible to index the content of the site '{}'"
                            + "because the search tool hasn't been added", siteId);
                    return;
                }
            } catch (IdUnusedException e) {
                logger.warn("Couldn't find the site '{}'", siteId, e);
                return;
            }
        }

        // Create a task for the current event
        Task task;
        switch (entityContentProducer.getAction(event)) {
            case 1: // SearchBuilderItem.ACTION_ADD
                task = new DefaultTask(INDEX_DOCUMENT, event.getEventTime())
                        .setProperty(DefaultTask.REFERENCE, resourceName);
                break;
            case 2: // SearchBuilderItem.ACTION_DELETE
                task = new DefaultTask(REMOVE_DOCUMENT, event.getEventTime())
                        .setProperty(DefaultTask.REFERENCE, resourceName);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported action " + entityContentProducer.getAction(event)
                        + " is not yet supported");
        }
        logger.debug("Add the task '{}' to the queuing system", task);
        indexQueueing.addTaskToQueue(task);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link ContentProducerFactory#addContentProducer(EntityContentProducer)} instead
     */
    @Override
    @Deprecated
    public void registerEntityContentProducer(EntityContentProducer ecp) {
        contentProducerFactory.addContentProducer(ecp);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link ContentProducerFactory#getContentProducerForElement(String)} instead
     */
    @Override
    @Deprecated
    public EntityContentProducer newEntityContentProducer(String ref) {
        return contentProducerFactory.getContentProducerForElement(ref);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link ContentProducerFactory#getContentProducerForEvent(Event)} instead
     */
    @Override
    @Deprecated
    public EntityContentProducer newEntityContentProducer(Event event) {
        return contentProducerFactory.getContentProducerForEvent(event);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link ContentProducerFactory#getContentProducers()} instead
     */
    @Override
    @Deprecated
    public List<EntityContentProducer> getContentProducers() {
        return new ArrayList<EntityContentProducer>(contentProducerFactory.getContentProducers());
    }

    @Override
    public void refreshIndex(String currentSiteId) {
        Task task = new DefaultTask(REFRESH_SITE)
                .setProperty(DefaultTask.SITE_ID, currentSiteId);
        logger.debug("Add the task '{}' to the queuing system", task);
        indexQueueing.addTaskToQueue(task);
    }

    @Override
    public void rebuildIndex(String currentSiteId) {
        Task task = new DefaultTask(INDEX_SITE)
                .setProperty(DefaultTask.SITE_ID, currentSiteId);
        logger.debug("Add the task '{}' to the queuing system", task);
        indexQueueing.addTaskToQueue(task);
    }

    @Override
    public void refreshIndex() {
        Task task = new DefaultTask(REFRESH_ALL);
        logger.debug("Add the task '{}' to the queuing system", task);
        indexQueueing.addTaskToQueue(task);
    }

    @Override
    public boolean isBuildQueueEmpty() {
        return getPendingDocuments() == 0;
    }

    @Override
    public void rebuildIndex() {
        Task task = new DefaultTask(INDEX_ALL);
        logger.debug("Add the task '{}' to the queuing system", task);
        indexQueueing.addTaskToQueue(task);
    }

    @Override
    public void destroy() {
        // Nope, we don't kill search that easily
    }

    @Override
    public int getPendingDocuments() {
        return solrTools.getPendingDocuments();
    }

    @Override
    public boolean isExcludeUserSites() {
        return ignoreUserSites;
    }

    @Override
    public boolean isOnlyIndexSearchToolSites() {
        return searchToolRequired;
    }

    @Override
    public List<SearchBuilderItem> getGlobalMasterSearchItems() {
        // Don't return any item now as the indexing is handled by solr
        return null;
    }

    @Override
    public List<SearchBuilderItem> getAllSearchItems() {
        // Don't return any item now as the indexing is handled by solr
        return null;
    }

    @Override
    public List<SearchBuilderItem> getSiteMasterSearchItems() {
        // Don't return any item now as the indexing is handled by solr
        return null;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSolrTools(SolrTools solrTools) {
        this.solrTools = solrTools;
    }

    public void setSearchToolRequired(boolean searchToolRequired) {
        this.searchToolRequired = searchToolRequired;
    }

    public void setIgnoreUserSites(boolean ignoreUserSites) {
        this.ignoreUserSites = ignoreUserSites;
    }

    public void setContentProducerFactory(ContentProducerFactory contentProducerFactory) {
        this.contentProducerFactory = contentProducerFactory;
    }

    public void setIndexQueueing(IndexQueueing indexQueueing) {
        this.indexQueueing = indexQueueing;
    }
}
