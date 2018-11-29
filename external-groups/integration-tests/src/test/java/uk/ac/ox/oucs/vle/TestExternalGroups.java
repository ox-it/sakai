package uk.ac.ox.oucs.vle;

import java.util.*;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import edu.amc.sakai.user.LdapConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
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

import static org.mockito.Mockito.*;
import static uk.ac.ox.oucs.vle.ExternalGroupManagerImpl.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/test.xml"})
public class TestExternalGroups extends Assert {

	private Log log = LogFactory.getLog(TestExternalGroups.class);

	private ExternalGroupManagerImpl groupManager;
	private LdapConnectionManager ldapConnectionManager;

	@Autowired
	protected ApplicationContext applicationContext;

	@Before
	public void onSetUp() {
		groupManager = applicationContext.getBean(ExternalGroupManagerImpl.class);
		ldapConnectionManager = applicationContext.getBean(LdapConnectionManager.class);

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
		when(memoryService.newCache(anyString())).thenReturn(cache);

		groupManager.setMemoryService(memoryService);

		MappedGroupDao mappedGroupDao = mock(MappedGroupDao.class);
		groupManager.setMappedGroupDao(mappedGroupDao);
		// TODO This is a hack and there should really be better isolation between tests.
		groupManager.setLdapConnectionPool(ldapConnectionManager);

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
		LdapConnectionManager bean = applicationContext.getBean(LdapConnectionManager.class);
		LdapConnectionManager spyConnectionManager = spy(bean);
		LDAPConnection connection = bean.getConnection();
		LDAPConnection spyConnection = spy(connection);
		// This is so that we fail the connection the first time.
		doThrow(LDAPException.class).when(spyConnection).search(eq(COURSE_BASE), anyInt(), anyString(), any(String[].class), anyBoolean());
        when(spyConnectionManager.getConnection()).thenCallRealMethod().thenReturn(spyConnection);
		// Reset the object with our spy
		groupManager.setLdapConnectionPool(spyConnectionManager);

		List<ExternalGroupNode> groups = groupManager.findNodes(COURSES);
		assertFalse(groups.isEmpty());
		// We get back lots of possible owners, but only some of them acutally offer courses.
		assertTrue(groups.size() > 200);
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
			if (!node.hasGroup()) {
				List<ExternalGroupNode> nodes = groupManager.findNodes(node.getPath());
				queue.addAll(nodes);
			}
		}
		log.debug("Tree size: "+ count);
	}
}
