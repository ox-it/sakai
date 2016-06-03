package org.sakaiproject.search.indexing;

/**
 * Element in charge of extracting the information provided by a {@link Task} and execute the expected code.
 *
 * @author Colin Hebert
 */
public interface TaskHandler {
    /**
     * Executes a given Task.
     *
     * @param task task to execute
     */
    void executeTask(Task task);
}
