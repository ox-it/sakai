package uk.ac.ox.oucs.termdates;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Weeks;
import org.junit.Test;
import uk.ac.ox.oucs.termdates.exception.WeekNameNotFoundException;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Colin Hebert
 */
public abstract class TermConverterServiceTest {
    public abstract TermConverterService getTermConverterService();

    @Test
    public void testCurrentWeekTermNumberIsActuallyCurrent() {
        DateTime dateTime = DateTime.now();
        int currentWeekTermNumber = getTermConverterService().getTermWeekNumber();
        int weekTermNumber = getTermConverterService().getTermWeekNumber(dateTime.toDate());
        assertEquals(weekTermNumber, currentWeekTermNumber);
    }

    @Test
    public void testCurrentWeekNameIsActuallyCurrent() {
        DateTime dateTime = DateTime.now();
        String currentWeekName = getTermConverterService().getWeekName();
        String weekName = getTermConverterService().getWeekName(dateTime.toDate());
        assertEquals(weekName, currentWeekName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekNameForWeekTermNumberWithLowerOutOfBounds() {
        getTermConverterService().getWeekName(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekNameForWeekTermNumberWithUpperOutOfBounds() {
        getTermConverterService().getWeekName(54);
    }

    @Test(expected = WeekNameNotFoundException.class)
    public void testWeekTermNumberForNameWithInvalidName() {
        getTermConverterService().getTermWeekNumber("Test string that won't be used as a week name...");
    }

    @Test
    public void testWeekTermNumberAndWeekNamesAreSymetric() {
        for (int i = 0; i <= 52; i++) {
            String weekName = getTermConverterService().getWeekName(i);
            assertEquals(i, getTermConverterService().getTermWeekNumber(weekName));
        }
    }

    @Test(expected = WeekNameNotFoundException.class)
    public void testStartOfWeekWithInvalidName() {
        getTermConverterService().getWeekStartDate("Test string that won't be used as a week name...");
    }

    @Test(expected = WeekNameNotFoundException.class)
    public void testEndOfWeekWithInvalidName() {
        getTermConverterService().getWeekEndDate("Test string that won't be used as a week name...");
    }

    @Test
    public void testStartOfWeekIsSameWithNameAndNumber() {
        String weekName = getTermConverterService().getWeekName(12);
        Date startDateFromName = getTermConverterService().getWeekStartDate(weekName);
        Date startDateFromNumber = getTermConverterService().getWeekStartDate(12);
        assertEquals(startDateFromName, startDateFromNumber);
    }

    @Test
    public void testEndOfWeekIsSameWithNameAndNumber() {
        String weekName = getTermConverterService().getWeekName(37);
        Date endDateFromName = getTermConverterService().getWeekEndDate(weekName);
        Date endDateFromNumber = getTermConverterService().getWeekEndDate(37);
        assertEquals(endDateFromName, endDateFromNumber);
    }

    @Test
    public void testTimeBetweenStartAndEndOfWeekIsOneWeekAtTheEndOfTheYear() {
        DateTime thirtyDecemberTwentyEleven = new DateTime(2011, 12, 30, 0, 0);
        int weekNumber = getTermConverterService().getTermWeekNumber(thirtyDecemberTwentyEleven.toDate());
        Date startDate = getTermConverterService().getWeekStartDate(weekNumber);
        Date endDate = getTermConverterService().getWeekEndDate(weekNumber);

        Period period = new Period(new DateTime(startDate), new DateTime(endDate).plusDays(1));

        assertEquals(Weeks.ONE, period.toStandardWeeks());
    }

    @Test
    public void testTimeBetweenStartAndEndOfWeekIsOneWeekDuringLeapYear() {
        DateTime firstMarchTwentyTwelve = new DateTime(2012, 3, 1, 0, 0);
        int weekNumber = getTermConverterService().getTermWeekNumber(firstMarchTwentyTwelve.toDate());
        Date startDate = getTermConverterService().getWeekStartDate(weekNumber);
        Date endDate = getTermConverterService().getWeekEndDate(weekNumber);

        Period period = new Period(new DateTime(startDate), new DateTime(endDate).plusDays(1));

        assertEquals(Weeks.ONE, period.toStandardWeeks());
    }
}
