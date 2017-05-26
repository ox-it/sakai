package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.user.api.User;

/**
 *
 * @author plukasew, bjones86
 */
@Slf4j
public class AnonIdentifier implements UserIdentifier, Serializable
{
    private final Map<String, GbUser> userMap;
    private final UserIdentificationReport report;

    public AnonIdentifier(Map<String, User> anonRosterMap)
    {
        userMap = new HashMap<>();

        for (String anonId : anonRosterMap.keySet())
        {
            GbUser gu = new GbUser(anonRosterMap.get(anonId));
            userMap.put(anonId, gu);
        }

        report = new UserIdentificationReport(new HashSet<>(userMap.values()));

        // OWLTODO: this is almost identical to UsernameIdentifier! Base class? constructor modifier?
        // OWLTODO: can we pull the roster maps out of SpreadsheetUploadBean and into these classes?
    }

    @Override
    public GbUser getUser(String userId)
    {
        GbUser user;

        if (userMap.containsKey(userId))
        {
            user = userMap.get(userId);
            report.addIdentifiedUser(user);
            log.debug("User {} identified as UID: {}", userId, user.getUserUuid());
        }
        else
        {
            user = new GbUser(userId, "");
            report.addUnknownUser(userId);
            log.debug("User {} is unknown to this gradebook", userId);
        }

        return user;
    }

    @Override
    public UserIdentificationReport getReport()
    {
        return report;
    }
}
