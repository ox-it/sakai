package org.sakaiproject.assignment.api;

import java.util.Date;

/**
 * This is an a record of a download by a user.
 */
public class DownloadEvent {

    private final Date datetime;
    private final String userId;

    public DownloadEvent(Date datetime, String userId) {
        this.datetime = datetime;
        this.userId = userId;
    }

    public Date getDatetime() {
        return datetime;
    }

    public String getUserId() {
        return userId;
    }
}
