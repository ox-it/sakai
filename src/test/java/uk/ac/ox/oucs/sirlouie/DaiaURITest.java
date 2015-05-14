package uk.ac.ox.oucs.sirlouie;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import uk.ac.ox.oucs.sirlouie.utils.DaiaURI;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class DaiaURITest {

    // The old URLs which had a doc as the key
    String originalUrl = "http%3A%2F%2Fsolo.bodleian.ox.ac.uk%2Fprimo_library%2Flibweb%2Faction%2Fdisplay.do" +
            "%3Fdoc%3DUkOxUb15244849%26vid%3DOXVU1%26fn%3Ddisplay%26displayMode%3Dfull";

    // The new URLs which use docId as the key
    String newUrl = "http%3A%2F%2Fsolo.bodleian.ox.ac.uk%2Fprimo_library%2Flibweb%2Faction%2FdlDisplay.do" +
            "%3FdocId%3Doxfaleph000443284%26vid%3DOXVU1%26displayMode%3Dfull";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDaiaURI() throws UnsupportedEncodingException, URISyntaxException {
        DaiaURI daia = new DaiaURI(originalUrl);
        assertEquals("UkOxUb15244849", daia.getDoc());
    }

    @Test
    public void testNewUrl() throws UnsupportedEncodingException, URISyntaxException {
        DaiaURI daia = new DaiaURI(newUrl);
        assertEquals("oxfaleph016689853", daia.getDoc());
    }

}
