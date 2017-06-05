package org.sakaiproject.gradebookng.business.importExport;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedRow;

/**
 * Factory class for building the correct {@link UserIdentifier} type for the given data.
 * 
 * @author plukasew, bjones86
 */
public class UserIdentifierFactory
{
    /**
     * Create the correct {@link UserIdentifier} that corresponds to the data parsed from the imported file.
     *
     * @param rows
     * @param rosterMap
     * @return
     */
    public static UserIdentifier buildIdentifierForSheet( List<ImportedRow> rows, Map<String, GbUser> rosterMap )
    {
        // No content (rows does not include headers); bail early
        UserIdentifier identifier;
        if( rows.isEmpty() )
        {
            return new UsernameIdentifier( rosterMap );
        }

        // Check the first row to determine content type
        ImportedRow firstRow = rows.get( 0 );
        if( StringUtils.isNotBlank( firstRow.getAnonID() ) )
        {
            identifier = new AnonIdentifier( rosterMap );
        }
        else if( StringUtils.isNotBlank( firstRow.getStudentNumber() ) )
        {
            identifier = new StudentNumberIdentifier( rosterMap );
        }
        else
        {
            identifier = new UsernameIdentifier( rosterMap );
        }

        validateUsers( identifier, rows );
        return identifier;
    }

    /**
     * Validate all users for the imported data, using the given {@link UserIdentifier} object.
     * 
     * @param userIdentifier
     * @param rows
     * @param rosterMap
     */
    private static void validateUsers( UserIdentifier userIdentifier, List<ImportedRow> rows )
    {
        for( ImportedRow row : rows )
        {
            String lookupProp;
            if( userIdentifier instanceof AnonIdentifier )
            {
                lookupProp = row.getAnonID();
            }
            else if( userIdentifier instanceof StudentNumberIdentifier )
            {
                lookupProp = row.getStudentNumber();
            }
            else
            {
                lookupProp = row.getStudentEid();
            }

            GbUser user = userIdentifier.getUser( lookupProp );
            if( user != null )
            {
                row.setUser( user );
            }
        }
    }
}
