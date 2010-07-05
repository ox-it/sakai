package uk.ac.ox.oucs.vle;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractSingleSpringContextTests;

public abstract class TestOnSampleData extends AbstractSingleSpringContextTests {

	SessionFactory factory;


	protected String[] getConfigPaths() {
		return new String[]{"/components.xml", "/test-components.xml"};
	}
	
	public void onSetUp() throws Exception {
		factory = (SessionFactory) getApplicationContext().getBean("sessionFactory");
	}



}