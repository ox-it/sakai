package org.sakaiproject.search.queueing;

import org.sakaiproject.search.indexing.Task;

/**
 * Component in the queueing system in charge of receiving queued Tasks and get them running.
 * <p>
 * TaskRunner gets Tasks that were already queued, set the environment (current thread) up and run the Task through a
 * {@link org.sakaiproject.search.indexing.TaskHandler}.
 * </p>
 *
 * @author Colin Hebert
 */
public interface TaskRunner {
    /**
     * Prepares the environment and run the given Task.
     *
     * @param task Task to run
     */
    void runTask(Task task);
}
