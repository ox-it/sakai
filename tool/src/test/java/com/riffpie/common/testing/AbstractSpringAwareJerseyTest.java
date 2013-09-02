package com.riffpie.common.testing;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

/**
 * Test class which will wire itelf into your the Spring context which
 * is configured on the WebAppDecriptor built for your tests.
 * Ensure you configure annotation-aware support into your contexts,
 * and annotate any auto-wire properties on your test class
 * @author George McIntosh
 *
 */
public abstract class AbstractSpringAwareJerseyTest extends JerseyTest {

	public AbstractSpringAwareJerseyTest() {
		super();
	}

	public AbstractSpringAwareJerseyTest(WebAppDescriptor wad) {
		super(wad);
	}
	
	protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
		return new SpringAwareGrizzlyTestContainerFactory(this);
	}
	
}
