package uk.ac.ox.oucs.vle;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractSingleSpringContextTests;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public abstract class TestOnSampleData extends AbstractTransactionalSpringContextTests {

	private SessionFactory factory;


	protected String[] getConfigPaths() {
		return new String[]{"/components.xml", "/test-components.xml"};
	}
	
	public void setFactory(SessionFactory factory) {
		this.factory = factory;
	}
	
	public SessionFactory getFactory() {
		return this.factory;
	}
	



}