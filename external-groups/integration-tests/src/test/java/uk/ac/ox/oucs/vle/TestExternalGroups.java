package uk.ac.ox.oucs.vle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.ExternalGroupManagerImpl.COURSES;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/test.xml"})
public class TestExternalGroups {

	private Log log = LogFactory.getLog(TestExternalGroups.class);

	private ExternalGroupManagerImpl groupManager;

	@Autowired
	protected ApplicationContext applicationContext;

	@Before
	public void onSetUp() {
		groupManager = applicationContext.getBean(ExternalGroupManagerImpl.class);

		Cache cache = mock(Cache.class);
		final Map<Object, Object> map = new HashMap<>();
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				map.put(arguments[0], arguments[1]);
				return Void.TYPE;
			}
		}).when(cache).put(any(), any());
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				return map.get(arguments[0]);
			}
		}).when(cache).get(any());

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
