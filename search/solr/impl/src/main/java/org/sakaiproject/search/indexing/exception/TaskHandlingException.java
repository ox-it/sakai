package org.sakaiproject.search.indexing.exception;

/**
 * Exception occurring during the execution of a task.
 * <p>
 * This exception is supposed to not be recoverable, otherwise use {@link TemporaryTaskHandlingException}.
 * </p>
 *
 * @author Colin Hebert
 */
public class TaskHandlingException extends RuntimeException {
    /**
     * Constructs a basic exception during the handling of a task.
     */
    public TaskHandlingException() {
    }

    /**
     * Constructs a basic exception during the handling of a task.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public TaskHandlingException(String message) {
        super(message);
    }

    /**
     * Constructs a basic exception during the handling of a task.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public TaskHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a basic exception during the handling of a task.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public TaskHandlingException(Throwable cause) {
        super(cause);
    }
}
