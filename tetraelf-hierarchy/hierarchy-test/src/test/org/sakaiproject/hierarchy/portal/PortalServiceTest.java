package org.sakaiproject.hierarchy.portal;


import org.sakaiproject.hierarchy.dao.HierarchyDao;
import org.sakaiproject.hierarchy.impl.CaretHierarchyServiceImpl;
import org.sakaiproject.hierarchy.impl.PortalHierarchyServiceImpl;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNodeDao;
import org.sakaiproject.site.api.SiteService;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import static org.easymock.EasyMock.*;

public class PortalServiceTest extends AbstractTransactionalSpringContextTests {

	private PortalHierarchyServiceImpl phs;
	private CaretHierarchyServiceImpl hs;
	private HierarchyDao hd;
	private PortalPersistentNodeDao ppnd;
	private SiteService ss;
	
	
	@Override
	protected String[] getConfigLocations() {
		// TODO Auto-generated method stub
		return new String[]{"classpath:/portal-spring-hibernate.xml", "classpath:/spring-hibernate.xml", "classpath:/hibernate-test.xml"};
	}
	
	protected void onSetUpBeforeTransaction() {
		
		ppnd = (PortalPersistentNodeDao) applicationContext.getBean("org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistenNodeDao");
		hd = (HierarchyDao)applicationContext.getBean("org.sakaiproject.hierarchy.dao.HierarchyDao");
		
		hs = new CaretHierarchyServiceImpl();
		hs.setDao(hd);
		
		ss = createMock(SiteService.class);
		
		phs = new PortalHierarchyServiceImpl();
		phs.setDao(ppnd);
		phs.setHierarchyService(hs);
		phs.setSiteService(ss);
		phs.setHierarchyId("portal");
		
		phs.init();
				
	}

	public void testSomething() {
		
	}
	
}
