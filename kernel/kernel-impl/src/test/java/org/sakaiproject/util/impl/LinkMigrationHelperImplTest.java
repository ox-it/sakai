package org.sakaiproject.util.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Matthew Buckett
 */
@RunWith(MockitoJUnitRunner.class)
public class LinkMigrationHelperImplTest {

	@Mock
	private ServerConfigurationService serverConfigurationService;

	private LinkMigrationHelperImpl linkMigration;

	@Before
	public void setUp() {
		linkMigration = new LinkMigrationHelperImpl();
		linkMigration.setServerConfigurationService(serverConfigurationService);
		when(serverConfigurationService.getString("LinkMigrationHelper.linksToBracket","assignment,forum"))
				.thenReturn("assignment,forum");
		when(serverConfigurationService.getString("LinkMigrationHelper.linksToNullify","sam_pub,/posts/"))
				.thenReturn("sam_pub,/posts/");
	}

	@Test
	public void testSimpleBracket() throws Exception {
		String source = "This is a <a href='http://news.bbc.co.uk/'>link</a>.";
		assertEquals(source, linkMigration.bracketAndNullifySelectedLinks(source));
	}

	@Test
	public void testSimpleBracketWithEncodedSpace() throws Exception {
		String source = "This is a <a href='http://example.com/hello%20world'>http://example.com/hello%20world</a>";
		assertEquals(source, linkMigration.bracketAndNullifySelectedLinks(source));
	}

	@Test
	public void testSimpleMigrateSiteId() {
		String source = "This has a <a href='/access/content/group/siteId/myfile.txt'>file</a> which is good.";
		Map<String, String> replacements = Collections.singletonMap("siteId", "newSiteId");
		assertEquals("This has a <a href='/access/content/group/newSiteId/myfile.txt'>file</a> which is good.",
				linkMigration.migrateAllLinks(replacements.entrySet(), source));
	}

	@Test
	public void testSimpleMigrateNoMatch() {
		String source = "This has a <a href='/access/content/group/siteId/myfile.txt'>file</a> which is good.";
		Map<String, String> replacements = Collections.singletonMap("noMatch", "newSiteId");
		assertEquals(source, linkMigration.migrateAllLinks(replacements.entrySet(), source));
	}

	@Test
	public void testSimpleMigrateMultipleMatch() {
		String source = "This is a <a href='/old/location/link1.txt'>Link</a>."+
				"This is another <a href='/old/location/link1.txt'>Link</a>."+
				"This is a third <a href='/old/location/link2.txt'>Link</a>.";
		String result = "This is a <a href='/new/location/link1.txt'>Link</a>."+
				"This is another <a href='/new/location/link1.txt'>Link</a>."+
				"This is a third <a href='/new/location/link2.txt'>Link</a>.";
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("/old/location/link1.txt", "/new/location/link1.txt");
		replacements.put("/old/location/link2.txt", "/new/location/link2.txt");
		replacements.put("/old/location/link3.txt", "/new/location/link3.txt");
		assertEquals(result, linkMigration.migrateAllLinks(replacements.entrySet(), source));
	}


	//@Test
	// Currently fails, but shouldn't
	public void testSimpleBracketWithStrings() throws Exception {
		String source = "This is a <a href='http://forum.com/'>message board</a>";
		assertEquals(source, linkMigration.bracketAndNullifySelectedLinks(source));
	}

	//@Test
	// Currently fails, but shouldn't
	public void testSimpleNullifyWithStrings() throws Exception {
		String source = "This is a <a href='http://mysite.com/posts/all.jsp'>message board posts</a>";
		assertEquals(source, linkMigration.bracketAndNullifySelectedLinks(source));
	}

}
