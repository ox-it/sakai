package org.sakaiproject.citation.impl.soloapi;

import org.codehaus.jackson.JsonNode;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.AbstractSingleSpringContextTests;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SoloApiServiceImplTest extends AbstractSingleSpringContextTests {

	private SoloApiServiceImpl service;
	private ServerConfigurationService serverConfigurationService;

	protected void onSetUp() throws Exception {
		this.service = (SoloApiServiceImpl) getApplicationContext().getBean("org.sakaiproject.citation.api.SoloApiServiceImpl");
		this.serverConfigurationService = (ServerConfigurationService) getApplicationContext().getBean("org.sakaiproject.component.api.ServerConfigurationService");
		when(serverConfigurationService.getString(anyString()))
				.thenReturn("http://solo.bodleian.ox.ac.uk/PrimoWebServices/xservice/search/brief?institution=OX&query=rid,exact,<<<RFTID>>>&onCampus=true&indx=1" +
						"&bulkSize=1&loc=local,scope:(OX,primo_central,ELD)&loc=adaptor,primo_central_multiple_fe&json=true");
	}

	protected String[] getConfigLocations() {
		return new String[] {
				"classpath:org/sakaiproject/citation/impl/soloapi/test-beans.xml"
		};
	}

	public void testConvertCitationNullContext() {
		try {
			service.convert(null);
			fail( "Missing exception" );
		} catch(IllegalArgumentException e) {
			assertEquals( "null context passed to convert(Context context)", e.getMessage() );
		}
	}

	public void testConvertCitationNullNode() {
		try {
			ContextObject co = new ContextObject();
			service.convert(co);
			fail( "Missing exception" );
		} catch(IllegalArgumentException e) {
			assertEquals( "null jsonNode passed to convert(Context context)", e.getMessage() );
		}
	}

	public void testParseNull() {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "http://localhost:8080/someurl");
		try {
			service.parse(req);
			fail( "Missing exception" );
		} catch(IllegalArgumentException e) {
			assertEquals( "null rft_id passed to parse(HttpServletRequest request)", e.getMessage() );
		}
	}

	public void testParseSolo() {
		MockHttpServletRequest req = createRequest(SampleSoloApiURLs.SOLO_API_EXAMPLE);
		ContextObject ContextObjectObject = service.parse(req);
		assertNotNull(ContextObjectObject);
		JsonNode book = ContextObjectObject.getNode();
		assertEquals("BOOK", book.get("SEGMENTS").get("JAGROOT").get("RESULT").get("DOCSET").get("DOC").get("PrimoNMBib").get("record").get("addata").get("ristype").getTextValue());

		Citation bookCitation = service.convert(ContextObjectObject);
		assertEquals("Linux in a nutshell", bookCitation.getCitationProperty(Schema.TITLE, false));
	}

	private MockHttpServletRequest createRequest(String openUrl) {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", openUrl);
		req.setQueryString(openUrl);
		req.setParameters(parseQueryString(openUrl));
		return req;
	}

	public void testParsePrimoFullId() {
		MockHttpServletRequest req = createRequest(SampleSoloApiURLs.SOLO_API_EXAMPLE_2);

		ContextObject ContextObjectObject = service.parse(req);
		Citation citation = service.convert(ContextObjectObject);

		assertNotNull(citation.getCitationProperty("otherIds", false));
		assertEquals("Cheese", citation.getCitationProperty("title", false));
	}

	public void testParseBook() {
		Citation book = convert(find(mockGetRequest(SampleSoloApiURLs.BOOK)));
		Map props = book.getCitationProperties();
		assertEquals("Patent searching : tools & techniques", props.get("title"));
		assertEquals("9780471783794", props.get("isnIdentifier"));
		assertEquals("2007", props.get("year"));
		assertEquals("[Hunt, David, Nguyen, Long, Rodgers, Matthew]", props.get("creator").toString());
	}

	public void testParseAnotherBook() {
		Citation book = convert(find(mockGetRequest(SampleSoloApiURLs.ANOTHER_BOOK)));
		Map props = book.getCitationProperties();
		assertEquals("Butler, Nicholas Murray", props.get("creator").toString());
		assertEquals("Philosophy", props.get("title"));
		assertEquals(null, props.get("isnIdentifier"));
		assertEquals("The Columbia University Press", props.get("publisher"));
		assertEquals("1938", props.get("year"));
		assertEquals("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=oxfaleph011277642&vid=OXVU1&fn=display&displayMode=full", props.get("otherIds").toString());
	}

	public void testParseYetAnotherBook() {
		Citation book = convert(find(mockGetRequest(SampleSoloApiURLs.YET_ANOTHER_BOOK)));
		Map props = book.getCitationProperties();
		assertEquals("Oliver, Martyn", props.get("creator").toString());
		assertEquals("Philosophy", props.get("title"));
		assertEquals("9780600592235", props.get("isnIdentifier"));
		assertEquals("Hamlyn", props.get("publisher"));
		assertEquals("1997", props.get("year"));
		assertEquals("London", props.get("publicationLocation"));
		assertEquals("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=oxfaleph012764388&vid=OXVU1&fn=display&displayMode=full", props.get("otherIds").toString());
	}

	public void testParseNotAnotherBook() {
		Citation book = convert(find(mockGetRequest(SampleSoloApiURLs.NOT_ANOTHER_BOOK)));
		Map props = book.getCitationProperties();
		assertEquals("Siever, Ellen", props.get("creator").toString());
		assertEquals("Linux in a nutshell", props.get("title"));
		assertEquals("9780596154486", props.get("isnIdentifier"));
		assertEquals("O'Reilly", props.get("publisher"));
		assertEquals("2009", props.get("year"));
		assertEquals("6th ed.", props.get("edition"));
		assertEquals("Sebastopol, Calif. ; Cambridge", props.get("publicationLocation"));
		assertEquals("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=oxfaleph017140770&vid=OXVU1&fn=display&displayMode=full", props.get("otherIds").toString());
	}

	public void testParseSampleBook() {
		HttpServletRequest req = mockGetRequest(SampleSoloApiURLs.BOOK);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("Patent searching : tools & techniques", citation.getCitationProperty("title", false));
		assertEquals("[Hunt, David, Nguyen, Long, Rodgers, Matthew]", citation.getCitationProperty("creator", false).toString());
		assertEquals("9780471783794", citation.getCitationProperty("isnIdentifier", false));
	}

	public void testParseSampleText() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.TEXT);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("Complete transcatheter closure of a patent arterial duct with subsequent haemolysis", citation.getCitationProperty("title", false));
		assertEquals("Cardiology in the Young", citation.getCitationProperty("sourceTitle", false));
		assertEquals("[Cace, Neven, Ahel, Vladimir, Bilic, Iva]", citation.getCitationProperty("creator", false).toString());
		assertEquals("2010-08", citation.getCitationProperty("date", false));
		assertEquals("20", citation.getCitationProperty("volume", false));
		assertEquals("4", citation.getCitationProperty("issue", false));
		assertEquals("462", citation.getCitationProperty("startPage", false));
		assertEquals("464", citation.getCitationProperty("endPage", false));
		assertEquals("1047-9511", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1017/S1047951110000326", citation.getCitationProperty("doi", false));
	}

	public Citation convert(ContextObject ContextObjectObject) {
		Citation citation = service.convert(ContextObjectObject);
		assertNotNull(citation);
		return citation;
	}

	public HttpServletRequest mockGetRequest(String url) {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", url);
		init(req);
		return req;
	}

	public ContextObject find(HttpServletRequest req) {
		ContextObject ContextObjectObject = service.parse(req);
		assertNotNull(ContextObjectObject);
		return ContextObjectObject;
	}

	/**
	 * Sets up the query string and parameters based on the URL.
	 * @param req
	 */
	public void init(MockHttpServletRequest req) {
		String url = req.getRequestURL().toString();
		int queryStart = url.indexOf('?');
		if (queryStart >= 0) {
			String query = url.substring(queryStart+1);
			req.setQueryString(query);
			req.setParameters(parseQueryString(query));
		}
	}

	// Stolen from HttpUtils.parseQueryString()
	public static Map parseQueryString(String s) {
		String valArray[] = null;

		if (s == null) {
			throw new IllegalArgumentException();
		}
		Hashtable ht = new Hashtable();
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(s, "&");
		while (st.hasMoreTokens()) {
			String pair = (String) st.nextToken();
			int pos = pair.indexOf('=');
			if (pos == -1) {
				// XXX
				// should give more detail about the illegal argument
				throw new IllegalArgumentException();
			}
			String key = parseName(pair.substring(0, pos), sb);
			String val = parseName(pair.substring(pos + 1, pair.length()), sb);
			if (ht.containsKey(key)) {
				String oldVals[] = (String[]) ht.get(key);
				valArray = new String[oldVals.length + 1];
				for (int i = 0; i < oldVals.length; i++)
					valArray[i] = oldVals[i];
				valArray[oldVals.length] = val;
			} else {
				valArray = new String[1];
				valArray[0] = val;
			}
			ht.put(key, valArray);
		}
		return ht;
	}

	// Stolen from HttpUtils.parseQueryString()
	static private String parseName(String s, StringBuffer sb) {
		sb.setLength(0);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '+':
					sb.append(' ');
					break;
				case '%':
					try {
						sb.append((char) Integer.parseInt(
								s.substring(i + 1, i + 3), 16));
						i += 2;
					} catch (NumberFormatException e) {
						// XXX
						// need to be more specific about illegal arg
						throw new IllegalArgumentException();
					} catch (StringIndexOutOfBoundsException e) {
						String rest = s.substring(i);
						sb.append(rest);
						if (rest.length() == 2)
							i++;
					}

					break;
				default:
					sb.append(c);
					break;
			}
		}
		return sb.toString();
	}

	public void testSampleJournal() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.JOURNAL);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("Kingston journal", citation.getCitationProperty("sourceTitle", false));
		assertEquals("1868", citation.getCitationProperty("date", false));
	}

	public void testSampleLegal() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.LEGAL);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertTrue(((String)citation.getCitationProperty("title", false)).startsWith("Humanitarian intervention"));
		assertEquals("1994", citation.getCitationProperty("year", false));
	}

	public void testSampleDissertation() {
		// Again a book, but using article title.
		HttpServletRequest req = createRequest(SampleSoloApiURLs.DISSERTATION);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("Thomas Blanchard's Patent Management", citation.getCitationProperty("title", false));
		assertEquals("The Journal of Economic History", citation.getCitationProperty("sourceTitle", false));
		assertEquals("Cooper, Carolyn C", citation.getCitationProperty("creator", false));
		assertEquals("1987-06", citation.getCitationProperty("date", false));
		assertEquals("47", citation.getCitationProperty("volume", false));
		assertEquals("2", citation.getCitationProperty("issue", false));
		assertEquals("487", citation.getCitationProperty("startPage", false));
		assertEquals("488", citation.getCitationProperty("endPage", false));
		assertEquals("0022-0507", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1017/S002205070004821X", citation.getCitationProperty("doi", false));
	}

	// We had an issue with this one where both the title and sourceTitle ended up with being the the same.
	public void testSamplePrimoJournalCorrect() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.CORRECT_JOURNAL);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("Theory and practice of logic programming.", citation.getCitationProperty("sourceTitle", false));
		assertEquals("", citation.getCitationProperty("title", false));
	}

	public void testParseSampleArticle() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.ARTICLE);
		ContextObject co = service.parse(req);

		Citation citation = service.convert(co);

		assertEquals("Patent", citation.getCitationProperty("title", false));
		assertEquals("Metal Powder Report", citation.getCitationProperty("sourceTitle", false));
		assertEquals("1992", citation.getCitationProperty("date", false));
		assertEquals("47", citation.getCitationProperty("volume", false));
		assertEquals("6", citation.getCitationProperty("issue", false));
		assertEquals("59", citation.getCitationProperty("startPage", false));
		assertEquals("61", citation.getCitationProperty("endPage", false));
		assertEquals("0026-0657", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1016/0026-0657(92)91523-M", citation.getCitationProperty("doi", false));
	}

	public void testParseSampleNewspaper() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.NEWSPAPER);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("The national era", citation.getCitationProperty("sourceTitle", false));
		assertEquals("1847", citation.getCitationProperty("date", false));
		assertEquals("1847", citation.getCitationProperty("year", false));
		assertEquals("Washington, D.C", citation.getCitationProperty("publicationLocation", false));
		assertEquals("L.P. Noble", citation.getCitationProperty("publisher", false));
		assertEquals("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=oxfaleph011255518&vid=OXVU1&fn=search&tab=remote&displayMode=full", citation.getCitationProperty("otherIds", false));
	}

	public void testParseSampleOther() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.OTHER);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		// Fails because it's described as a book, but is actually a journal
		assertEquals("Transcatheter closure of a patent foramen ovale following mitral valve replacement", citation.getCitationProperty("title", false));
		assertEquals("Annals of Thoracic Surgery", citation.getCitationProperty("sourceTitle", false));
		assertEquals("[Skulski, R., Snider, J.M., Buzzard, C.J., Ling, F.S., Mendelsohn, A.M.]", citation.getCitationProperty("creator", false).toString());
		assertEquals("1999-08", citation.getCitationProperty("date", false));
		assertEquals("68", citation.getCitationProperty("volume", false));
		assertEquals("2", citation.getCitationProperty("issue", false));
		assertEquals("582", citation.getCitationProperty("startPage", false));
		assertEquals("583", citation.getCitationProperty("endPage", false));
		assertEquals("00034975", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1016/S0003-4975(99)00600-1", citation.getCitationProperty("doi", false));
	}

	public void testSamplePrimoImage() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.IMAGE);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("greek new media shit", citation.getCitationProperty("title", false));
		assertEquals("2011", citation.getCitationProperty("date", false));
		assertEquals("Sterling Crispin", citation.getCitationProperty("creator", false));
		assertEquals("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=TN_artstorARHIZOMEIG_10313507317&vid=OXVU1&fn=display&displayMode=full", citation.getCitationProperty("otherIds", false));
	}

	public void testSamplePrimoAnotherImage() {
		HttpServletRequest req = createRequest(SampleSoloApiURLs.ANOTHER_IMAGE);
		ContextObject co = service.parse(req);
		Citation citation = service.convert(co);
		assertEquals("How do we use ancient Greek ideas today?", citation.getCitationProperty("title", false));
		assertEquals("2002", citation.getCitationProperty("date", false));
		assertEquals("2002", citation.getCitationProperty("year", false));
		assertEquals("London", citation.getCitationProperty("publicationLocation", false));
		assertEquals("Woodhouse, Jayne", citation.getCitationProperty("creator", false).toString());
		assertEquals("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=oxfaleph020302996&vid=OXVU1&fn=display&displayMode=full", citation.getCitationProperty("otherIds", false));
	}
}
