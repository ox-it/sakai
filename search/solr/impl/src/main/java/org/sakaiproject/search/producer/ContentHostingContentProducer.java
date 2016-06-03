package org.sakaiproject.search.producer;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;

import java.util.*;

import static org.sakaiproject.content.api.ContentHostingService.*;

/**
 * Abstract implementation defining basic tools to provide indexable documents from the ContentHostingService
 * <p>
 * Extend this class to create an EntityContentProducer for new custom content types.
 * </p>
 *
 * @author Colin Hebert
 */
public abstract class ContentHostingContentProducer implements EntityContentProducer {
    /**
     * Content hosting service providing details on the potentially indexed documents.
     */
    protected ContentHostingService contentHostingService;
    /**
     * Entity manager giving details on any entity given its reference.
     */
    protected EntityManager entityManager;
    private ServerConfigurationService serverConfigurationService;
    private SearchService searchService;
    private SearchIndexBuilder searchIndexBuilder;

    /**
     * Registers events related to ContentHosting that should trigger an indexation.
     */
    public void init() {
        if (serverConfigurationService.getBoolean("search.enable", false)) {
            searchService.registerFunction(EVENT_RESOURCE_ADD);
            searchService.registerFunction(EVENT_RESOURCE_WRITE);
            searchService.registerFunction(EVENT_RESOURCE_REMOVE);
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    @Override
    public String getTitle(String reference) {
        ContentResource contentResource;
        try {
            contentResource = contentHostingService.getResource(getId(reference));
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve resource ", e);
        }
        ResourceProperties rp = contentResource.getProperties();
        return rp.getProperty(rp.getNamePropDisplayName());
    }

    @Override
    public Integer getAction(Event event) {
        String eventName = event.getEvent();
        // Skip the resourceType check if the event isn't about resources
        if (!EVENT_RESOURCE_REMOVE.equals(eventName) && !EVENT_RESOURCE_ADD.equals(eventName)
                && !EVENT_RESOURCE_WRITE.equals(eventName))
            return SearchBuilderItem.ACTION_UNKNOWN;

        String resourceType = getResourceType(event.getResource());
        // If the resource type isn't provided, assume that it's a document we want to delete, try to proceed.
        // The resource type should always be provided, if it isn't assume that the document doesn't exist anymore.
        if (resourceType == null && EVENT_RESOURCE_REMOVE.equals(eventName) && isForIndexDelete(event.getResource())) {
            return SearchBuilderItem.ACTION_DELETE;
        } else if (isResourceTypeSupported(resourceType)
                && (EVENT_RESOURCE_ADD.equals(eventName) || EVENT_RESOURCE_WRITE.equals(eventName))
                && isForIndex(event.getResource())) {
            return SearchBuilderItem.ACTION_ADD;
        } else {
            return SearchBuilderItem.ACTION_UNKNOWN;
        }
    }

    /**
     * Obtains the resource type of some hosted content.
     *
     * @param reference reference to the hosted content.
     * @return the resource type of the content or null if either the reference is null or or the reference is invalid.
     */
    private String getResourceType(String reference) {
        try {
            if (reference == null)
                return null;
            return contentHostingService.getResource(getId(reference)).getResourceType();
        } catch (IdUnusedException e) {
            // It isn't uncommon to have an old reference to some content that doesn't exist anymore
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve resource ", e);
        }
    }

    /**
     * Provides the list of resource type supported by the implementation of ContentHostingContentProducer.
     *
     * @param contentType tested content type.
     * @return true if the content type is handled, false otherwise.
     */
    protected abstract boolean isResourceTypeSupported(String contentType);

    @Override
    public boolean matches(Event event) {
        return !SearchBuilderItem.ACTION_UNKNOWN.equals(getAction(event));
    }

    @Override
    public String getTool() {
        return "content";
    }

    @Override
    public Iterator<String> getSiteContentIterator(String context) {
        String siteCollection = contentHostingService.getSiteCollection(context);
        final Collection<ContentResource> siteContent;
        if (!"/".equals(siteCollection)) siteContent = contentHostingService.getAllResources(siteCollection);
        else siteContent = Collections.emptyList();

        // Extract references withing the given site and return only the supported ones
        Collection<String> contentReferences = new ArrayList<String>(siteContent.size());
        for (ContentResource contentResource : siteContent) {
            String reference = contentResource.getReference();
            if (isResourceTypeSupported(contentResource.getResourceType()))
                contentReferences.add(reference);
        }
        return contentReferences.iterator();
    }

    /**
     * Nasty hack to not index dropbox without loading an entity from the DB.
     */
    private boolean isInDropbox(String reference) {
        return reference.length() > "/content".length()
                && contentHostingService.isInDropbox(reference.substring("/content".length()));
    }

    private boolean isAnAssignment(String reference) {
        final int assignmentPosition = 4;
        String[] parts = reference.split("/");
        return parts.length > assignmentPosition && "Assignments".equals(parts[assignmentPosition])
                && ContentHostingService.ATTACHMENTS_COLLECTION.equals("/" + parts[2] + "/");
    }

    private boolean isForIndexDelete(String reference) {
        return !isInDropbox(reference);
    }

    @Override
    public boolean isForIndex(String reference) {
        try {
            if (isInDropbox(reference) || isAnAssignment(reference))
                return false;

            ContentResource contentResource = contentHostingService.getResource(getId(reference));
            // Only index files, not directories
            return contentResource != null && !contentResource.isCollection();
        } catch (IdUnusedException idun) {
            return false; // an unknown resource that cant be indexed
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve resource ", e);
        }
    }

    @Override
    public boolean canRead(String reference) {
        try {
            contentHostingService.checkResource(getId(reference));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Collection<String>> getCustomProperties(String ref) {
        try {
            Map<String, Collection<String>> props = new HashMap<String, Collection<String>>();

            ResourceProperties rp = contentHostingService.getResource(getId(ref)).getProperties();
            Iterator<String> propertiesIterator = rp.getPropertyNames();
            while (propertiesIterator.hasNext()) {
                String propertyName = propertiesIterator.next();
                props.put(propertyName, rp.getPropertyList(propertyName));
            }
            // We want a good filename which doesn't include the path.
            String filename = getFileName(ref);
            if (filename != null) {
                props.put("filename", Collections.singletonList(filename));
            }
            return props;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * This just extracts a filename
     * @param ref
     * @return
     */
    String getFileName(String ref) {
        // This attempts to get the filename from a reference.
        int start = ref.lastIndexOf("/");
        int end = ref.length();

        if (start == ref.length() - 1) {
            // Don't include the last "/"
            end--;
            start = ref.lastIndexOf("/", start -1);
        }
        if (start > 0) {
            return ref.substring(start + 1, end);
        } else {
            return null;
        }

    }

    @Override
    public String getCustomRDF(String ref) {
        return null;
    }

    @Override
    public String getUrl(String reference) {
        return entityManager.newReference(reference).getUrl();
    }

    @Override
    public String getId(String ref) {
        return entityManager.newReference(ref).getId();
    }

    @Override
    public String getType(String ref) {
        return entityManager.newReference(ref).getType();
    }

    @Override
    public String getSubType(String ref) {
        return entityManager.newReference(ref).getSubType();
    }

    @Override
    public String getContainer(String ref) {
        return entityManager.newReference(ref).getContainer();
    }

    @Override
    public String getSiteId(String reference) {
        return entityManager.newReference(reference).getContext();
    }

    @Override
    public boolean matches(String reference) {
        return entityManager.newReference(reference).getEntityProducer() instanceof ContentHostingService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder) {
        this.searchIndexBuilder = searchIndexBuilder;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
