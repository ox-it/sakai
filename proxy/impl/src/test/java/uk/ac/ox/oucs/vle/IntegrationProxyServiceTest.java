package uk.ac.ox.oucs.vle;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Just test that we've got everything wired up correctly.
 * @author buckett
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:proxy.xml"})
public class IntegrationProxyServiceTest extends Assert {

	@Autowired
	private ProxyService proxyService;
	
	@Test
	public void testRepeatbleSignature() {
		String signature1 = proxyService.getSignature("http://news.bbc.co.uk/");
		String signature2 = proxyService.getSignature("http://news.bbc.co.uk/");
		assertEquals(signature1, signature2);
	}

	@Test
	public void testRoundTrip() {
		String url = proxyService.getProxyURL("http://news.bbc.co.uk/");
		assertNotNull(url);
		String signature = proxyService.getSignature("http://news.bbc.co.uk/");
		// Roughly split out the URL.
		Map<String,String> params = new HashMap<String, String>();
		String splitParams[] = url.substring(url.indexOf('?')+1).split("&");
		for (String param: splitParams) {
			String kv[] = param.split("=", 2);
			if (kv.length == 2) {
				params.put(kv[0], kv[1]);
			}
		}
		assertEquals(signature, params.get("sig"));
	}
}
