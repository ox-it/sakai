package org.sakaiproject.content.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.site.api.SiteService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UnpublishedSiteAttachmentsTest {

    private BaseContentService baseContentService;
    private SiteService siteService;

    @Before
    public void setUp() {
        baseContentService = new DbContentService();
        siteService = Mockito.mock(SiteService.class);
        baseContentService.setSiteService(siteService);
    }

    @Test
    public void testAnother() {
        assertTrue(baseContentService.allowGetResourceContext("/should/never/do/anything"));
    }

    @Test
    public void testAttachmentOutsideSite() {
        assertTrue(baseContentService.allowGetResourceContext("/attachment/someId"));
    }

    @Test
    public void testAttachmentNoSiteId() {
        assertTrue(baseContentService.allowGetResourceContext("/attachment//Assignments/file"));
        verify(siteService, never()).siteExists("");
    }


    @Test
    public void testAttachmentInSite() {
        when(siteService.siteExists("siteId")).thenReturn(true);
        when(siteService.allowAccessSite("siteId")).thenReturn(true);
        assertTrue(baseContentService.allowGetResourceContext("/attachment/siteId/Assignments/file"));
    }

    @Test
    public void testAttachmentInNonExistentSite() {
        when(siteService.siteExists("siteId")).thenReturn(false);
        when(siteService.allowAccessSite("siteId")).thenReturn(false);
        assertTrue(baseContentService.allowGetResourceContext("/attachment/siteId/Assignments/file"));
    }

    @Test
    public void testAttachmentWithNoSiteAccess() {
        when(siteService.siteExists("siteId")).thenReturn(true);
        when(siteService.allowAccessSite("siteId")).thenReturn(false);
        assertFalse(baseContentService.allowGetResourceContext("/attachment/siteId/Assignments/file"));
    }

}
