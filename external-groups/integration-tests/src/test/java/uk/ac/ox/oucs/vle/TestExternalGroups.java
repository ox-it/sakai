package uk.ac.ox.oucs.vle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.ExternalGroupManagerImpl.COURSES;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/test.xml"})
public class TestExternalGroups {

	private Log log = LogFactory.getLog(TestExternalGroups.class);

	private ExternalGroupManagerImpl groupManager;

	@Autowired
	protected ApplicationContext applicationContext;

	@BeforeClass
	public static void onlyOnce() {
		String config = "/test.properties";
		assumeTrue("Test skipped as can't find on classpath: " + config,
				TestExternalGroups.class.getResourceAsStream(config) != null);
	}

	@Before
	public void onSetUp() {
		groupManager = applicationContext.getBean(ExternalGroupManagerImpl.class);

		Cache<Object, Object> cache = Utils.mockCache();

		MemoryService memoryService = mock(MemoryService.class);
		when(memoryService.getCache(anyString())).thenReturn(cache);

		groupManager.setMemoryService(memoryService);

		MappedGroupDao mappedGroupDao = mock(MappedGroupDao.class);
		groupManager.setMappedGroupDao(mappedGroupDao);
		groupManager.init();
	}

	@Test
	public void testSearch() throws Exception {
		List<ExternalGroup> groups = groupManager.search(new String[]{"IT", "Services"});
		assertTrue("Expected to find the IT Services group.", groups.size() > 0);
		log.debug("Search returned: "+ groups.size());
	}

	@Test
	public void testFindById() throws Exception {
		ExternalGroup group = groupManager.findExternalGroup("oakUnitCode=itserv,ou=units,dc=oak,dc=ox,dc=ac,dc=uk");
		assertNotNull(group);
		Iterator<User> users = group.getMembers();
		assertTrue(users.hasNext());
		int count = 0;
		while (users.hasNext()) {
			users.next();
			count++;
		}
		assertTrue(count > 0);
		log.debug("Members found: "+ count);
	}

	@Test
	public void testBrowseCourseDepartments() throws Exception {
		List<ExternalGroupNode> groups = groupManager.findNodes(COURSES);
		assertFalse(groups.isEmpty());
		assertTrue(groups.size() > 50);
		assertTrue(groups.size() < 100);
		log.debug("Groups size: "+ groups.size());
	}

	@Test
	public void testBrowseCourseDepartmentsWithTimeout() throws Exception {
		List<ExternalGroupNode> groups = groupManager.findNodes(COURSES);
		assertFalse(groups.isEmpty());
		// We get back lots of possible owners, but only some of them acutally offer courses.
		assertFalse(groups.size() > 200);
		log.debug("Groups size: "+ groups.size());
	}

	@Test
	public void testWalkTree() throws Exception {
		// Takes about 3 minutes
		Queue<ExternalGroupNode> queue = new ArrayDeque<>();
		queue.addAll(groupManager.findNodes(null));
		int count = 0;
		while (!queue.isEmpty()) {
			ExternalGroupNode node = queue.poll();
			count++;
			// TODO Current if you attempt to ask for the nodes and it has groups you get an error, it shouldn't.
            log.info(node.getPath());
			if (!node.hasGroup()) {
				List<ExternalGroupNode> nodes = groupManager.findNodes(node.getPath());
				queue.addAll(nodes);
			} else {
				log.info("Group: "+node.getPath()+" "+String.valueOf(groupManager.findMembers(node.getGroup().getId()).size()));
			}
		}
		log.info("Tree size: "+ count);
	}

	@Test
	public void testBadPart() throws Exception {
		// This part gets remapped across.
		List<ExternalGroupNode> nodes = groupManager.findNodes("units:oxuni:councildep:councildep");
		assertFalse(nodes.isEmpty());
	}
}
