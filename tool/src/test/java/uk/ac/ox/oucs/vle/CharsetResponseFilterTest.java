package uk.ac.ox.oucs.vle;

import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertTrue;

/**
 * Check that we're setting a good character set on our responses.
 */
public class CharsetResponseFilterTest extends ResourceTest {

    @Test
    public void testCharsetHeader() {
        // Needs to be a URL that generates a response
        Response response = target("/signup/my").request("application/json").get();
        String header = response.getHeaderString("Content-Type");
        assertTrue(header.contains("charset=UTF-8"));
    }
}

