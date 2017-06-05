package org.sakaiproject.gradebookng.business.importExport;

import org.sakaiproject.gradebookng.business.model.GbUser;

/**
 * Generic interface to be implemented by all identifier objects.
 * 
 * @author plukasew, bjones86
 */
public interface UserIdentifier
{
    /**
     * Finds the user by the given identifier
     * @param userID a string that uniquely identifies a user
     * @return the user
     */
    public GbUser getUser( String userID );

    public UserIdentificationReport getReport();
}
