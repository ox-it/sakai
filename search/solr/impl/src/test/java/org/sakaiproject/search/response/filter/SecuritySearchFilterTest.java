package org.sakaiproject.search.response.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.producer.ContentProducerFactory;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Colin Hebert
 */
public class SecuritySearchFilterTest {
    private SecuritySearchFilter securitySearchFilter;
    @Mock
    private ContentProducerFactory contentProducerFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        securitySearchFilter = new SecuritySearchFilter();
        securitySearchFilter.setContentProducerFactory(contentProducerFactory);
    }

    @Test
    public void testResultNotCensored() {
        String reference = UUID.randomUUID().toString();
        SearchResult searchResult = mock(SearchResult.class);
        EntityContentProducer entityContentProducer = mock(EntityContentProducer.class);

        when(searchResult.getReference()).thenReturn(reference);
        when(entityContentProducer.canRead(reference)).thenReturn(false);
        when(contentProducerFactory.getContentProducerForElement(reference)).thenReturn(entityContentProducer);

        SearchResult actualResult = securitySearchFilter.filter(searchResult);

        assertNotEquals(searchResult, actualResult);
        assertTrue(actualResult.isCensored());
    }

    @Test
    public void testResultCensored() {
        String reference = UUID.randomUUID().toString();
        SearchResult searchResult = mock(SearchResult.class);
        EntityContentProducer entityContentProducer = mock(EntityContentProducer.class);

        when(searchResult.getReference()).thenReturn(reference);
        when(entityContentProducer.canRead(reference)).thenReturn(true);
        when(contentProducerFactory.getContentProducerForElement(reference)).thenReturn(entityContentProducer);

        SearchResult actualResult = securitySearchFilter.filter(searchResult);

        assertEquals(searchResult, actualResult);
        assertFalse(actualResult.isCensored());
    }
}
