package org.sakaiproject.search.solr.indexing;

import org.sakaiproject.search.indexing.DefaultTask;
import org.sakaiproject.search.indexing.Task;
import org.sakaiproject.search.indexing.TaskHandler;
import org.sakaiproject.search.indexing.exception.TaskHandlingException;
import org.sakaiproject.search.queueing.IndexQueueing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Queue;

import static org.sakaiproject.search.indexing.DefaultTask.Type.*;
import static org.sakaiproject.search.solr.indexing.SolrTask.Type.REMOVE_ALL_DOCUMENTS;

/**
 * Task handler splitting heavy tasks (such as reindex everything) in smaller subtasks added to the queueing system.
 * <p>
 * The SolrSplitterProcesses split the two heavy tasks "reindex everything" and "refresh everything" in subtasks
 * "reindex site X" and "refresh site X" for each site available.<br />
 * This is followed by a cleanup task that will remove everything that was indexed before the creation of this task
 * but wasn't updated since.<br />
 * An optimisation is also triggered after those heavy operations.
 * </p>
 * <p>
 * If a task isn't an heavy one that deserves to be split, the task will be sent to another {@link TaskHandler}.
 * </p>
 *
 * @author Colin Hebert
 */
public class SolrSplitterProcesses implements TaskHandler {
    private static final Logger logger = LoggerFactory.getLogger(SolrSplitterProcesses.class);
    private TaskHandler actualTaskHandler;
    private IndexQueueing indexQueueing;
    private SolrTools solrTools;

    @Override
    public void executeTask(Task task) {
        try {
            logger.debug("Attempt to handle '{}'", task);
            String taskType = task.getType();
            if (INDEX_ALL.getTypeName().equals(taskType)) {
                createTaskForEverySite(INDEX_SITE, task.getCreationDate());
            } else if (REFRESH_ALL.getTypeName().equals(taskType)) {
                createTaskForEverySite(REFRESH_SITE, task.getCreationDate());
            } else {
                // The task is small enough to be executed directly, send that to another task handler
                actualTaskHandler.executeTask(task);
            }
        } catch (TaskHandlingException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskHandlingException("Couldn't execute the task '" + task + "'", e);
        }
    }

    /**
     * Creates and queues a similar task for every site.
     * <p>
     * This allows to create a repetitive task such as "reindex site" for every site.
     * </p>
     *
     * @param taskType     type of the task to create.
     * @param creationDate creation date of the task (should be the same as the original task).
     */
    private void createTaskForEverySite(DefaultTask.Type taskType, Date creationDate) {
        Queue<String> sites = solrTools.getIndexableSites();
        while (sites.peek() != null) {
            Task refreshSite = new DefaultTask(taskType, creationDate).setProperty(DefaultTask.SITE_ID, sites.poll());
            indexQueueing.addTaskToQueue(refreshSite);
        }

        // Clean up the index by removing sites/documents that shouldn't be indexed anymore
        Task removeAll = new SolrTask(REMOVE_ALL_DOCUMENTS, creationDate);
        indexQueueing.addTaskToQueue(removeAll);
    }

    public void setActualTaskHandler(TaskHandler actualTaskHandler) {
        this.actualTaskHandler = actualTaskHandler;
    }

    public void setIndexQueueing(IndexQueueing indexQueueing) {
        this.indexQueueing = indexQueueing;
    }

    public void setSolrTools(SolrTools solrTools) {
        this.solrTools = solrTools;
    }
}
