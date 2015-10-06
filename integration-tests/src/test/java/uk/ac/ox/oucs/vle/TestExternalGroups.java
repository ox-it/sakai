package uk.ac.ox.oucs.vle;

import java.util.*;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.User;
import org.springframework.context.ApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class TestExternalGroups extends AbstractDependencyInjectionSpringContextTests {

	private ExternalGroupManagerImpl groupManager;

	@Override
	protected String[] getConfigLocations() {
		return new String[]{"classpath:/test.xml"};
	}
	
	protected void onSetUp() {
		ApplicationContext context = getApplicationContext();
		groupManager = (ExternalGroupManagerImpl) context.getBean("ExternalGroupManager");

		Cache cache = Mockito.mock(Cache.class);
		final Map<Object, Object> map = new HashMap<>();
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				map.put(arguments[0], arguments[1]);
				return Void.TYPE;
			}
		}).when(cache).put(Mockito.any(), Mockito.any());
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				return map.get(arguments[0]);
			}
		}).when(cache).get(Mockito.any());

		MemoryService memoryService = Mockito.mock(MemoryService.class);
		Mockito.when(memoryService.newCache(Mockito.anyString())).thenReturn(cache);

		groupManager.setMemoryService(memoryService);

		MappedGroupDao mappedGroupDao = Mockito.mock(MappedGroupDao.class);
		groupManager.setMappedGroupDao(mappedGroupDao);

		groupManager.init();
	}
	
	public void testSearch() throws Exception {
		List<ExternalGroup> groups = groupManager.search(new String[]{"IT", "Services"});
		assertTrue("Expected to find the IT Services group.", groups.size() > 0);
	}
	
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
	}

	public void testBrowseCourseDepartments() throws Exception {
		List<ExternalGroupNode> groups = groupManager.findNodes(ExternalGroupManagerImpl.COURSES);
		assertFalse(groups.isEmpty());
	}

	public void testWalkTree() throws Exception {
		// Takes about 3 minutes
		Queue<ExternalGroupNode> queue = new ArrayDeque<>();
		queue.addAll(groupManager.findNodes(null));
		while (!queue.isEmpty()) {
			ExternalGroupNode node = queue.poll();
			System.out.println(node.getPath());
			// TODO Current if you attempt to ask for the nodes and it has groups you get an error, it shouldn't.
			if (!node.hasGroup()) {
				List<ExternalGroupNode> nodes = groupManager.findNodes(node.getPath());
				queue.addAll(nodes);
			}
		}
	}
}
