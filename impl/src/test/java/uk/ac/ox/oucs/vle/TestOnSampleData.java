package uk.ac.ox.oucs.vle;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractSingleSpringContextTests;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import uk.ac.ox.oucs.vle.proxy.SakaiProxyTest;

public abstract class TestOnSampleData extends AbstractTransactionalSpringContextTests {

	private SessionFactory factory;
	protected CourseSignupService service;
	protected SakaiProxyTest proxy;
	protected CourseDAOImpl dao;

	protected void onSetUpBeforeTransaction() {
		transactionManager.toString();
	}

	protected String[] getConfigPaths() {
		return new String[]{"/components.xml", "/test-components.xml"};
	}
	
	public void setFactory(SessionFactory factory) {
		this.factory = factory;
	}
	
	public SessionFactory getFactory() {
		return this.factory;
	}

	public CourseSignupService getService() {
		return service;
	}

	public void setService(CourseSignupService service) {
		this.service = service;
	}

	public SakaiProxyTest getProxy() {
		return proxy;
	}

	public void setProxy(SakaiProxyTest proxy) {
		this.proxy = proxy;
	}

	public void setDao(CourseDAOImpl dao) {
		this.dao = dao;
	}
	



}