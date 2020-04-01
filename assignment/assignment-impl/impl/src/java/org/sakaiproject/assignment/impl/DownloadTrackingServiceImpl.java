package org.sakaiproject.assignment.impl;

import org.sakaiproject.assignment.api.DownloadEvent;
import org.sakaiproject.assignment.api.DownloadTrackingService;
import org.sakaiproject.assignment.api.model.DownloadEventItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This chucks the events into a database table using hibernate.
 */
public class DownloadTrackingServiceImpl extends HibernateDaoSupport implements DownloadTrackingService {

    private final Logger log = LoggerFactory.getLogger(DownloadTrackingServiceImpl.class);

    public void saveDownload(String userId, String attachmentPath, Date timestamp) {
        DownloadEventItem item = new DownloadEventItem();
        item.setUserId(userId);
        item.setAttachment(attachmentPath);
        item.setDatetime(timestamp);
        try {
            getHibernateTemplate().saveOrUpdate(item);
            getHibernateTemplate().flush();
        } catch (DataIntegrityViolationException e) {
            // This is expected.
            log.debug("Not saving as record exists for {} with attachment {}", userId, attachmentPath);
        }
    }

    public List<DownloadEvent> getDownloads(String attachmentPath) {
        DownloadEventItem item = new DownloadEventItem();
        item.setAttachment(attachmentPath);
        List<DownloadEventItem> items = getHibernateTemplate().findByExample(item);
        return items.stream().map(download -> new DownloadEvent(download.getDatetime(), download.getUserId())).collect(Collectors.toList());
    }



}
