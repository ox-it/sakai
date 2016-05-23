package org.sakaiproject.assignment.api;

import org.sakaiproject.user.api.User;

import java.util.Optional;

/**
 * This is a provider interface that allows Assignments to provide addition details about candidates
 * to the interface.
 */
interface CandidateDetailProvider {

    /**
     * This gets an anonymous ID for a user.
     * @param user The user for who an anonymous ID is wanted. Cannot be <code>null</code>
     * @return An option containing the candidate ID.
     */
    Optional<String> getAnonymousID(User user);

    /**
     * This gets additional notes for a user.
     * @param user The user for who addition notes are wanted. Cannot be <code>null</code>
     * @return An option containing the additional user notes.
     */
    Optional<String> getAdditionalNotes(User user);

}
