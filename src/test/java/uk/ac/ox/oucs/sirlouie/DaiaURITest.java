package uk.ac.ox.oucs.sirlouie;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import uk.ac.ox.oucs.sirlouie.utils.DaiaURI;

public class DaiaURITest extends TestCase {
	
	String uri_1 = "http%3A%2F%2Fsolo.bodleian.ox.ac.uk%2Fprimo_library%2Flibweb%2Faction%2Fdisplay.do"
		+"%3Fdoc%3DUkOxUb15244849%26vid%3DOXVU1%26fn%3Ddisplay%26displayMode%3Dfull";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void testDaiaURI() {
		try {
			DaiaURI daia = new DaiaURI(uri_1);
			
			System.out.println("scheme ["+daia.getURI().getScheme()+"]");
			System.out.println("host   ["+daia.getURI().getHost()+"]");
			System.out.println("path   ["+daia.getURI().getPath()+"]");
			System.out.println("query  ["+daia.getURI().getQuery()+"]");
			
			assertEquals("UkOxUb15244849",daia.getDoc());
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}

}
