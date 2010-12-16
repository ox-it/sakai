package uk.ac.ox.oucs.sirlouie;

import java.util.Collection;

import uk.ac.ox.oucs.sirlouie.reply.SearObject;
import uk.ac.ox.oucs.sirlouie.response.ResponseBean;

import junit.framework.Assert;
import junit.framework.TestCase;

public class PrimoServiceTest extends TestCase {
	
	PrimoService service;
	
	private String nameSpaceURI = "http://www.exlibrisgroup.com/xsd/jaguar/search";
	
	private String WEBRESOURCE_URL = "http://primo-s-web-2.sers.ox.ac.uk:1701/PrimoWebServices/xservice/getit";
	
	private String XML = "<SEGMENTS xmlns=\"http://www.exlibrisgroup.com/xsd/jaguar/search\">"
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
	
	private String errorXML = "<SEGMENTS xmlns=\"http://www.exlibrisgroup.com/xsd/jaguar/search\">"
		+"<JAGROOT>"
		+"<RESULT>"
		+"<ERROR MESSAGE=\"PrimoGetItWS Remote Search Key is missing or expired\" CODE=\"-6\"/>"
		+"</RESULT>"
		+"</JAGROOT>"
		+"</SEGMENTS>";


	protected void setUp() throws Exception {
		super.setUp();
		service = new PrimoService(WEBRESOURCE_URL);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	public void testGetResource() {
		try {
			long l = System.currentTimeMillis();
			ResponseBean bean = service.getResource("UkOxUUkOxUb10108045");
			System.out.println("testGetResource("+(System.currentTimeMillis()-l)+")");
			Assert.assertNotNull(bean);
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
		
	}
	*/
	
	public void testFilterResponse() {
		
		try {
			Collection<SearObject> beans =
				PrimoService.filterResponse(nameSpaceURI, XML);
			Assert.assertEquals(2, beans.size());
			
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
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

}
