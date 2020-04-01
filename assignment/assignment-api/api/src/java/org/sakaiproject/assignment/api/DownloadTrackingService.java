package org.sakaiproject.assignment.api;

import java.util.Date;
import java.util.List;

public interface DownloadTrackingService {

    void saveDownload(String userId, String attachmentPath, Date timestamp);

    List<DownloadEvent> getDownloads(String attachmentPath);
}
