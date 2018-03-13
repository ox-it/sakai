package org.sakaiproject.gradebookng.business.importExport;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedRow;

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
     * @param row - the row data (which captures the first and last name of users not enrolled in the site)
     * @return the user
     */
    public GbUser getUser( String userID, ImportedRow row );

    public UserIdentificationReport getReport();
}
