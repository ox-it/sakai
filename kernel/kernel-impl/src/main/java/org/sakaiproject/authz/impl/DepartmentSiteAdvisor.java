package org.sakaiproject.authz.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.DevolvedSakaiSecurity;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteAdvisor;
import org.sakaiproject.site.api.SiteService;

import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

/**
 * This sets a property on a managed Site to the title of it's admin site.
 * This is useful so that we can do reporting on sites based on their admin site without having to look them up
 * in the database. The original work was to allow reporting on LTI tools, so they show their affiliation in the
 * dashboard.
 */
public class DepartmentSiteAdvisor implements SiteAdvisor, Observer {

    private final Log log = LogFactory.getLog(DepartmentSiteAdvisor.class);

    private DevolvedSakaiSecurity devolvedSakaiSecurity;
    private SiteService siteService;
    private EntityManager entityManager;
    private EventTrackingService eventTrackingService;
    private String siteProperty;

    public void setDevolvedSakaiSecurity(DevolvedSakaiSecurity devolvedSakaiSecurity) {
        this.devolvedSakaiSecurity = devolvedSakaiSecurity;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setSiteProperty(String siteProperty) {
        this.siteProperty = siteProperty;
    }

    public void init() {
        Objects.requireNonNull(devolvedSakaiSecurity);
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(entityManager);
        Objects.requireNonNull(eventTrackingService);
        Objects.requireNonNull(siteProperty);
        siteService.addSiteAdvisor(this);
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy() {
        siteService.removeSiteAdvisor(this);
        eventTrackingService.deleteObserver(this);
    }

    @Override
    public void update(Site site) {
        updateSite(site);
    }

    @Override
    public void update(Observable o, Object arg) {
        // This catches changes to an admin realm change
        if (arg instanceof Event) {
            Event event = (Event) arg;
            if (DevolvedSakaiSecurityImpl.ADMIN_REALM_CHANGE.equals(event.getEvent())) {
                Reference reference = entityManager.newReference(event.getResource());
                Entity entity = reference.getEntity();
                if (entity instanceof Site) {
                    Site site = (Site)entity;
                    updateSite(site);
                    try {
                        siteService.save(site);
                    } catch (IdUnusedException e) {
                        log.warn("Failed to find site when admin realm changed: "+ site.getId(), e);
                    } catch (PermissionException e) {
                        log.warn("No permission to change site when changing admin realm: "+ site.getId(), e);
                    }
                }
            } else if (SiteService.SECURE_UPDATE_SITE.equals(event.getEvent())) {
                Reference reference = entityManager.newReference(event.getResource());
                Entity entity = reference.getEntity();
                if (entity instanceof Site) {
                    Site site = (Site) entity;
                    updateAllSites(site);
                }
            }
        }
    }

    private void updateSite(Site site) {
        String adminRealm = devolvedSakaiSecurity.getAdminRealm(site.getReference());
        if (adminRealm != null) {
            Reference reference = entityManager.newReference(adminRealm);
            Entity entity = reference.getEntity();
            if (entity instanceof Site) {
                Site adminSite = (Site)entity;
                String adminTitle = adminSite.getTitle();
                site.getProperties().addProperty(siteProperty, adminTitle);
                log.debug("Set property("+ siteProperty+ ") on site: "+ site.getId()+ " from  "+ adminRealm);
            }
        } else {
            site.getProperties().removeProperty(siteProperty);
            // There won't be an admin site when the first save is made, but then afterwards when pages are added and
            // updated there will.
            log.debug("Removing property on site: "+ site.getId()+ " as no admin realm.");
        }
    }

    public void updateAllSites(Site site) {
        if (devolvedSakaiSecurity.getAdminSiteType().equals(site.getType())) {
            // Update all the sites using this.
            List<Entity> managed = devolvedSakaiSecurity.findUsesOfAdmin(site.getReference());
            for (Entity entity : managed ) {
                if (entity instanceof Site) {
                    Site managedSite = (Site) entity;
                    ResourcePropertiesEdit properties = managedSite.getPropertiesEdit();
                    String property = properties.getProperty(siteProperty);
                    if (property == null || !property.equals(site.getTitle())) {
                        properties.addProperty(siteProperty, site.getTitle());
                        try {
                            siteService.save(managedSite);
                        } catch (IdUnusedException e) {
                            log.warn("Failed to find site we just loaded: "+ managedSite.getId(), e);
                        } catch (PermissionException e) {
                            log.warn("No permission to save managed site: "+ managedSite.getId(), e);
                        }
                    }
                }
            }
        }
    }


}
