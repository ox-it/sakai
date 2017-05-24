package org.sakaiproject.authz.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.authz.api.DevolvedSakaiSecurity;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DevolvedSakaiSecurityTest extends SakaiKernelTestBase {

	private static Logger log = LoggerFactory.getLogger(DevolvedSakaiSecurityTest.class);

	@BeforeClass
	public static void beforeClass() {
		try {
			log.debug("starting oneTimeSetup");
			oneTimeSetup();
			log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Test
	public void testCheck() throws Exception {
		
		// Get the Service.
		DevolvedSakaiSecurity security = (DevolvedSakaiSecurity)getService(SecurityService.class.getName());
		SecurityService securityService = getService(SecurityService.class);

		SiteService siteService = (SiteService)getService(SiteService.class.getName());
		
		UserDirectoryService userService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		
		SessionManager sessionManager = (SessionManager)getService(SessionManager.class.getName());

		MemoryService memoryService = getService(MemoryService.class);

		UserEdit user1 = userService.addUser("user1", "user1");
		userService.commitEdit(user1);
		UserEdit user2 = userService.addUser("user2", "user2");
		userService.commitEdit(user2);
		
		// Login as admin (to set stuff up).
		Session session = sessionManager.getCurrentSession();
		session.setUserId("admin");
		
		Site site1 = siteService.addSite("site1", (String)null);
        site1.setPublished(true);
		Role adminRole = site1.addRole("admin");
		adminRole.allowFunction("site.upd");
		// So user can use role swap
		adminRole.allowFunction(SiteService.SITE_ROLE_SWAP);
		Role memberRole = site1.addRole("member");
		memberRole.allowFunction("site.visit");
		site1.addMember("user1", "admin", true, false);
		siteService.save(site1);
		
		Site site2 = siteService.addSite("site2", (String)null);
		Role adminRole2 = site2.addRole("admin");
		adminRole2.allowFunction(SiteService.SECURE_UPDATE_SITE);
		adminRole2.allowFunction(DevolvedSakaiSecurity.ADMIN_REALM_PERMISSION);
		Role useRole = site2.addRole("user");
		useRole.allowFunction(DevolvedSakaiSecurity.ADMIN_REALM_PERMISSION_USE);

		site2.addMember("user2", "admin", true, false);
		site2.addMember("user1", "user", true, false);
		siteService.save(site2);

		// This site shouldn't allow it's maintain user to change the admin site.
		Site siteRestricted = siteService.addSite("siteRestricted", (String)null);
		siteRestricted.setType("submission");
		Role custom = siteRestricted.addRole("custom");
		custom.allowFunction(SiteService.SECURE_UPDATE_SITE);
		siteRestricted.addMember("user2", "custom", true, false);
		siteService.save(siteRestricted);

		assertTrue(site1.isAllowed("user1", SiteService.SECURE_UPDATE_SITE));
		assertTrue(site2.isAllowed("user2", SiteService.SECURE_UPDATE_SITE));

		
		session.setUserId("user2");
		assertFalse(siteService.allowUpdateSite(site1.getId()));
		assertFalse(security.canSetAdminRealm(siteRestricted.getReference()));
		
		// Make people in site2 with the right permission be able to modify site one.
		session.setUserId("user1");
		assertTrue(security.canSetAdminRealm(site1.getReference()));
		security.setAdminRealm(site1.getReference(), site2.getReference());

        // TODO This should really invalidate correctly.
        session.setUserId("admin");
		memoryService.resetCachers();

		session.setUserId("user2");
		assertTrue(siteService.allowUpdateSite(site1.getId()));

		// Roleswap works as expected, you don't get permissions from admin site.
		assertTrue(securityService.setUserEffectiveRole(site1.getReference(), "member"));

	}

}
