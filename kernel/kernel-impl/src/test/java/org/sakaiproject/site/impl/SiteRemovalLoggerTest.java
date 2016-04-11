package org.sakaiproject.site.impl;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * These are mainly to check we don't blow up with a NPE with logger with bad site data.
 * @author Matthew Buckett
 */
public class SiteRemovalLoggerTest {

	private SiteRemovalLogger logger;
	private UserDirectoryService userDirectoryService;

	@Before
	public void setUp() {
		logger = new SiteRemovalLogger();

		userDirectoryService = mock(UserDirectoryService.class);
		logger.setUserDirectoryService(userDirectoryService);
	}

	@Test
	public void testLogSoftlyDelete() {
		User user = mock(User.class);
		when(user.getDisplayName()).thenReturn("Display Name");
		when(user.getDisplayId()).thenReturn("display-id");

		Site site = mock(Site.class);
		when(site.getId()).thenReturn("siteId");
		when(site.getTitle()).thenReturn("Site Title");
		when(site.getType()).thenReturn("type");
		when(site.isSoftlyDeleted()).thenReturn(true);
		when(site.getModifiedDate()).thenReturn(new Date(0));
		when(site.getModifiedBy()).thenReturn(user);

		when(userDirectoryService.getCurrentUser()).thenReturn(user);

		assertEquals("Removing Site ID: siteId title: Site Title type: type marked for deletion by: Display " +
				"Name(display-id) at: 1970-01-01T00:00Z removed by: Display Name(display-id)", logger.buildMessage(site));
	}

	@Test
	public void testLogNullSiteValues() {
		Site site = mock(Site.class);
		assertEquals("Removing Site ID: null title: null type: null removed by: unknown",logger.buildMessage(site));
		when(site.isSoftlyDeleted()).thenReturn(true);
		assertEquals("Removing Site ID: null title: null type: null marked for deletion by: unknown removed by: unknown",
				logger.buildMessage(site));
	}

	@Test
	public void testDateFormatting() {
		Date date = new Date(0);
		assertEquals("1970-01-01T00:00Z", logger.displayDate(date));
	}


}
