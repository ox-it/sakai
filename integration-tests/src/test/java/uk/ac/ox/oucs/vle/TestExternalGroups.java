package uk.ac.ox.oucs.vle;

import java.util.Iterator;
import java.util.List;

import org.sakaiproject.user.api.User;
import org.springframework.context.ApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class TestExternalGroups extends AbstractDependencyInjectionSpringContextTests {

	private ExternalGroupManager groupManager;

	@Override
	protected String[] getConfigLocations() {
		return new String[]{"classpath:/test.xml"};
	}
	
	protected void onSetUp() {
		ApplicationContext context = getApplicationContext();
		groupManager = (ExternalGroupManager)context.getBean("ExternalGroupManager");
	}
	
	public void testSearch() throws Exception {
		List<ExternalGroup> groups = groupManager.search("Computing services");
		assertTrue("Expected to find the OUCS group.", groups.size() > 0);
	}
	
	public void testFindById() throws Exception {
		ExternalGroup group = groupManager.findExternalGroup("oakUnitCode=oucs,ou=units,dc=oak,dc=ox,dc=ac,dc=uk");
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
	


}
