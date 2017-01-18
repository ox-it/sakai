package uk.ac.ox.it.shoal.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ox.it.shoal.logic.SakaiProxy;
import uk.ac.ox.it.shoal.logic.SakaiProxyImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MocksApplication.class, TestApplication.class } )
public class TestSpring {

    @Autowired
    private SakaiProxyImpl sakaiProxy;

    @Before
    public void setUp() {
    }

    /**
     * Check we are wiring spring beans correctly.
     */
    @Test
    public void testWired() {
        sakaiProxy.init();
    }

}
