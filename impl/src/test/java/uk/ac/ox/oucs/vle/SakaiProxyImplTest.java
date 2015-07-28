package uk.ac.ox.oucs.vle;

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

    @Test
    public void testAES() {

        SakaiProxyImpl impl = new SakaiProxyImpl();
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        impl.setServerConfigurationService(serverConfigurationService);

        // You have to have 16
        when(serverConfigurationService.getString("aes.secret.key", null)).thenReturn("1234567890abcdef");

        String encoded = impl.encode("test");
        assertNotEquals("encryption.failed", encoded);
        assertEquals("test", impl.uncode(encoded));

    }
}
