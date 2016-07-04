package uk.ac.ox.oucs.vle;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import uk.ac.ox.oucs.vle.proxy.SakaiProxyImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by buckett on 14/07/15.
 */
public class SakaiProxyImplTest {

    private SakaiProxyImpl impl;
    private ServerConfigurationService serverConfigurationService;

    @Before
    public void setUp() {
        impl = new SakaiProxyImpl();
        serverConfigurationService = mock(ServerConfigurationService.class);
        impl.setServerConfigurationService(serverConfigurationService);
    }
    @Test
    public void testAES16() {
        // You have to have 16
        when(serverConfigurationService.getString("aes.secret.key", null)).thenReturn("1234567890abcdef");

        String encoded = impl.encode("test");
        assertNotEquals("encryption.failed", encoded);
        assertEquals("test", impl.uncode(encoded));

    }

    @Test
    public void testAES5() {
        // You have to have 32
        when(serverConfigurationService.getString("aes.secret.key", null)).thenReturn("12345");

        String encoded = impl.encode("test");
        assertEquals("encryption.failed", encoded);
    }
}
