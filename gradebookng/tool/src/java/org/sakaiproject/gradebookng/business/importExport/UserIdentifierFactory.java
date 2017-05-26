package org.sakaiproject.gradebookng.business.importExport;

import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.user.api.User;

/**
 *
 * @author plukasew, bjones86
 */
public class UserIdentifierFactory
{
    private static final String USERNAME_TYPE = "username";
    private static final String STUDENT_NUMBER_TYPE = "student";
    private static final String ANON_TYPE = "anon";

    private static final String ALL_DIGITS_PATTERN = "^\\d+$";
    private static final int MAX_ANON_ID_LENGTH = 5;

    private static final int ROW_READ_MAX = 5;

    public static UserIdentifier buildIdentifierForSheet( List<ImportedRow> rows, Map<String, User> rosterMap )
    {
        String type = idSheetType( rows );
        if( ANON_TYPE.equals( type ) )
        {
            return new AnonIdentifier( rosterMap );
        }
//        else if( STUDENT_NUMBER_TYPE.equals( type ) )
//        {
//
//        }
        else
        {
            // OWLTODO: fix the name placeholder thing
            return new UsernameIdentifier( rosterMap, "" );
        }
    }

    private static String idSheetType( List<ImportedRow> rows )
    {
        // No content (rows does not include headers); bail early
        if( rows.isEmpty() )
        {
            return USERNAME_TYPE;
        }

        // OWLTODO: introduce more logic here to determine type when anon and DPC functionality is added
        return USERNAME_TYPE;
    }
}
