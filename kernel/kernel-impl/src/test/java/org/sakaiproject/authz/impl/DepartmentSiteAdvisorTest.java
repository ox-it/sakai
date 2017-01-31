package org.sakaiproject.authz.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * Tests the department site advisor.
 */
@RunWith(MockitoJUnitRunner.class)
public class DepartmentSiteAdvisorTest {

    private DepartmentSiteAdvisor advisor;

    @Mock
    private DevolvedSakaiSecurityImpl devolvedSakaiSecurity;
    @Mock
    private SiteService siteService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private EventTrackingService eventTrackingService;

    @Before
    public void setUp() {
        advisor = new DepartmentSiteAdvisor();
        advisor.setDevolvedSakaiSecurity(devolvedSakaiSecurity);
        advisor.setSiteService(siteService);
        advisor.setEntityManager(entityManager);
        advisor.setEventTrackingService(eventTrackingService);
        advisor.setSiteProperty("property");

    }

    @Test
    public void testInit() {
        advisor.init();
        verify(siteService).addSiteAdvisor(advisor);
        verify(eventTrackingService).addLocalObserver(advisor);
    }

    // Checks that when an admin site is changed on a site the new title is set.
    @Test
    public void testAdminChangeSite() throws PermissionException, IdUnusedException {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(DevolvedSakaiSecurityImpl.ADMIN_REALM_CHANGE);
        when(event.getResource()).thenReturn("/site/id");
        Site site = mock(Site.class);
        when(site.getReference()).thenReturn("/site/id");
        ResourceProperties properties = mock(ResourceProperties.class);
        when(site.getProperties()).thenReturn(properties);

        Reference reference = mock(Reference.class);
        when(reference.getEntity()).thenReturn(site);
        when(entityManager.newReference("/site/id")).thenReturn(reference);

        when(devolvedSakaiSecurity.getAdminRealm("/site/id")).thenReturn("/site/admin");
        Reference adminReference = mock(Reference.class);
        when(entityManager.newReference("/site/admin")).thenReturn(adminReference);
        Site adminSite = mock(Site.class);
        when(adminSite.getTitle()).thenReturn("Admin Title");
        when(adminReference.getEntity()).thenReturn(adminSite);

        advisor.update(null, event);

        verify(properties).addProperty("property", "Admin Title");
        verify(siteService).save(site);
    }

    // Checks that when an admin site is removed on a site the old title is remove.
    @Test
    public void testAdminChangeNone() throws PermissionException, IdUnusedException {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(DevolvedSakaiSecurityImpl.ADMIN_REALM_CHANGE);
        when(event.getResource()).thenReturn("/site/id");
        Site site = mock(Site.class);
        when(site.getReference()).thenReturn("/site/id");
        ResourceProperties properties = mock(ResourceProperties.class);
        when(site.getProperties()).thenReturn(properties);

        Reference reference = mock(Reference.class);
        when(reference.getEntity()).thenReturn(site);
        when(entityManager.newReference("/site/id")).thenReturn(reference);

        when(devolvedSakaiSecurity.getAdminRealm("/site/id")).thenReturn(null);
        Reference adminReference = mock(Reference.class);
        when(entityManager.newReference("/site/admin")).thenReturn(adminReference);
        Site adminSite = mock(Site.class);
        when(adminSite.getTitle()).thenReturn("Admin Title");
        when(adminReference.getEntity()).thenReturn(adminSite);

        advisor.update(null, event);

        verify(properties).removeProperty("property");
        verify(siteService).save(site);
    }

    // Checks when an admin site's title is set the managed sites are updated.
    @Test
    public void testAdminUpdate() throws PermissionException, IdUnusedException {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(SiteService.SECURE_UPDATE_SITE);
        when(event.getResource()).thenReturn("/site/admin");
        Reference adminReference = mock(Reference.class);
        Site adminSite = mock(Site.class);
        when(entityManager.newReference("/site/admin")).thenReturn(adminReference);
        when(adminReference.getEntity()).thenReturn(adminSite);
        when(adminSite.getReference()).thenReturn("/site/admin");
        when(adminSite.getType()).thenReturn("admin");
        when(adminSite.getTitle()).thenReturn("Admin Title");

        Site site = mock(Site.class);
        ResourcePropertiesEdit properties = mock(ResourcePropertiesEdit.class);
        when(site.getPropertiesEdit()).thenReturn(properties);

        when(devolvedSakaiSecurity.getAdminSiteType()).thenReturn("admin");
        when(devolvedSakaiSecurity.findUsesOfAdmin("/site/admin")).thenReturn(Arrays.asList(site));

        advisor.update(null, event);

        verify(properties).addProperty("property", "Admin Title");
        verify(siteService).save(site);
    }

    // Checks when an admin site's title is changed the managed sites are updated.
    @Test
    public void testAdminUpdateChange() throws PermissionException, IdUnusedException {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(SiteService.SECURE_UPDATE_SITE);
        when(event.getResource()).thenReturn("/site/admin");
        Reference adminReference = mock(Reference.class);
        Site adminSite = mock(Site.class);
        when(entityManager.newReference("/site/admin")).thenReturn(adminReference);
        when(adminReference.getEntity()).thenReturn(adminSite);
        when(adminSite.getReference()).thenReturn("/site/admin");
        when(adminSite.getType()).thenReturn("admin");
        when(adminSite.getTitle()).thenReturn("Admin Title");

        Site site = mock(Site.class);
        ResourcePropertiesEdit properties = mock(ResourcePropertiesEdit.class);
        when(site.getPropertiesEdit()).thenReturn(properties);

        when(devolvedSakaiSecurity.getAdminSiteType()).thenReturn("admin");
        when(devolvedSakaiSecurity.findUsesOfAdmin("/site/admin")).thenReturn(Arrays.asList(site));

        // Give site an old title.
        when(properties.getProperty("property")).thenReturn("Old Admin Title");

        advisor.update(null, event);

        verify(properties).addProperty("property", "Admin Title");
        verify(siteService).save(site);
    }


}
