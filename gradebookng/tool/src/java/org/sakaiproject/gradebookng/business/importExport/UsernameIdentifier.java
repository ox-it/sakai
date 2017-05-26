package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.user.api.User;

/**
 *
 * @author plukasew, bjones86
 */
@Slf4j
public class UsernameIdentifier implements UserIdentifier, Serializable
{
    private final Map<String, GbUser> userMap;
    private final String namePlaceHolder;
    private final UserIdentificationReport report;

    public UsernameIdentifier(Map<String, User> rosterMap, String unknownNamePlaceholder)
    {
        userMap = new HashMap<>();
        namePlaceHolder = StringUtils.trimToEmpty(unknownNamePlaceholder);

        for (String userId : rosterMap.keySet())
        {
            GbUser gu = new GbUser(rosterMap.get(userId));
            userMap.put(userId, gu);
        }

        report = new UserIdentificationReport(new HashSet<>(userMap.values()));
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
            user = new GbUser(userId, namePlaceHolder);
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
