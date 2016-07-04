package org.sakaiproject.authz.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;
import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Tests two factor authentication code.
 * @author buckett
 *
 */
public class TwoFactorAuthenticationTest extends TestCase {

	@Mock
	private ServerConfigurationService serverConfigurationService;
	@Mock
	private EntityManager entityManager;
	@Mock
	private SessionManager sessionManager;
	@Mock
	private SiteService siteService;
	@Mock
	private ThreadLocalManager threadLocalManager;

	private TwoFactorAuthenticationImpl twoFactorAuthentication;

	@Override
	public void setUp() {
		 MockitoAnnotations.initMocks(this);
	}

	public void wireUp() {
		twoFactorAuthentication = new TwoFactorAuthenticationImpl();
		twoFactorAuthentication.setEntityManager(entityManager);
		twoFactorAuthentication.setSessionManager(sessionManager);
		twoFactorAuthentication.setServerConfigurationService(serverConfigurationService);
		twoFactorAuthentication.setSiteService(siteService);
		twoFactorAuthentication.setThreadLocalManager(threadLocalManager);
		twoFactorAuthentication.init();
	}

	public void testEnabled() {
		when(serverConfigurationService.getBoolean("twofactor.enable", false)).thenReturn(false);
		when(serverConfigurationService.getString(eq("twofactor.site.type"), anyString())).thenReturn("secure");

		wireUp();

		assertFalse(twoFactorAuthentication.isTwoFactorRequired("/site/someid"));
	}

	public void testNormalSite() {
		when(serverConfigurationService.getBoolean("twofactor.enable", false)).thenReturn(true);
		when(serverConfigurationService.getString(eq("twofactor.site.type"), anyString())).thenReturn("secure");

		Reference reference = mock(Reference.class);
		Site site = mock(Site.class);
		when(site.getType()).thenReturn("project");
		when(reference.getEntity()).thenReturn(site);
		when(reference.getType()).thenReturn(SiteService.APPLICATION_ID);
		when(entityManager.newReference("/site/someid")).thenReturn(reference);

		wireUp();

		assertFalse(twoFactorAuthentication.isTwoFactorRequired("/site/someid"));
	}

	public void testSecureSite() throws IdUnusedException {
		when(serverConfigurationService.getBoolean("twofactor.enable", false)).thenReturn(true);
		when(serverConfigurationService.getString(eq("twofactor.site.type"), anyString())).thenReturn("secure");

		Reference reference = mock(Reference.class);
		Site site = mock(Site.class);
		when(site.getType()).thenReturn("secure");
		when(reference.getType()).thenReturn(SiteService.APPLICATION_ID);
		when(reference.getId()).thenReturn("someid");
		when(reference.getSubType()).thenReturn(SiteService.SITE_SUBTYPE);
		when(entityManager.newReference("/site/someid")).thenReturn(reference);
		when(siteService.getSite("someid")).thenReturn(site);
		when(siteService.siteExists("someid")).thenReturn(true);

		wireUp();

		assertTrue(twoFactorAuthentication.isTwoFactorRequired("/site/someid"));

	}

	public void testSecureSiteGroup() throws IdUnusedException {
		when(serverConfigurationService.getBoolean("twofactor.enable", false)).thenReturn(true);
		when(serverConfigurationService.getString(eq("twofactor.site.type"), anyString())).thenReturn("secure");

		Reference reference = mock(Reference.class);
		Site site = mock(Site.class);
		when(site.getType()).thenReturn("secure");
		when(reference.getType()).thenReturn(SiteService.APPLICATION_ID);
		when(reference.getId()).thenReturn("someGroupId"); // The ID of this reference is the group.
		when(reference.getContext()).thenReturn("someid");
		when(reference.getSubType()).thenReturn(SiteService.GROUP_SUBTYPE);
		when(entityManager.newReference("/site/someid/group/someGroupId")).thenReturn(reference);
		when(siteService.getSite("someid")).thenReturn(site);
		when(siteService.siteExists("someid")).thenReturn(true);

		wireUp();

		assertTrue(twoFactorAuthentication.isTwoFactorRequired("/site/someid/group/someGroupId"));

	}
	

	public void testNormalSiteContent() throws Exception {
		// Check that content in a normal site is allowed through.
		when(serverConfigurationService.getBoolean("twofactor.enable", false)).thenReturn(true);
		when(serverConfigurationService.getString(eq("twofactor.site.type"), anyString())).thenReturn("secure");

		Reference reference = mock(Reference.class);
		when(reference.getType()).thenReturn(ContentHostingService.APPLICATION_ID);
		when(reference.getContext()).thenReturn("siteid");
		when(entityManager.newReference("/content/group/siteid/somefile.txt")).thenReturn(reference);

		Site site = mock(Site.class);
		when(site.getType()).thenReturn("project");
		when(siteService.getSite("siteid")).thenReturn(site);

		when(entityManager.newReference("/site/someid")).thenReturn(reference);

		wireUp();

		assertFalse(twoFactorAuthentication.isTwoFactorRequired("/content/group/siteid/somefile.txt"));
	}

	public void testSecureSiteContent() throws Exception {
		// Check that we block access to uploaded content in a two factor site.
		when(serverConfigurationService.getBoolean("twofactor.enable", false)).thenReturn(true);
		when(serverConfigurationService.getString(eq("twofactor.site.type"), anyString())).thenReturn("secure");

		Reference reference = mock(Reference.class);
		when(reference.getType()).thenReturn(ContentHostingService.APPLICATION_ID);
		when(reference.getContext()).thenReturn("siteid");
		when(entityManager.newReference("/content/group/siteid/somefile.txt")).thenReturn(reference);

		Site site = mock(Site.class);
		when(site.getType()).thenReturn("secure");
		when(siteService.getSite("siteid")).thenReturn(site);
		when(siteService.siteExists("siteid")).thenReturn(true);

		when(entityManager.newReference("/site/someid")).thenReturn(reference);

		wireUp();

		assertTrue(twoFactorAuthentication.isTwoFactorRequired("/content/group/siteid/somefile.txt"));
	}

	public void testNullReference() {
		// Check that when security checks without a reference are don't this code doesn't break them.
		when(serverConfigurationService.getBoolean("twofactor.enable", false)).thenReturn(true);
		when(serverConfigurationService.getString(eq("twofactor.site.type"), anyString())).thenReturn("secure");
		wireUp();
		assertFalse(twoFactorAuthentication.isTwoFactorRequired(null));

	}
}
