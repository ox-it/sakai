package org.sakaiproject.authz.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.authz.api.DevolvedSakaiSecurity;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DevolvedSakaiSecurityTest extends SakaiKernelTestBase {
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		oneTimeSetup();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		oneTimeTearDown();
	}

	@Test
	public void testCheck() throws Exception {
		
		// Get the Service.
		DevolvedSakaiSecurity security = (DevolvedSakaiSecurity)getService(SecurityService.class.getName());
		
		SiteService siteService = (SiteService)getService(SiteService.class.getName());
		
		UserDirectoryService userService = (UserDirectoryService)getService(UserDirectoryService.class.getName());
		
		SessionManager sessionManager = (SessionManager)getService(SessionManager.class.getName());
		
		UserEdit user1 = userService.addUser("user1", "user1");
		userService.commitEdit(user1);
		UserEdit user2 = userService.addUser("user2", "user2");
		userService.commitEdit(user2);
		
		// Login as admin (to set stuff up).
		Session session = sessionManager.getCurrentSession();
		session.setUserId("admin");
		
		Site site1 = siteService.addSite("site1", (String)null);
		Role adminRole = site1.addRole("admin");
		adminRole.allowFunction("site.upd");
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
		
		assertTrue(site1.isAllowed("user1", SiteService.SECURE_UPDATE_SITE));
		assertTrue(site2.isAllowed("user2", SiteService.SECURE_UPDATE_SITE));
		
		
		session.setUserId("user2");
		assertFalse(siteService.allowUpdateSite(site1.getId()));
		
		// Make people in site2 with the right permission be able to modify site one.
		session.setUserId("user1");
		security.setAdminRealm(site1.getReference(), site2.getReference());
		
		session.setUserId("user2");

		// TODO Need to work on invalidation
		// assertTrue(siteService.allowUpdateSite(site1.getId()));
		
		
		
	}
}
