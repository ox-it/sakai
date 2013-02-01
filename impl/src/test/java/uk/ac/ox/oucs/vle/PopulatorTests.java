package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

public class PopulatorTests extends AbstractTransactionalSpringContextTests {

	private CourseDAOImpl courseDao;
	private SessionFactory sessionFactory;
	private PopulatorWrapper populator;
	
	private String prefix = new String("test.populator");

	public void onSetUp() throws Exception {
		super.onSetUp();
		courseDao = (CourseDAOImpl) getApplicationContext().getBean("uk.ac.ox.oucs.vle.CourseDAO");
		sessionFactory = (SessionFactory) getApplicationContext().getBean("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory");
		populator = (PopulatorWrapper) getApplicationContext().getBean("uk.ac.ox.oucs.vle.OxcapPopulatorWrapper");
	}
	
	public void onTearDown() throws Exception {
		super.onTearDown();
	} 


	protected String[] getConfigPaths() {
		//return new String[]{"/components.xml", "/test-components.xml"};
		return new String[]{"/course-signup-beans.xml", "/test-sakai-beans.xml"};
	}
	
	public void testPopulator() {
		
		Map<String, String> contextMap = new HashMap<String, String>();
		contextMap.put(prefix+".uri", "file:///home/marc/oxford-sakai-2.8/extras/course-signup/impl/xcri.xml");
		contextMap.put(prefix+"..username", "");
		contextMap.put(prefix+".password", "");
		contextMap.put(prefix+".name", "test");
		
		PopulatorContext pContext = new PopulatorContext(prefix, contextMap);
		populator.update(pContext);
		
		CourseGroupDAO group = courseDao.findCourseGroupById("3273");
		assertNotNull(group);
	}
	
}