package edu.amc.sakai.user;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JLDAPDirectoryProviderPerformanceTest extends TestCase {

	public void testPerformance() throws Exception {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("/perftest.xml");
		
		JLDAPDirectoryProvider udp = (JLDAPDirectoryProvider) context.getBean("org.sakaiproject.user.api.UserDirectoryProvider");
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/eidlist.txt")));
		String line = reader.readLine();
		ArrayList<UserEditStub> eids = new ArrayList<UserEditStub>();
		while (line != null) {
			String eid = line.trim();
			eids.add(new UserEditStub(eid));
			line = reader.readLine();
		}
		
		for (UserEditStub eid: eids) {
			udp.getUser(eid);
		}
	}
	
	
}
