package org.sakaiproject.search.indexing.exception;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Exception aggregating multiple {@link TaskHandlingException} occurring during the execution of a composite task.
 * <p>
 * Adding a NestedTaskHandlingException to another NestedTaskHandlingException will copy the exceptions
 * of the first one in the second one.
 * </p>
 *
 * @author Colin Hebert
 */
public class NestedTaskHandlingException extends TaskHandlingException {
    private final Collection<TaskHandlingException> taskHandlingExceptions = new LinkedList<TaskHandlingException>();

    /**
     * Constructs an exception composed of multiple other {@link TaskHandlingException}.
     */
    public NestedTaskHandlingException() {
    }

    /**
     * Constructs an exception composed of multiple other {@link TaskHandlingException}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public NestedTaskHandlingException(String message) {
        super(message);
    }

    /**
     * Constructs an exception composed of multiple other {@link TaskHandlingException}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public NestedTaskHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception composed of multiple other {@link TaskHandlingException}.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public NestedTaskHandlingException(Throwable cause) {
        super(cause);
    }

    /**
     * Adds an exception to the group of nested exceptions.
     *
     * @param t new exception to add to the nested exceptions
     */
    public void addTaskHandlingException(TaskHandlingException t) {
        if (t instanceof NestedTaskHandlingException)
            taskHandlingExceptions.addAll(((NestedTaskHandlingException) t).getTaskHandlingExceptions());
        else
            taskHandlingExceptions.add(t);
    }

    public Collection<TaskHandlingException> getTaskHandlingExceptions() {
        return taskHandlingExceptions;
    }

    public boolean isEmpty() {
        return taskHandlingExceptions.isEmpty();
    }
}
