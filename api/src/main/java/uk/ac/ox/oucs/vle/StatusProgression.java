package uk.ac.ox.oucs.vle;

import java.util.Collection;

import static uk.ac.ox.oucs.vle.CourseSignupService.*;

/**
 * This interface models the progression through the statuses.
 * This is a graph of directed nodes through which normal flow can occur.
 * Next being forwards and previous being backwards.
 */
public interface StatusProgression {

    /**
     * This gets the next statuses that a signup might have.
     * @param status The current status.
     * @return A collection of statuses or an empty collection if there are none.
     */
    Collection<Status> next(Status status);

    /**
     * This gets the previous statuses that a signup might have.
     * @param status The current status.
     * @return A collection of statuses or an empty collection if there are none.
     */
    Collection<Status> previous(Status status);
}
