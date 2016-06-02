package org.sakaiproject.search.queueing;

import org.sakaiproject.search.indexing.Task;

/**
 * Every new indexing Task is sent to the IndexQueueing to be put on hold until a {@link TaskRunner} can process it.
 *
 * @author Colin Hebert
 */
public interface IndexQueueing {
    /**
     * Receives a newly created Task and queues it to be run as soon as possible.
     *
     * @param task task to queue
     */
    void addTaskToQueue(Task task);
}
