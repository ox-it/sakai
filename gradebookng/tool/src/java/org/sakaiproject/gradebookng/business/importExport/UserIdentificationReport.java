package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.Getter;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.gradebookng.business.model.GbUser;

/**
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
    private final SortedSet<String> unknownUsers; // ids that couldn't be matched against a user

    @Getter
    private final SortedSet<GbUser> duplicateUsers; // users that have more than one entry in the sheet

    public UserIdentificationReport(Set<GbUser> allUsers)
    {
        missingUsers = new ConcurrentSkipListSet<>(allUsers);
        identifiedUsers = new ConcurrentSkipListSet<>();
        unknownUsers = new ConcurrentSkipListSet<>();
        duplicateUsers = new ConcurrentSkipListSet<>();
    }

    public void addIdentifiedUser(GbUser user)
    {
        if (user.isValid())
        {
            if (identifiedUsers.contains(user))
            {
                duplicateUsers.add(user);
            }
            else
            {
                identifiedUsers.add(user);
                missingUsers.remove(user);
            }
        }
    }

    public void addUnknownUser(String user)
    {
        if (StringUtils.isNotBlank(user))
        {
            unknownUsers.add(user);
        }
    }

    public int getOmittedUserCount()
    {
        return unknownUsers.size() + missingUsers.size();
    }
}
