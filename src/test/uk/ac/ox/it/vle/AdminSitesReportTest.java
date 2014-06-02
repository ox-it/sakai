package uk.ac.ox.it.vle;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.sakaiproject.site.api.SiteService.SelectionType;

/**
 * @author Matthew Buckett
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminSitesReportTest {

	public static final String ROLE = "coordinator";
	public static final String DIVISION = "division";

	@Mock
	private UserDirectoryService userDirectoryService;

	@Mock
	private SiteService siteService;

	@Mock
	private AdminSiteReportWriter adminSiteReportWriter;

	@Captor
	ArgumentCaptor<InputStream> captor;

	private AdminSitesReport adminSitesReport;

	@Before
	public void setUp() {
		adminSitesReport = new AdminSitesReport();
		adminSitesReport.setUserDirectoryService(userDirectoryService);
		adminSitesReport.setSiteService(siteService);
		adminSitesReport.setReportWriter(adminSiteReportWriter);
		adminSitesReport.setDivisionNames(Collections.singletonMap(DIVISION, "Division"));
	}

	@Test
	public void testSingleDivision() throws JobExecutionException, IOException {
		Site site = newSite("1", new HashSet<String>(Arrays.asList("1")));

		User user = newUser("1");

		Mockito.when(siteService.getSites(Matchers.eq(SelectionType.ANY), Matchers.anyString(), Matchers.anyString(), Matchers.anyMap(), Matchers.any(SiteService.SortType.class), Matchers.any(PagingPosition.class))).thenReturn(
				Collections.singletonList(site)
		);
		Mockito.when(userDirectoryService.getUsers(Matchers.any(Collection.class))).thenReturn(Collections.singletonList(user));

		JobExecutionContext ctx = Mockito.mock(JobExecutionContext.class);
		adminSitesReport.execute(ctx);
		Mockito.verify(adminSiteReportWriter, Mockito.atLeastOnce()).writeReport(Matchers.anyString(), Matchers.anyString(), captor.capture(), Matchers.any(AdminSiteReportWriter.Access.class));
		Assert.assertNotNull(captor.getAllValues());
		for (InputStream stream: captor.getAllValues()) {
			IOUtils.copy(stream, System.out);
		}
	}

	@Test
	 public void testEmptyDivision() throws JobExecutionException, IOException {
		User user = newUser("1");

		Mockito.when(siteService.getSites(Matchers.eq(SelectionType.ANY), Matchers.anyString(), Matchers.anyString(), Matchers.anyMap(), Matchers.any(SiteService.SortType.class), Matchers.any(PagingPosition.class))).thenReturn(
				Collections.<Site>emptyList()
		);
		Mockito.when(userDirectoryService.getUsers(Matchers.any(Collection.class))).thenReturn(Collections.singletonList(user));

		JobExecutionContext ctx = Mockito.mock(JobExecutionContext.class);
		adminSitesReport.execute(ctx);
		Mockito.verify(adminSiteReportWriter, Mockito.atLeastOnce()).writeReport(Matchers.anyString(), Matchers.anyString(), captor.capture(), Matchers.any(AdminSiteReportWriter.Access.class));
		Assert.assertNotNull(captor.getAllValues());
	}

	@Test
	public void testDuplicateUser() throws JobExecutionException, IOException {
		User user1 = newUser("user1");
		User user2 = newUser("user2");

		Set<String> site1Users = new TreeSet<String>(Arrays.asList("user1"));
		Site site1 = newSite("site1", site1Users);
		Set<String> site2Users = new TreeSet<String>(Arrays.asList("user1", "user2"));
		Site site2 = newSite("site2", site2Users);

		Mockito.when(siteService.getSites(Matchers.eq(SelectionType.ANY), Matchers.anyString(), Matchers.anyString(), Matchers.anyMap(), Matchers.any(SiteService.SortType.class), Matchers.any(PagingPosition.class))).thenReturn(
				Arrays.asList(site1, site2)
			);
		Mockito.when(userDirectoryService.getUsers(site1Users)).thenReturn(Arrays.asList(user1));
		Mockito.when(userDirectoryService.getUsers(site2Users)).thenReturn(Arrays.asList(user1, user2));

		JobExecutionContext ctx = Mockito.mock(JobExecutionContext.class);
		adminSitesReport.execute(ctx);
		Mockito.verify(adminSiteReportWriter, Mockito.times(1)).writeReport(Matchers.eq("emails.txt"), Matchers.eq("text/plain"), captor.capture(), Matchers.any(AdminSiteReportWriter.Access.class));
		InputStream stream = captor.getValue();
		String output = IOUtils.toString(stream);
		assertEquals("user1@hostname\nuser2@hostname\n", output);

	}

	private User newUser(String id) {
		User user = Mockito.mock(User.class);
		Mockito.when(user.getDisplayName()).thenReturn("User Display Name "+id);
		Mockito.when(user.getEmail()).thenReturn(id+"@hostname");
		Mockito.when(user.getDisplayId()).thenReturn(id);
		return user;
	}

	private Site newSite(String id, Set<String> userIds) {
		Site site = Mockito.mock(Site.class);
		Mockito.when(site.getTitle()).thenReturn("Site Title "+id);
		Mockito.when(site.getId()).thenReturn(id);
		Mockito.when(site.getUrl()).thenReturn("http://hostname/portal/"+ id);
		Mockito.when(site.getUsersHasRole(ROLE)).thenReturn(userIds);
		ResourceProperties resourceProperties = Mockito.mock(ResourceProperties.class);
		Mockito.when(site.getProperties()).thenReturn(resourceProperties);
		Mockito.when(resourceProperties.getProperty(AdminSitesReport.DEFAULT_DIVISION_PROP)).thenReturn(DIVISION);
		return site;
	}
}
