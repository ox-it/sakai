package org.sakaiproject.feedback.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@Ignore("This requires the network access to the Google Recaptcha service.")
@RunWith(MockitoJUnitRunner.class)
public class TestRecaptchaServiceImpl {

    private RecaptchaServiceImpl impl;

    @Mock
    private ServerConfigurationService config;

    @Before
    public void setUp() {
        impl = new RecaptchaServiceImpl();
        impl.setServerConfigurationService(config);
    }

    @Test
    public void testWithTestKey() {
        // This is the test key that always passes.
        when(config.getString(RecaptchaServiceImpl.PRIVATE_KEY)).thenReturn("6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe");

        assertTrue(impl.verify("testing"));
    }

    @Test
    public void testWithBadKey() {
        when(config.getString(RecaptchaServiceImpl.PRIVATE_KEY)).thenReturn("this-does-not-work");
        assertFalse(impl.verify("testing"));
    }
}
