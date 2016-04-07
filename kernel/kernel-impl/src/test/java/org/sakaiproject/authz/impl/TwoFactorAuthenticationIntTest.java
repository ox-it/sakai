package org.sakaiproject.authz.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.authz.api.TwoFactorAuthentication;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TwoFactorAuthenticationIntTest extends SakaiKernelTestBase {

	@BeforeClass
	public static void setUp() throws Exception {
		oneTimeSetup("two_factor");
	}
	@AfterClass
	public static void tearDown() throws Exception {
		oneTimeTearDown();
	}

	/**
	 * Fuller integration test of two factor authentication stopping a user from accessing a site.
	 */
	@Test
	public void testTwoFactorRequired() throws Exception {

		SiteService siteService = (SiteService)ComponentManager.get(SiteService.class);

		SessionManager sessionManager = (SessionManager)ComponentManager.get(SessionManager.class);

		UserDirectoryService userDirectoryService = (UserDirectoryService)ComponentManager.get(UserDirectoryService.class);

		ContentHostingService contentHostingService = (ContentHostingService)ComponentManager.get(ContentHostingService.class);

		TwoFactorAuthentication twoFactorAuthentication = (TwoFactorAuthentication)ComponentManager.get(TwoFactorAuthentication.class);


		// set the user information into the current session
		Session sakaiSession = sessionManager.getCurrentSession();
		sakaiSession.setUserEid("admin");
		sakaiSession.setUserId("admin");

		UserEdit user = userDirectoryService.addUser("other", "other");
		user.setFirstName("First");
		user.setLastName("Last");
		userDirectoryService.commitEdit(user);

		String id = IdManager.createUuid();
		Site secureSite = siteService.addSite(id, "project");
		secureSite.setTitle("A Secure Site");
		secureSite.setPublished(true);
		secureSite.addMember("other", "access", true, false);
		siteService.save(secureSite);

		String siteCollectionId = contentHostingService.getSiteCollection(id);
		String resourceId = siteCollectionId+ "test.txt";
		ContentResourceEdit resourceEdit = contentHostingService.addResource(resourceId);
		resourceEdit.setContent(new String("Just some basic content").getBytes());
		resourceEdit.setContentType("text/plain");
		contentHostingService.commitResource(resourceEdit);



		assertFalse(twoFactorAuthentication.isTwoFactorRequired(secureSite.getReference()));

		try {
			Site site = siteService.getSiteVisit(id);
		} catch (Exception e) {
			fail("Admin should be able to get to the site.");
		}

		try {
			contentHostingService.getReference(resourceId);
		} catch (Exception e) {
			fail("Admin should be able to get to the resource.");
		}

		sakaiSession.setUserEid("other");
		sakaiSession.setUserId("other");
		try {
			Site site = siteService.getSiteVisit(id);
		} catch (Exception e) {
			fail("Other user should be able to access the site as it's not yet secure");
		}
		try {
			contentHostingService.getResource(resourceId);
		} catch (Exception e) {
			fail("Other user should be able to access the site as it's not yet secure");
		}

		sakaiSession.setUserEid("admin");
		sakaiSession.setUserId("admin");

		secureSite.setType("secure");
		siteService.save(secureSite);

		assertTrue(twoFactorAuthentication.isTwoFactorRequired(secureSite.getReference()));

		// While we are still admin check that we can't actually get the file either.
		// WL-3453 
		try {
			contentHostingService.getResource(resourceId);
			fail("As we don't have two factor auth yet (even for admin), this should fail.");
		} catch (Exception e) {
		}

		sakaiSession.setUserEid("other");
		sakaiSession.setUserId("other");
		try {
			Site site = siteService.getSiteVisit(id);
			fail("As we don't have two factor auth yet, this should fail.");
		} catch (Exception e) {
		}
		try {
			contentHostingService.getResource(resourceId);
			fail("As we don't have two factor auth yet, this should fail.");
		} catch (Exception e) {
		}

		// This is to check that we don't get a stack overflow error on a site that doesn't
		// exist yet.
		String userSiteId = siteService.getUserSiteId(user.getId());
		try {
			Site site = siteService.getSiteVisit(userSiteId);
		} catch (Exception e) {
			fail("This should work");
		}
	}

}
