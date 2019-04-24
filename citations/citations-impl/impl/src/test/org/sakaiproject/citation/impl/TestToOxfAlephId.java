package org.sakaiproject.citation.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestToOxfAlephId {

    @Test
    public void testToAlephIdGood() {
        String id = BaseCitationService.toOxfAlephId("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/dlDisplay.do?docId=oxfaleph016086099&vid=OXVU1&displayMode=full");
        assertEquals("016086099", id);
    }

    @Test
    public void testToAlephIdTrailing() {
        String id = BaseCitationService.toOxfAlephId("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/dlDisplay.do?docId=oxfaleph016086099");
        assertEquals("016086099", id);
    }

    @Test
    public void testToAlephIdNull() {
        String id = BaseCitationService.toOxfAlephId(null);
        assertEquals("", id);
    }

    @Test
    public void testToAlephIdNoId() {
        String id = BaseCitationService.toOxfAlephId("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/dlDisplay.do?");
        assertEquals("", id);
    }

    @Test
    public void testToAlephWrongType() {
        String id = BaseCitationService.toOxfAlephId(new Object());
        assertEquals("", id);
    }

    @Test
    public void testToAlephNoUrl() {
        String id = BaseCitationService.toOxfAlephId("1234567");
        assertEquals("", id);
    }

    @Test
    public void testToAlephIdEncoded() {
        String id = BaseCitationService.toOxfAlephId("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/dlDisplay.do?docId=oxfaleph1%202%203&");
        assertEquals("1 2 3", id);
    }

    @Test
    public void testToAlephIdDocNoId() {
        String id = BaseCitationService.toOxfAlephId("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=oxfaleph016086099&vid=OXVU1&fn=display&displayMode=full");
        assertEquals("016086099", id);
    }
}
