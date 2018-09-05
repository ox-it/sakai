package org.sakaiproject.component.app.scheduler.jobs.removesites;


import lombok.Setter;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;


/**
 * This removes all the user's sites. This is useful if you change the template and want to have all the sites
 * re-copied from the templates.
 */
@Slf4j
public class RemoveMySitesJob implements Job {

    @Setter
    private SiteService siteService;
    @Setter
    private SessionManager sessionManager;
    @Setter
    private Collection<String> ignoredSiteIds;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Session session = sessionManager.getCurrentSession();
        try {
            session.setUserId("admin");
            session.setUserEid("admin");

            int removed = 0;
            // Ratio of sleep to work ( 2 = sleep twice as much as you work ), this is to prevent the job from
            // using up all the resources
            int workRate = 1;
            Instant started = Instant.now();
            Duration slept = Duration.ZERO;

            // Load all in one go (but just the IDs) so we know we have them all.
            List<String> siteIds = siteService.getSiteIds(SiteService.SelectionType.USER, null, null, null, SiteService.SortType.NONE, null);
            siteIds.removeIf(siteId -> !siteService.isUserSite(siteId));
            siteIds.removeAll(ignoredSiteIds);
            for(String siteId : siteIds) {
                try {
                    Site site = siteService.getSite(siteId);
                    // There's no recycle bin for user sites.
                    siteService.removeSite(site);
                    removed++;
                    Duration working = Duration.between(started, Instant.now()).minus(slept);
                    Duration pause = working.dividedBy(workRate).minus(slept);
                    if (!pause.isNegative()) {
                        long millis = pause.toMillis();
                        log.debug("Sleeping for {}ms", millis);
                        Thread.sleep(millis);
                        slept = slept.plus(pause);
                    }
                } catch (IdUnusedException iue) {
                    log.warn("Failed to find site {}, concurrent jobs running?", siteId);
                } catch (PermissionException e) {
                    log.error("Failed to have permission to remove site: {}", siteId);
                } catch (InterruptedException e) {
                    log.error("Failed to sleep because of interruption");
                }
            }
            log.info("Job completed, {}/{} removed, took {} minutes",
                removed, siteIds.size(), Duration.between(started, Instant.now()).toMinutes());
        } finally {
            session.clear();
        }


    }
}
