package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;

import org.sakaiproject.gradebookng.business.model.GbUser;

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
    public GbUser getUser( String userEID )
    {
        GbUser user = userEidMap.get( userEID );
        if( user != null )
        {
            report.addIdentifiedUser( user );
            log.debug( "User {} identified as UUID: {}", userEID, user.getUserUuid() );
        }
        else
        {
            user = new GbUser( userEID, "" );
            report.addUnknownUser( userEID );
            log.debug( "User {} is unknown to this gradebook", userEID );
        }

        return user;
    }
}
