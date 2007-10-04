package org.sakaiproject.hierarchy.test.contextable;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.test.ServiceProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class ContextableServiceProvider implements ServiceProvider
	{

		private BasicDataSource ds;
		private HierarchyService service;

		public void setUp()
		{

			BeanFactory factory = new XmlBeanFactory(new ClassPathResource("spring.xml",getClass()));

			service = (HierarchyService)factory.getBean("org.sakaiproject.hierarchy.api.PortalHierarchyService");
			ds = (BasicDataSource)factory.getBean("javax.sql.DataSource");

		}

		public void tearDown() throws SQLException
		{
			ds.getConnection().prepareStatement("SHUTDOWN").execute();
			ds.close();
		}

		public HierarchyService getService() {
			return service;
		}
}
