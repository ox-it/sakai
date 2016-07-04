package org.sakaiproject.search.indexing.exception;

import org.sakaiproject.search.indexing.Task;

/**
 * Recoverable exception occurring during the execution of a task.
 * <p>
 * This exception is recoverable and provides the recovering task to run later.
 * </p>
 *
 * @author Colin Hebert
 */
public class TemporaryTaskHandlingException extends TaskHandlingException {
    private final Task newTask;

    /**
     * Constructs a temporary exception that will lead to the execution of a new task.
     *
     * @param newTask new task to execute in result of this exception.
     */
    public TemporaryTaskHandlingException(Task newTask) {
        this.newTask = newTask;
    }

    /**
     * Constructs a temporary exception that will lead to the execution of a new task.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param newTask new task to execute in result of this exception.
     */
    public TemporaryTaskHandlingException(String message, Task newTask) {
        super(message);
        this.newTask = newTask;
    }

    /**
     * Constructs a temporary exception that will lead to the execution of a new task.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @param newTask new task to execute in result of this exception.
     */
    public TemporaryTaskHandlingException(String message, Throwable cause, Task newTask) {
        super(message, cause);
        this.newTask = newTask;
    }

    /**
     * Constructs a temporary exception that will lead to the execution of a new task.
     *
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @param newTask new task to execute in result of this exception.
     */
    public TemporaryTaskHandlingException(Throwable cause, Task newTask) {
        super(cause);
        this.newTask = newTask;
    }

    public Task getNewTask() {
        return newTask;
    }
}
