package uk.ac.ox.it.shoal.components;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Testing of page number constraint.
 */
public class StatelessSimplePagingNavigatorTest {

    // Needed for any wicket testing
    private WicketTester wicketTester;

    private StatelessSimplePagingNavigator<Page> navigator;
    private PageParameters pageParameters;
    private IPageable pageable;

    @Before
    public void setUp() {
        wicketTester = new WicketTester();

        pageParameters = new PageParameters();
        pageable = Mockito.mock(IPageable.class);
        navigator = new StatelessSimplePagingNavigator<>("id", Page.class, pageParameters, pageable, 5);
    }

    @Test
    public void testConstrainNoPages() {
        Mockito.when(pageable.getPageCount()).thenReturn(0L);
        Assert.assertEquals(0, navigator.constrainPageNumber(0));
        Assert.assertEquals(0, navigator.constrainPageNumber(1));
        Assert.assertEquals(0, navigator.constrainPageNumber(-1));
    }

    @Test
    public void testConstrainOnePage() {
        Mockito.when(pageable.getPageCount()).thenReturn(1L);
        Assert.assertEquals(0, navigator.constrainPageNumber(0));
        Assert.assertEquals(0, navigator.constrainPageNumber(1));
        Assert.assertEquals(0, navigator.constrainPageNumber(-1));
    }

    @Test
    public void testConstrainTwoPages() {
        Mockito.when(pageable.getPageCount()).thenReturn(2L);
        Assert.assertEquals(0, navigator.constrainPageNumber(0));
        Assert.assertEquals(1, navigator.constrainPageNumber(1));
        Assert.assertEquals(1, navigator.constrainPageNumber(2));
        Assert.assertEquals(1, navigator.constrainPageNumber(-1));
    }

    @Test
    public void testConstrainLotsPages() {
        Mockito.when(pageable.getPageCount()).thenReturn(5L);
        Assert.assertEquals(0, navigator.constrainPageNumber(0));
        Assert.assertEquals(1, navigator.constrainPageNumber(1));
        Assert.assertEquals(2, navigator.constrainPageNumber(2));
        Assert.assertEquals(4, navigator.constrainPageNumber(9));
        Assert.assertEquals(4, navigator.constrainPageNumber(-1));
        Assert.assertEquals(0, navigator.constrainPageNumber(-9));
    }
}
