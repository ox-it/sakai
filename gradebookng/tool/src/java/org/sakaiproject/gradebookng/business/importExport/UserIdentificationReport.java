package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import lombok.Getter;


import org.sakaiproject.gradebookng.business.model.GbUser;

/**
 * Contains the data relevant to user identification: identified users, missing users, unknown users and duplicate users.
 * 
 * @author plukasew, bjones86
 */
public class UserIdentificationReport implements Serializable
{
    @Getter
    private final SortedSet<GbUser> identifiedUsers; // users that were matched against an id

    @Getter
    private final SortedSet<GbUser> missingUsers; // users that could have been matched against an id but weren't

    @Getter
    private final SortedSet<GbUser> unknownUsers; // ids that couldn't be matched against a user

    @Getter
    private final SortedSet<GbUser> duplicateUsers; // users that have more than one entry in the sheet

    public UserIdentificationReport( Set<GbUser> allUsers )
    {
        missingUsers = new ConcurrentSkipListSet<>( allUsers );
        identifiedUsers = new ConcurrentSkipListSet<>();
        unknownUsers = new ConcurrentSkipListSet<>();
        duplicateUsers = new ConcurrentSkipListSet<>();
    }

    public void addIdentifiedUser( GbUser user )
    {
        if( user.isValid() )
        {
            if( identifiedUsers.contains( user ) )
            {
                duplicateUsers.add( user );
            }
            else
            {
                identifiedUsers.add( user );
                missingUsers.remove( user );
            }
        }
    }

    public void addUnknownUser( GbUser user )
    {
        if( user != null )
        {
            unknownUsers.add( user );
        }
    }

    public int getOmittedUserCount()
    {
        return unknownUsers.size() + missingUsers.size();
    }
}
