package uk.ac.ox.oucs.sirlouie;

import java.net.URI;
import java.net.URLEncoder;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import uk.ac.ox.oucs.sirlouie.utils.OpenURI;

public class OpenURITest extends TestCase {
	
	String uri = "http://oxfordsfx-direct.hosted.exlibrisgroup.com/oxford?ctx_ver=Z39.88-2004&"
		+ "ctx_enc=info:ofi/enc:UTF-8&ctx_tim=2010-12-17T17%3A09%3A33IST&url_ver=Z39.88-2004&"
		+ "url_ctx_fmt=infofi/fmt:kev:mtx:ctx&"
		+ "rfr_id=info:sid/primo.exlibrisgroup.com:primo3-Article-elsevier&"
		+ "rft_val_fmt=info:ofi/fmt:kev:mtx:&rft.genre=article&"
		+ "rft.atitle=Chemical%20changes%20that%20predispose%20smoked%20Cheddar%20cheese%20to%20calcium%20lactate%20crystallization&"
		+ "rft.jtitle=Journal%20of%20Dairy%20Science&"
		+ "rft.btitle=&"
		+ "rft.aulast=Rajbhandari&"
		+ "rft.auinit=&"
		+ "rft.auinit1=&rft.auinitm=&rft.ausuffix=&rft.au=Rajbhandari,%20P.&rft.aucorp=&rft.date=200908&"
		+ "rft.volume=92&rft.issue=8&rft.part=&rft.quarter=&rft.ssn=&rft.spage=3616&rft.epage=3622&"
		+ "rft.pages=3616-3622&rft.artnum=&rft.issn=00220302&rft.eissn=&rft.isbn=&rft.sici=&rft.coden=&"
		+ "rft_id=info:doi/10.3168/jds.2009-2157&rft.object_id=&"
		+ "rft_dat=%3Celsevier%3EOM09120A_00220302_00920008_09706824%3C/elsevier%3E&rft.eisbn=&"
		+ "rft_id=http%3A%2F%2Fsolo.bodleian.ox.ac.uk%2Fprimo_library%2Flibweb%2Faction%2Fdisplay.do%3Fdoc%3DTN_elsevierOM09120A_00220302_00920008_09706824%26vid%3DOXVU1%26fn%3Ddisplay%26displayMode%3Dfull";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void testOpenURI() {
		try {
			OpenURI open = new OpenURI(URLEncoder.encode(uri));
			
			System.out.println("scheme ["+open.getURI().getScheme()+"]");
			System.out.println("host   ["+open.getURI().getHost()+"]");
			System.out.println("path   ["+open.getURI().getPath()+"]");
			System.out.println("query  ["+open.getURI().getQuery()+"]");
			
			assertEquals(uri,open.getURI().toString());
		
		} catch (Exception e) {
			System.out.println("Exception caught ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Assert.fail("Exception caught ["+e.getLocalizedMessage()+"]");
		}
	}

}
