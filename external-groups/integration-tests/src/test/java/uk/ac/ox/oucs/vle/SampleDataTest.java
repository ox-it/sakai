package uk.ac.ox.oucs.vle;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.UserDirectoryService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SampleDataTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private InMemoryDirectoryServer ds;
    private LDAPConnectionPool pool;

    @Mock
    private UserDirectoryService userDirectoryService;
    @Mock
    private MappedGroupDao mappedGroupDao;
    @Mock
    private MemoryService memoryService;
    private ExternalGroupManagerImpl groupManager;

    @Before
    public void setUp() throws LDAPException {
        // Create the configuration to use for the server.
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=oak,dc=ox,dc=ac,dc=uk");
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        // We don't want to validate the schema.
        config.setSchema(null);

        // Create the directory server instance, populate it with data from the
        // "test-data.ldif" file, and start listening for client connections.
        ds = new InMemoryDirectoryServer(config);
        ds.importFromLDIF(true, new LDIFReader(getClass().getResourceAsStream("/sample/structure.ldif")));
        ds.importFromLDIF(false, new LDIFReader(getClass().getResourceAsStream("/sample/user.ldif")));
        ds.importFromLDIF(false, new LDIFReader(getClass().getResourceAsStream("/sample/itserv.ldif")));
        ds.importFromLDIF(false, new LDIFReader(getClass().getResourceAsStream("/sample/R16_1.ldif")));
        ds.startListening();

        pool = new LDAPConnectionPool(ds.getConnection(), 10);

        Cache<Object, Object> cache = Utils.mockCache();
        when(memoryService.getCache(anyString())).thenReturn(cache);

        groupManager = new ExternalGroupManagerImpl();
        groupManager.setLdapConnectionPool(pool);
        groupManager.setUserDirectoryService(userDirectoryService);
        groupManager.setMappedGroupDao(mappedGroupDao);
        groupManager.setMemoryService(memoryService);
        Map<String, String> displayNames = new HashMap<>();
        displayNames.put("centadm", "UAS");
        displayNames.put("mathsci", "MPLS");
        groupManager.setDisplayNames(displayNames);
        groupManager.init();
    }

    @After
    public void tearDown() {
        pool.close();
        ds.shutDown(true);
    }

    @Test
    public void testNothing() {
        // Just check that the test framework is ok.
    }

    @Test
    public void testRootNodes() throws ExternalGroupException {
        List<ExternalGroupNode> nodes = groupManager.findNodes("");
        assertEquals(2, nodes.size());
        // Check they aren't groups.
        assertFalse(nodes.get(0).hasGroup());
        assertFalse(nodes.get(1).hasGroup());
    }

    @Test
    public void testUnitNode() throws ExternalGroupException {
        List<ExternalGroupNode> nodes = groupManager.findNodes("units");
        assertEquals(2, nodes.size());
        {
            ExternalGroupNode node = nodes.get(0);
            assertEquals("UAS", node.getName());
            assertFalse(node.hasGroup());
            assertEquals("units:centadm", node.getPath());
        }
        {
            ExternalGroupNode node = nodes.get(1);
            assertEquals("MPLS", node.getName());
            assertFalse(node.hasGroup());
            assertEquals("units:mathsci", node.getPath());
        }
    }

    @Test
    public void testDepartmentNode() throws ExternalGroupException {
        List<ExternalGroupNode> nodes = groupManager.findNodes("units:centadm");
        assertEquals(1, nodes.size());
        ExternalGroupNode node = nodes.get(0);
        assertEquals("IT Services", node.getName());
        assertFalse(node.hasGroup());
        assertEquals("units:oxuni:centadm:itserv", node.getPath());
    }

    @Test
    public void testDepartmentNodeGroup() throws ExternalGroupException {
        List<ExternalGroupNode> nodes = groupManager.findNodes("units:oxuni:centadm:itserv");
        assertEquals(1, nodes.size());
        ExternalGroupNode node = nodes.get(0);
        assertEquals("IT Services, All", node.getName());
        assertTrue(node.hasGroup());
        assertThat(node.getPath(), startsWith("units:oxuni:centadm:itserv:cn=all"));
    }

    @Test
    public void testCoursesNode() throws ExternalGroupException {
        List<ExternalGroupNode> nodes = groupManager.findNodes("courses");
        assertEquals(1, nodes.size());
        ExternalGroupNode node = nodes.get(0);
        assertEquals("Department of Earth Sciences", node.getName());
        assertFalse(node.hasGroup());
        assertEquals("courses:4D03DG", node.getPath());
    }

    @Test
    public void testOwnerNode() throws ExternalGroupException {
        List<ExternalGroupNode> nodes = groupManager.findNodes("courses:4D03DG");
        assertEquals(1, nodes.size());
        ExternalGroupNode node = nodes.get(0);
        assertEquals("Oil and Gas (NERC CDT)", node.getName());
        assertFalse(node.hasGroup());
        assertEquals("courses:4D03DG:R16_9A1:R16_1", node.getPath());
    }

    @Test
    public void testRouteNodeGroups() throws ExternalGroupException {
        List<ExternalGroupNode> nodes = groupManager.findNodes("courses:4D03DG:R16_9A1:R16_1");
        assertEquals(3, nodes.size());
        {
            ExternalGroupNode node = nodes.get(0);
            assertEquals("Oil and Gas (NERC CDT), Current students", node.getName());
            assertTrue(node.hasGroup());
            ExternalGroup group = node.getGroup();
            assertThat(group.getId(), startsWith("cn=current,ou=R16_9A1,ou=route,ou=R16_1,ou=programme"));
            Iterator<String> members = group.getMemberEids();
            assertTrue(members.hasNext());
            assertEquals(members.next(), "1234");
            assertFalse(members.hasNext());
        }
        {
            ExternalGroupNode node = nodes.get(1);
            assertEquals("Oil and Gas (NERC CDT), starting 2017/18, current students only", node.getName());
            assertTrue(node.hasGroup());
            ExternalGroup group = node.getGroup();
            assertThat(group.getId(), startsWith("cn=2017-current,ou=start-year,ou=R16_9A1,ou=route,ou=R16_1,ou=programme"));
            Iterator<String> members = group.getMemberEids();
            assertTrue(members.hasNext());
            assertEquals(members.next(), "1234");
            assertFalse(members.hasNext());
        }
        {
            ExternalGroupNode node = nodes.get(2);
            assertEquals("Oil and Gas (NERC CDT), year 2, current students only", node.getName());
            assertTrue(node.hasGroup());
            ExternalGroup group = node.getGroup();
            assertThat(group.getId(), startsWith("cn=2-current,ou=year-of-study,ou=R16_9A1,ou=route,ou=R16_1,ou=programme"));
            Iterator<String> members = group.getMemberEids();
            assertTrue(members.hasNext());
            assertEquals(members.next(), "1234");
            assertFalse(members.hasNext());
        }
    }

}
