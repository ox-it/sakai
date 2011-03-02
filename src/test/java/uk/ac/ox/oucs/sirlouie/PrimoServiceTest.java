package uk.ac.ox.oucs.sirlouie;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONObject;

import uk.ac.ox.oucs.sirlouie.daia.ResponseBean;
import uk.ac.ox.oucs.sirlouie.primo.PrimoService;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;

public class PrimoServiceTest extends TestCase {
	
	PrimoService service;
	
	private String nameSpaceURI = "http://www.exlibrisgroup.com/xsd/jaguar/search";
	
	private String WEBRESOURCE_URL = "http://primo-s-web-2.sers.ox.ac.uk:1701/PrimoWebServices/xservice/getit";
	
	private String OLIS_XML = "<SEGMENTS xmlns=\"http://www.exlibrisgroup.com/xsd/jaguar/search\">"
		+"<JAGROOT>"
		+"<RESULT>"
		+"<DOCSET TOTALHITS=\"1\">"
		+"<sear:DOC xmlns=\"http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib\" "
		+"xmlns:sear=\"http://www.exlibrisgroup.com/xsd/jaguar/search\">"
		+"<PrimoNMBib>"
		+"<record>"
		+"<display>"
		+"<type>book</type>"
		+"<title>The history of the Times.</title>"
		+"<contributor>Stanley Morison 1889-1967.; Stanley Morison 1889-1967.; Stanley"
		+"Morison 1889-1967.; Stanley Morison 1889-1967.; Iverach McDonald; John Grigg;"
		+"Graham Stewart</contributor>"
		+"<publisher>London : The Times ; HarperCollins</publisher>"
		+"<creationdate>1935-</creationdate>"
		+"<subject>Times (London, England) -- History</subject>"
		+"<description>v. 1. \"The Thunderer\" in the making, 1785-1841 / Stanley Morison"
		+"-- v. 2. The tradition established, 1841-1884 / Stanley Morison -- v. 3. The twentieth century"
		+"test, 1884-1912 / Stanley Morison -- v. 4. in 2 parts. The 150th anniversary and beyond,"
		+"1912-1948 / Stanley Morison -- v. 5. Struggles in war and peace, 1939-1966 / by Iverach"
		+"McDonald -- v. 6. The Thomson years, 1966-1981 / by John Grigg -- v. 7. The Murdoch"
		+"years 1981-2002 / Graham Stewart.</description>"
		+"<language>eng</language>"
		+"<source>UkOxU</source>"
		+"<availlibrary>$$IOX$$LBLL$$1Main Libr$$2(0360 h 015/01)$"
		+"$Scheck_holdings</availlibrary>"
		+"<unititle>Times (London, England)</unititle>"
		+"<availinstitution>$$IOX$$Scheck_holdings</availinstitution>"
		+"</display>"
		+"<search>"
		+"<creatorcontrib>Morison, Stanley, 1889-1967.</creatorcontrib>"
		+"<title>The history of the Times.</title>"
		+"<subject>Times (London, England) History.</subject>"
		+"<general>The Times ; HarperCollins,</general>"
		+"<sourceid>UkOxU</sourceid>"
		+"<recordid>UkOxUUkOxUb10108045</recordid>"
		+"<isbn>0723002622</isbn>"
		+"<rsrctype>book</rsrctype>"
		+"<creationdate>1935</creationdate>"
		+"<lsr01>BLL:0360 h 015/01</lsr01>"
		+"<lsr01>BLL:0360 h 015/02</lsr01>"
		+"</search>"
		+"<sort>"
		+"<title>history of the Times.</title>"
		+"<creationdate>1935</creationdate>"
		+"<author>Morison, Stanley, 1889-1967.</author>"
		+"</sort>"
		+"<facets>"
		+"<language>eng</language>"
		+"<creationdate>1935</creationdate>"
		+"<topic>Times (London, England)–History</topic>"
		+"<collection>OLIS</collection>"
		+"<prefilter>books</prefilter>"
		+"<rsrctype>books</rsrctype>"
		+"<creatorcontrib>Morison, S</creatorcontrib>"
		+"<creatorcontrib>McDonald, I</creatorcontrib>"
		+"<creatorcontrib>Grigg, J</creatorcontrib>"
		+"<creatorcontrib>Stewart, G</creatorcontrib>"
		+"<library>BLL</library>"
		+"<library>BOD</library>"
		+"</facets>"
		+"</record>"
		+"</PrimoNMBib>"
		+"<sear:GETIT GetIt2=\"http://oxfordsfx-direct.hosted.exlibrisgroup.com/oxford?"
		+"ctx_ver=Z39.88-2004&amp;ctx_enc=info:ofi/"
		+"enc:UTF-8&amp;ctx_tim=2010-10-27T14%3A50%3A53IST&amp;url_ver=Z39.88-2004&amp;"
		+"url_ctx_fmt=infofi/fmt:kev:mtx:ctx&amp;rfr_id=info:sid/primo.exlibrisgroup.com:primo3-"
		+"Journal-UkOxU&amp;rft_val_fmt=info:ofi/"
		+"fmt:kev:mtx:book&amp;rft.genre=book&amp;rft.atitle=&amp;rft.jtitle=&amp;rft.btitle=The"
		+"%20history%20of%20the"
		+"%20Times.&amp;rft.aulast=Morison&amp;rft.auinit=&amp;rft.auinit1=&amp;rft.auinitm=&amp;"
		+"rft.ausuffix=&amp;rft.au=&amp;rft.aucorp=&amp;rft.volume=&amp;rft.issue=&amp;rft.part=&amp;"
		+"rft.quarter=&amp;rft.ssn=&amp;rft.spage=&amp;rft.epage=&amp;rft.pages=&amp;"
		+"rft.artnum=&amp;rft.issn=&amp;rft.eissn=&amp;rft.isbn=0723002622&amp;rft.sici=&amp;"
		+"rft.coden=&amp;rft_id=info:doi/&amp;rft.object_id="
		+"%20&amp;rft.eisbn=&amp;rft_dat=&lt;UkOxU>UkOxUb10108045&lt;/UkOxU>\" "
		+"GetIt1=\"http://1.1.1.1/cgi-bin/record_display_link.pl?id=10108045\" "
		+"deliveryCategory=\"Physical Item\"/>"
		+"<sear:LIBRARIES>"
		+"<sear:LIBRARY>"
		+"<sear:institution>OX</sear:institution>"
		+"<sear:library>BLL</sear:library>"
		+"<sear:status>check_holdings</sear:status>"
		+"<sear:collection>Main Libr</sear:collection>"
		+"<sear:callNumber>(0360 h 015/01)</sear:callNumber>"
		+"<sear:url>http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045</sear:url>"
		+"</sear:LIBRARY>"
		+"<sear:LIBRARY>"
		+"<sear:institution>OX</sear:institution>"
		+"<sear:library>BLL</sear:library>"
		+"<sear:status>check_holdings</sear:status>"
		+"<sear:collection>Main Libr</sear:collection>"
		+"<sear:callNumber>(0360 h 015/02)</sear:callNumber>"
		+"<sear:url>http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045</sear:url>"
		+"</sear:LIBRARY>"
		+"</sear:LIBRARIES>"
		+"<sear:LINKS>"
		+"<sear:backlink>http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045</sear:backlink>"
		+"<sear:thumbnail>http://images.amazon.com/images/P/"
		+"0723002622.01._SSTHUM_.jpg</sear:thumbnail>"
		+"<sear:thumbnail>http://books.google.com/books?bibkeys=ISBN:"
		+"9780007184385,OCLC:,LCCN:"
		+"35-27067&amp;jscmd=viewapi&amp;callback=updateGBSCover</sear:thumbnail>"
		+"<sear:linktotoc>http://syndetics.com/index.aspx?isbn=0723002622/"
		+"INDEX.HTML&amp;client=unioxford&amp;type=xw12</sear:linktotoc>"
		+"<sear:linktoabstract>http://syndetics.com/index.aspx?isbn=0723002622/"
		+"SUMMARY.HTML&amp;client=unioxford&amp;type=rn12</sear:linktoabstract>"
		+"<sear:linktoholdings>http://library.ox.ac.uk/cgi-bin/record_display_link.pl?"
		+"id=10108045</sear:linktoholdings>"
		+"<sear:linktouc>http://www.amazon.co.uk/gp/search?keywords=0723002622</sear:linktouc>"
		+"<sear:linktouc>http://www.worldcat.org/search?q=isbn%3A0723002622</sear:linktouc>"
		+"<sear:lln03>http://books.google.com/books?vid=ISBN0723002622</sear:lln03>"
		+"<sear:lln04>http://www.amazon.com/gp/reader/0723002622</sear:lln04>"
		+"</sear:LINKS>"
		+"</sear:DOC>"
		+"</DOCSET>"
		+"</RESULT>"
		+"</JAGROOT>"
		+"</SEGMENTS>";
	
	private String ORA_XML = "<SEGMENTS>" 
		+"<JAGROOT>"
		+"<RESULT>"
		+"<DOCSET TOTALHITS=\"1\">"
		//+"<sear:DOC>"
		+"<sear:DOC xmlns=\"http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib\" "
		+"xmlns:sear=\"http://www.exlibrisgroup.com/xsd/jaguar/search\">"
		+"<PrimoNMBib>"
		+"<record>"
		+"<control>"
		+"<sourcerecordid>debe641a-17ca-4196-ab2c-fe7565ced721</sourcerecordid>"
		+"<sourceid>ORA</sourceid>"
		+"<recordid>ORAdebe641a-17ca-4196-ab2c-fe7565ced721</recordid>"
		+"<originalsourceid>ORA</originalsourceid>"
		+"<sourceformat>DC</sourceformat>"
		+"<sourcesystem>Other</sourcesystem>"
		+"</control>"
		+"<display>"
		+"<type>other</type>"
		+"<title>"
		+"‘The times they are a-changing’ Dissemination services in an evolving scholarly landscape"
		+"</title>"
		+"<creator>Rumsey, Sally</creator>"
		+"<creationdate>2010</creationdate>"
		+"<format>Not Published</format>"
		+"<format>born digital</format>"
		+"<identifier>"
		+"Oxford Research Archive internal ID: ora:3462; ora:3462; urn:uuid:debe641a-17ca-4196-ab2c-fe7565ced721"
		+"</identifier>"
		+"<subject>"
		+"Library &amp; information science; Internet and science and learning; Scholarly dissemination; publishing; scholarly communication; ORA; Oxford University Research Archive"
		+"</subject>"
		+"<description>"
		+"Presentation given on 4 March 2010 as part of Bodleian Libraries and ORA seminar series ‘Scholarship, publishing and the dissemination of research’"
		+"</description>"
		+"<language>eng</language>"
		+"<source>ORA</source>"
		+"</display>"
		+"<links>"
		+"<backlink>$$Tora_backlink$$DThis item in ORA</backlink>"
		+"<linktorsrc>$$Tora_linktorsrc$$DShow Resource via ORA</linktorsrc>"
		+"<thumbnail>$$Tamazon_thumb</thumbnail>"
		+"<openurlfulltext>$$Topenurlfull_journal</openurlfulltext>"
		+"</links>"
		+"<search>"
		+"<creatorcontrib>Rumsey, Sally</creatorcontrib>"
		+"<title>"
		+"‘The times they are a-changing’ Dissemination services in an evolving scholarly landscape"
		+"</title>"
		+"<description>"
		+"Presentation given on 4 March 2010 as part of Bodleian Libraries and ORA seminar series ‘Scholarship, publishing and the dissemination of research’"
		+"</description>"
		+"<subject>Library &amp; information science</subject>"
		+"<subject>Internet and science and learning</subject>"
		+"<subject>Scholarly dissemination</subject>"
		+"<subject>publishing</subject>"
		+"<subject>scholarly communication</subject>"
		+"<subject>ORA</subject>"
		+"<subject>Oxford University Research Archive</subject>"
		+"<sourceid>ORA</sourceid>"
		+"<recordid>ORAdebe641a-17ca-4196-ab2c-fe7565ced721</recordid>"
		+"<rsrctype>other</rsrctype>"
		+"<creationdate>2010</creationdate>"
		+"<searchscope>DIG</searchscope>"
		+"<searchscope>OX</searchscope>"
		+"<scope>DIG</scope>"
		+"<scope>OX</scope>"
		+"</search>"
		+"<sort>"
		+"<creationdate>2010</creationdate>"
		+"</sort>"
		+"<facets>"
		+"<language>eng</language>"
		+"<creationdate>2010</creationdate>"
		+"<topic>Library &amp; information science</topic>"
		+"<topic>Internet and science and learning</topic>"
		+"<topic>Scholarly dissemination</topic>"
		+"<topic>publishing</topic>"
		+"<topic>scholarly communication</topic>"
		+"<topic>ORA</topic>"
		+"<topic>Oxford University Research Archive</topic>"
		+"<collection>ORA</collection>"
		+"<toplevel>online_resources</toplevel>"
		+"<rsrctype>other</rsrctype>"
		+"<creatorcontrib>Rumsey, Sally</creatorcontrib>"
		+"<format>Not Published</format>"
		+"<format>born digital</format>"
		+"</facets>"
		+"<delivery>"
		+"<institution>OX</institution>"
		+"<delcategory>Online Resource</delcategory>"
		+"</delivery>"
		+"<ranking>"
		+"<booster1>1</booster1>"
		+"<booster2>1</booster2>"
		+"</ranking>"
		+"<addata>"
		+"<au>Rumsey, Sally</au>"
		+"<btitle>"
		+"‘The times they are a-changing’ Dissemination services in an evolving scholarly landscape"
		+"</btitle>"
		+"<date>2010</date>"
		+"<risdate>2010</risdate>"
		+"<format>book</format>"
		+"<genre>unknown</genre>"
		+"<ristype>GEN</ristype>"
		+"</addata>"
		+"</record>"
		+"</PrimoNMBib>"
		+"<sear:GETIT GetIt2=\"http://oxfordsfx-direct.hosted.exlibrisgroup.com/oxford?ctx_ver=Z39.88-2004&amp;ctx_enc=info:ofi/enc:UTF-8&amp;ctx_tim=2010-12-20T15%3A32%3A13IST&amp;url_ver=Z39.88-2004&amp;url_ctx_fmt=infofi/fmt:kev:mtx:ctx&amp;rfr_id=info:sid/primo.exlibrisgroup.com:primo3-Journal-ORA&amp;rft_val_fmt=info:ofi/fmt:kev:mtx:book&amp;rft.genre=unknown&amp;rft.atitle=&amp;rft.jtitle=&amp;rft.btitle=‘The%20times%20they%20are%20a-changing’%20Dissemination%20services%20in%20an%20evolving%20scholarly%20landscape&amp;rft.aulast=&amp;rft.auinit=&amp;rft.auinit1=&amp;rft.auinitm=&amp;rft.ausuffix=&amp;rft.au=Rumsey,%20Sally&amp;rft.aucorp=&amp;rft.volume=&amp;rft.issue=&amp;rft.part=&amp;rft.quarter=&amp;rft.ssn=&amp;rft.spage=&amp;rft.epage=&amp;rft.pages=&amp;rft.artnum=&amp;rft.issn=&amp;rft.eissn=&amp;rft.isbn=&amp;rft.sici=&amp;rft.coden=&amp;rft_id=info:doi/&amp;rft.object_id=%20&amp;rft.eisbn=&amp;rft_dat=&lt;ORA&gt;debe641a-17ca-4196-ab2c-fe7565ced721&lt;/ORA&gt;&amp;rft_id=http%3A%2F%2Fsolo.bodleian.ox.ac.uk%2Fprimo_library%2Flibweb%2Faction%2Fdisplay.do%3Fdoc%3Ddebe641a-17ca-4196-ab2c-fe7565ced721%26vid%3DOXVU1%26fn%3Ddisplay%26displayMode%3Dfull\" GetIt1=\"http://1.1.1.1/objects/uuid:debe641a-17ca-4196-ab2c-fe7565ced721\" deliveryCategory=\"Online Resource\"/>"
		+"<sear:LINKS>"
		+"<sear:backlink>"
		+"http://ora.ouls.ox.ac.uk/objects/uuid:debe641a-17ca-4196-ab2c-fe7565ced721"
		+"</sear:backlink>"
		+"<sear:linktorsrc>"
		+"http://ora.ouls.ox.ac.uk/objects/uuid:debe641a-17ca-4196-ab2c-fe7565ced721"
		+"</sear:linktorsrc>"
		+"<sear:thumbnail>http://images.amazon.com/images/P/.01._SSTHUM_.jpg</sear:thumbnail>"
		+"<sear:openurlfulltext>"
		+"http://oxfordsfx-direct.hosted.exlibrisgroup.com/oxford?ctx_ver=Z39.88-2004&amp;ctx_enc=info:ofi/enc:UTF-8&amp;ctx_tim=2010-12-20T15%3A32%3A13IST&amp;url_ver=Z39.88-2004&amp;url_ctx_fmt=infofi/fmt:kev:mtx:ctx&amp;rfr_id=info:sid/primo.exlibrisgroup.com:primo3-Journal-ORA&amp;rft_val_fmt=info:ofi/fmt:kev:mtx:book&amp;rft.genre=unknown&amp;rft.atitle=&amp;rft.jtitle=&amp;rft.btitle=‘The%20times%20they%20are%20a-changing’%20Dissemination%20services%20in%20an%20evolving%20scholarly%20landscape&amp;rft.aulast=&amp;rft.auinit=&amp;rft.auinit1=&amp;rft.auinitm=&amp;rft.ausuffix=&amp;rft.au=Rumsey,%20Sally&amp;rft.aucorp=&amp;rft.volume=&amp;rft.issue=&amp;rft.part=&amp;rft.quarter=&amp;rft.ssn=&amp;rft.spage=&amp;rft.epage=&amp;rft.pages=&amp;rft.artnum=&amp;rft.issn=&amp;rft.eissn=&amp;rft.isbn=&amp;rft.sici=&amp;rft.coden=&amp;rft_id=info:doi/&amp;rft.object_id=&amp;svc_val_fmt=info:ofi/fmt:kev:mtx:sch_svc&amp;svc.fulltext=yes%20&amp;rft.eisbn=&amp;rft_dat=&lt;ORA&gt;debe641a-17ca-4196-ab2c-fe7565ced721&lt;/ORA&gt;&amp;rft_id=http%3A%2F%2Fsolo.bodleian.ox.ac.uk%2Fprimo_library%2Flibweb%2Faction%2Fdisplay.do%3Fdoc%3Ddebe641a-17ca-4196-ab2c-fe7565ced721%26vid%3DOXVU1%26fn%3Ddisplay%26displayMode%3Dfull"
		+"</sear:openurlfulltext>"
		+"</sear:LINKS>"
		+"</sear:DOC>"
		+"</DOCSET>"
		+"</RESULT>"
		+"</JAGROOT>"
		+"</SEGMENTS>";
	
	private String errorXML = "<SEGMENTS xmlns=\"http://www.exlibrisgroup.com/xsd/jaguar/search\">"
		+"<JAGROOT>"
		+"<RESULT>"
		+"<ERROR MESSAGE=\"PrimoGetItWS Remote Search Key is missing or expired\" CODE=\"-6\"/>"
		+"</RESULT>"
		+"</JAGROOT>"
		+"</SEGMENTS>";

	private String ORA_JSON = "{\"version\":\"0.5\",\"schema\":\"http://ws.gbv.de/daia/\","
		+"\"timestamp\":\"2009-06-09T15:39:52.831+02:00\","
		+"\"institution\":{\"content\":\"University of Oxford\","
		+"\"href\":\"http://www.ox.ac.uk\"},"
		+"\"document\":[{\"id\":\"ORAdebe641a-17ca-4196-ab2c-fe7565ced721\","
		+"\"href\":\"\","
		+"\"item\":["
		+"{\"href\":\"http://ora.ouls.ox.ac.uk/objects/uuid:debe641a-17ca-4196-ab2c-fe7565ced721\"}]}]}";

	
	private String OLIS_JSON = "{\"version\":\"0.5\",\"schema\":\"http://ws.gbv.de/daia/\","
		+"\"timestamp\":\"2009-06-09T15:39:52.831+02:00\","
		+"\"institution\":{\"content\":\"University of Oxford\","
		+"\"href\":\"http://www.ox.ac.uk\"},"
		+"\"document\":[{\"id\":\"UkOxUUkOxUb15585873\","
		+"\"href\":\"http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=10108045\","
		+"\"item\":["
		+"{\"department\":{\"id\":\"BLL\",\"content\":\"Balliol College Library\"},"
		+"\"storage\":{\"content\":\"Main Libr\"}},"
		+"{\"department\":{\"id\":\"BLL\",\"content\":\"Balliol College Library\"},"
		+"\"storage\":{\"content\":\"Main Libr\"}}]}]}";
	
	/*
	private String OLIS_JSON = "{\"version\":\"0.5\",\"schema\":\"http://ws.gbv.de/daia/\","
		+"\"timestamp\":\"2009-06-09T15:39:52.831+02:00\","
		+"\"institution\":{\"content\":\"University of Oxford\","
		+"\"href\":\"http://www.ox.ac.uk\"},"
		+"\"document\":[{\"id\":\"UkOxUUkOxUb15585873\","
		+"\"href\":\"http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=15585873\","
		+"\"item\":["
		+"{\"department\":{\"id\":\"RSL\",\"content\":\"Radcliffe Science Library\"},"
		+"\"storage\":{\"content\":\"Level 2\"}},"
		+"{\"department\":{\"id\":\"STX\",\"content\":\"St Cross College Library\"},"
		+"\"storage\":{\"content\":\"Main Libr\"}}]}]}";
	*/
	protected void setUp() throws Exception {
		super.setUp();
		service = new PrimoService(WEBRESOURCE_URL);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	public void testGetOLISResource() {
		try {
			long l = System.currentTimeMillis();
			ResponseBean bean = service.getResource("UkOxUUkOxUb15585873");
			System.out.println("testGetResource("+(System.currentTimeMillis()-l)+")");
			Assert.assertNotNull(bean);
			
			JSONObject jsonData = bean.toJSON("2009-06-09T15:39:52.831+02:00");
			
			Assert.assertEquals(OLIS_JSON, jsonData.toString());
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
		
	}
	
	public void testGetORAResource() {
		try {
			long l = System.currentTimeMillis();
			ResponseBean bean = service.getResource("ORAdebe641a-17ca-4196-ab2c-fe7565ced721");
			System.out.println("testGetResource("+(System.currentTimeMillis()-l)+")");
			Assert.assertNotNull(bean);
			
			JSONObject jsonData = bean.toJSON("2009-06-09T15:39:52.831+02:00");
			
			Assert.assertEquals(ORA_JSON, jsonData.toString());
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}
	*/
	
	public void testFilterOLISResponse() {
		
		try {
			Collection<SearObject> beans =
				PrimoService.filterResponse(nameSpaceURI, OLIS_XML);
			Assert.assertEquals(2, beans.size());
			
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}
	
	public void testFilterORAResponse() {
		
		try {
			Collection<SearObject> beans =
				PrimoService.filterResponse(nameSpaceURI, ORA_XML);
			Assert.assertEquals(1, beans.size());
			
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}
	
	public void testParser() {
		
		String originalString = "<tag>This string is mine & yours <trouble &amp; strife></tag>"; 
		
		Pattern pattern = Pattern.compile("&(?!(amp|apos|quot|lt|gt);)"); 
		Matcher mat = pattern.matcher(originalString);  
		String result = mat.replaceAll("&amp;");
		System.out.println("parser ["+result+"]");
		Assert.assertEquals("<tag>This string is mine &amp; yours <trouble &amp; strife></tag>", result);
	}
	
	public void testFilterErrorResponse() {
		
		try {
			PrimoService.filterResponse(nameSpaceURI, errorXML);
			
			//Assert.fail("Exception expected");
			
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}
/*
	public void testOLISToJSON() {
		try {
			String id = "UkOxUUkOxUb15585873";
			ResponseBean responseBean = new ResponseBean(id);
		    Collection<SearObject> beans = PrimoService.filterResponse(nameSpaceURI, OLIS_XML);
			responseBean.addSearObjects(beans);
			
			Map<String, Object> jsonData = responseBean.toJSON("2009-06-09T15:39:52.831+02:00");
			ObjectMapper mapper = new ObjectMapper();
			//mapper.writeValue(new File("response.json"), jsonData);
			
			Assert.assertEquals(OLIS_JSON, mapper.writeValueAsString(jsonData));
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}
*/
	public void testORAoJSON() {
		try {
			String id = "ORAdebe641a-17ca-4196-ab2c-fe7565ced721";
			ResponseBean responseBean = new ResponseBean(id);
		    Collection<SearObject> beans = PrimoService.filterResponse(nameSpaceURI, ORA_XML);
			responseBean.addSearObjects(beans);
			
			JSONObject json = responseBean.toJSON("2009-06-09T15:39:52.831+02:00");
			Assert.assertEquals(ORA_JSON.length(), json.toString().length());
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}

}
