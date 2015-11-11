package uk.ac.ox.oucs.vle;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertTrue;

/**
 * Check that we're setting a good character set on our responses. All the REST services shouldn't be cached by
 * the client as they may have changed.
 */
public class CacheResponseFilterTest extends ResourceTest {

    @Test
    public void testCacheHeader() {
        // Needs to be a URL that generates a response
        Response response = target("/signup/my").request("application/json").get();
        String header = response.getHeaderString("Cache-Control");
        assertTrue(header.contains("no-cache"));
    }
}

