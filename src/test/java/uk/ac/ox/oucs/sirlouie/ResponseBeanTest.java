package uk.ac.ox.oucs.sirlouie;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;

import uk.ac.ox.oucs.sirlouie.reply.SearLibrary;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;
import uk.ac.ox.oucs.sirlouie.response.ResponseBean;

public class ResponseBeanTest extends TestCase {
	
	private String json = "{\"version\":\"0.5\",\"schema\":\"http://ws.gbv.de/daia/\","
		+"\"timestamp\":\"2009-06-09T15:39:52.831+02:00\","
		+"\"institution\":{\"content\":\"University of Oxford\","
		+"\"href\":\"http://www.ox.ac.uk\"},"
		+"\"document\":[{\"id\":\"UkOxUUkOxUb15585873\","
		+"\"href\":\"http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045\","
		+"\"item\":["
		+"{\"department\":{\"id\":\"BOD\",\"content\":\"Bodleian Library\"},"
		+"\"storage\":{\"content\":\"bookstack\"}},"
		+"{\"department\":{\"id\":\"BLL\",\"content\":\"Balliol College Library\"},"
		+"\"storage\":{\"content\":\"Main Libr\"}},"
		+"{\"department\":{\"id\":\"BLL\",\"content\":\"Balliol College Library\"},"
		+"\"storage\":{\"content\":\"Main Libr\"}},"
		+"{\"department\":{\"id\":\"BLL\",\"content\":\"Balliol College Library\"},"
		+"\"storage\":{\"content\":\"Main Libr\"}}]}]}";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void testToJSON() {
		try {
			ResponseBean response = new ResponseBean("UkOxUUkOxUb15585873");
			
			Collection <SearObject> beans = new ArrayList<SearObject>();
			beans.add(new SearLibrary("OX", "BOD", "check_holdings" ,"bookstack", "(M00.D01645)", "http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045"));
			beans.add(new SearLibrary("OX", "BLL", "check_holdings" ,"Main Libr", "(0360 h 015/01)", "http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045"));
			beans.add(new SearLibrary("OX", "BLL", "check_holdings" ,"Main Libr", "(0360 h 015/02)", "http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045"));
			beans.add(new SearLibrary("OX", "BLL", "check_holdings" ,"Main Libr", "(0360 h 015/02)", "http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045"));
			
			
			response.addSearObjects(beans);
			
			Map<String, Object> jsonData = response.toJSON("2009-06-09T15:39:52.831+02:00");
			ObjectMapper mapper = new ObjectMapper();
			//mapper.writeValue(new File("response.json"), jsonData);
			
			Assert.assertEquals(json, mapper.writeValueAsString(jsonData));
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
		//fail("Not yet implemented");
	}

}
