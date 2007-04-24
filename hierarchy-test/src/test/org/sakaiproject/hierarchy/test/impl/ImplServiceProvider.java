package org.sakaiproject.hierarchy.test.impl;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.sakaiproject.db.impl.BasicSqlService;
import org.sakaiproject.db.impl.SqlServiceTest;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.impl.HierarchyServiceImpl;
import org.sakaiproject.hierarchy.impl.ibatis.dao.HierarchyDAO;
import org.sakaiproject.hierarchy.test.ServiceProvider;
import org.sakaiproject.id.impl.UuidV4IdComponent;
import org.sakaiproject.orm.ibatis.SqlMapClientFactoryBean;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.thread_local.impl.ThreadLocalComponent;
import org.sakaiproject.util.LocalClassPathResource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.Resource;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class ImplServiceProvider implements ServiceProvider {


	private BasicDataSource ds;
	private HierarchyService service;

	public void setUp()
	{
		BeanFactory factory = new XmlBeanFactory(new LocalClassPathResource("spring.xml", this.getClass()));

		service = (HierarchyService)factory.getBean("org.sakaiproject.hierarchy.api.HierarchyService");
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
