package uk.ac.ox.oucs.vle;

import junit.framework.TestCase;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLInputFactory;

public class XMLFactoryTest extends TestCase {

	public void testXMLFactory() {
		// Check that we are getting one from woodstox.
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
		assertNotNull(xmlInputFactory);
		assertTrue(xmlInputFactory instanceof XMLInputFactory2);
	}

	public void onlyWorksOnJDK6 () {
		// On JDK6 you have to specify the class that you want to be created.
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory("com.ctc.wstx.stax.WstxInputFactory",
				getClass().getClassLoader());
		assertNotNull(xmlInputFactory);
		assertTrue(xmlInputFactory instanceof XMLInputFactory2);
	}

	public void onlyWorksOnJDK7 () {
		// On JDK7 you have the specify the file in /META-INF/services/ which contains the class name.
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory("javax.xml.stream.XMLInputFactory",
				getClass().getClassLoader());
		assertNotNull(xmlInputFactory);
		assertTrue(xmlInputFactory instanceof XMLInputFactory2);
	}}
