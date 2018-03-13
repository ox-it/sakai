package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedRow;

/**
 * Identifier utility for user EIDs.
 * 
 * @author plukasew, bjones86
 */
@Slf4j
public class UsernameIdentifier implements UserIdentifier, Serializable
{
    private final Map<String, GbUser> userEidMap;

    @Getter
    private final UserIdentificationReport report;

    public UsernameIdentifier( Map<String, GbUser> eidMap )
    {
        userEidMap = eidMap;
        report = new UserIdentificationReport( new HashSet<>( userEidMap.values() ) );
    }

    @Override
    public GbUser getUser( String userEID, ImportedRow row )
    {
        GbUser user = userEidMap.get( userEID );
        if( user != null )
        {
            report.addIdentifiedUser( user );
            log.debug( "User {} identified as UUID: {}", userEID, user.getUserUuid() );
        }
        else
        {
            user = GbUser.forDisplayOnly( userEID, row.getStudentName().trim() );
            report.addUnknownUser( user );
            log.debug( "User {} is unknown to this gradebook", userEID );
        }

        return user;
    }
}
