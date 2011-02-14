package uk.ac.ox.oucs.vle;

import java.util.Random;

import junit.framework.TestCase;

public class ProxyServiceTest extends TestCase {

	private ProxyServiceImpl proxyService;
	
	public void setUp() {
		proxyService = new ProxyServiceImpl();
		proxyService.setSecret("mysecret");
		proxyService.init();
	}
	
	public void testSignature() {
		try {
			proxyService.getSignature(null);
			fail("Should have had an exception.");
		} catch (Exception e) {
			// Good
		}
		
		assertNotNull(proxyService.getSignature(""));
		
		assertNotSame(
				proxyService.getSignature("http://news.bbc.co.uk").intern(),
				proxyService.getSignature("http://www.amazon.co.uk").intern()
				);
		// Make sure it's reproduceable.
		assertEquals("j9aOudbA1ojfODEMNZK_ZapTi28", proxyService.getSignature("http://someurl/path"));
	}
	
	public void testProxyUrl() {
		assertEquals(
				"/proxy/?url=http%3A%2F%2Fnews.bbc.co.uk&sig=Gctj_bZkGYAKG2fnw3z-OmM0UWE",
				proxyService.getProxyURL("http://news.bbc.co.uk")
				);
	}
	
	public void test1000Urls() {
		// Just to check it's not REALLY slow.
		Random rnd = new Random();
		for (int i = 0; i < 1000; i++) {
			String url = proxyService.getProxyURL("http://some.url.that.is.reasonably.long/so/it/is/not/too/quick"+ rnd.nextInt(10000));
			assertNotNull(url);
		}
	}
}
