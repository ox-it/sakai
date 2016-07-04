package uk.ac.ox.oucs.vle;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static uk.ac.ox.oucs.vle.CourseSignupService.Status.*;

public class StatusProgressionTest {

    private StatusProgression progression;

    @Before
    public void setUp() {
        progression = new StatusProgressionImpl();
    }

    @Test
    public void testNormalProgression() {
        assertTrue(progression.next(PENDING).contains(ACCEPTED));
        assertTrue(progression.next(ACCEPTED).contains(APPROVED));
        assertTrue(progression.next(APPROVED).contains(CONFIRMED));
    }

    @Test
    public void testWithdrawing() {
        assertTrue(progression.next(PENDING).contains(WITHDRAWN));
    }

}
