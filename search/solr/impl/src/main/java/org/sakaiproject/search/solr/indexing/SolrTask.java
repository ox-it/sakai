package org.sakaiproject.search.solr.indexing;

import org.sakaiproject.search.indexing.DefaultTask;

import java.util.Date;

/**
 * Tasks specific to a Solr search index (such as optimise, remove site documents and remove all documents).
 *
 * @author Colin Hebert
 */
public class SolrTask extends DefaultTask {
    /**
     * Creates a solr task based on the types provided in {@link Type}.
     * <p>
     * The creation date of the task is automatically generated.
     * </p>
     *
     * @param type type of the task to create.
     */
    public SolrTask(Type type) {
        this(type, new Date());
    }

    /**
     * Creates a solr task based on the types provided in {@link Type}.
     *
     * @param type         type of the task to create.
     * @param creationDate creation date of the new task.
     */
    public SolrTask(Type type, Date creationDate) {
        super(type.getTypeName(), creationDate);
    }

    /**
     * Task types specific to Solr.
     */
    public static enum Type {
        /**
         * Type of a task in charge of removing every indexed document from a site.
         * <p>
         * This task is usually executed after the reindexation of a site in order to remove every document created
         * before the reindexation that hasn't been updated since.<br />
         * If it wasn't updated it is deprecated.
         * </p>
         */
        REMOVE_SITE_DOCUMENTS,
        /**
         * Type of a task in charge of removing every indexed document from the index.
         * <p>
         * This task is usually executed after a complete reindexation in order to remove every document created
         * before the reindexation that hasn't been updated since.<br />
         * If it wasn't updated it is deprecated.
         * </p>
         */
        REMOVE_ALL_DOCUMENTS,
        /**
         * Type of a task in charge of optimising the solr index.
         */
        OPTIMISE_INDEX;
        private final String typeName = Type.class.getCanonicalName() + '.' + this.toString();

        public String getTypeName() {
            return typeName;
        }
    }
}
