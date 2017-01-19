package uk.ac.ox.it.shoal.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import uk.ac.ox.it.shoal.model.TeachingItem;
import uk.ac.ox.it.shoal.model.ValidatedTeachingItem;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * This indexes any changed sites.
 */
public class Indexer implements Observer {

    private final Log log = LogFactory.getLog(Indexer.class);

    private EventTrackingService eventTrackingService;
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;
    private SolrServer solrServer;
    private ValidatorFactory validatorFactory;

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public void init() {
        Objects.requireNonNull(eventTrackingService);
        Objects.requireNonNull(serverConfigurationService);
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(solrServer);
        validatorFactory = Validation.buildDefaultValidatorFactory();
        eventTrackingService.addLocalObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Event) {
            Event event = (Event)arg;
            // Have to fire this after it's been saved in the DB
            if (SiteService.SECURE_ADD_SITE.equals(event.getEvent()) ||
                    SiteService.SECURE_UPDATE_SITE.equals(event.getEvent())){
                try {
                    Site site = siteService.getSite(event.getContext());
                    String siteType = serverConfigurationService.getString("shoal.site.type");
                    if (siteType == null || siteType.isEmpty() || siteType.equals(site.getType())) {
                        // Want to include this one.
                        TeachingItem teachingItem = SakaiProxyImpl.toTeachingItem(site);
                        addToIndex(teachingItem);
                    }
                } catch (IdUnusedException e) {
                    log.warn("Failed to find site that has just been saved: "+ event.getContext());
                }
            }
        }
    }

    /**
     * This rebuilds the whole index.
     */
    public void reIndex() {
        try {
            solrServer.deleteByQuery("*:*");
            String siteType = serverConfigurationService.getString("shoal.site.type", null);
            List<Site> sites = siteService.getSites(SiteService.SelectionType.ANY, siteType, null, null, SiteService.SortType.NONE, null);
            for (Site site: sites) {
                TeachingItem item = SakaiProxyImpl.toTeachingItem(site);
                addToIndex(item);
            }
            log.info("Re-indexer looked at :"+ sites.size());
            solrServer.commit();

        } catch (SolrServerException | IOException e) {
            log.warn("Failed to re-index.", e);
        }

    }

    /**
     * Adds the item to the index, if it's not valid then it does nothing.
     * @param teachingItem The item to add.
     */
    private void addToIndex(TeachingItem teachingItem) {

        ValidatedTeachingItem validatedTeachingItem = new ValidatedTeachingItem(teachingItem);
        Set<ConstraintViolation<ValidatedTeachingItem>> validate = validatorFactory.getValidator()
                .validate(validatedTeachingItem);
        // Validate before adding, we don't care about errors here, just that it passes.
        if (!validate.isEmpty())  {
            return;
        }

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", teachingItem.getId());
        doc.addField("title", teachingItem.getTitle());
        doc.addField("description", teachingItem.getDescription());
        doc.addField("subject", teachingItem.getSubject());
        doc.addField("level", teachingItem.getLevel());
        doc.addField("purpose", teachingItem.getPurpose());
        doc.addField("interactivity", teachingItem.getInteractivity());
        doc.addField("type", teachingItem.getType());
        doc.addField("url", teachingItem.getUrl());
        doc.addField("author", teachingItem.getAuthor());
        doc.addField("contact", teachingItem.getContact());
        doc.addField("added", teachingItem.getAdded());
        doc.addField("permission", teachingItem.getPermission());
        doc.addField("updated", Instant.now());
        doc.addField("license", teachingItem.getLicense());
        doc.addField("thumbnail", teachingItem.getThumbnail());

        try {
            solrServer.add(doc);
            solrServer.commit();
        } catch (SolrServerException | IOException e) {
            log.warn("Problem adding "+ teachingItem.getId()+ " to solr.", e);
        }

    }

}
