package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;

import org.sakaiproject.gradebookng.business.model.GbUser;

/**
 * Identifier utility for anonymous IDs.
 * 
 * @author plukasew, bjones86
 */
@Slf4j
public class AnonIdentifier implements UserIdentifier, Serializable
{
    private final Map<String, GbUser> anonIdMap;

    @Getter
    private final UserIdentificationReport report;

    public AnonIdentifier( Map<String, GbUser> anonymousIdMap )
    {
        anonIdMap = anonymousIdMap;
        report = new UserIdentificationReport( new HashSet<>( anonIdMap.values() ) );
    }

    @Override
    public GbUser getUser( String anonID )
    {
        GbUser user = anonIdMap.get( anonID );
        if( user != null )
        {
            report.addIdentifiedUser( user );
            log.debug( "User's anon ID {} identified as UUID: {}", anonID, user.getUserUuid() );
        }
        else
        {
            user = new GbUser( anonID, "" );
            report.addUnknownUser( anonID );
            log.debug( "User's anon ID {} is unknown to this gradebook", anonID );
        }

        return user;
    }
}
