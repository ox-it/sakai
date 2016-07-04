package uk.ac.ox.oucs.termdates;

import java.util.Date;
import java.util.Map;

/**
 * Service to convert term week names and numbers into actual dates and vice-versa
 *
 * @author Colin Hebert
 */
public interface TermConverterService {
    /**
     * Get week name for today's week
     *
     * @return week's name
     */
    String getWeekName();

    /**
     * Get week name for a specific day's week
     *
     * @param date day in the said week
     * @return week's name
     */
    String getWeekName(Date date);

    /**
     * Get week name for a week number
     *
     * @param termWeekNumber Week number, as a term week number not an iso week number.
     * @return week's name
     */
    String getWeekName(int termWeekNumber);


    /**
     * Get the term week's number
     * <p>
     * A term week number is defined as the number of weeks since the start of the academic year, not the ISO week number
     * </p>
     *
     * @return term week's number
     */
    int getTermWeekNumber();

    /**
     * Get a term week's number based on a specific date
     * <p>
     * A term week number is defined as the number of weeks since the start of the academic year, not the ISO week number
     * </p>
     *
     * @param date day in the said week
     * @return term week's number
     */
    int getTermWeekNumber(Date date);

    /**
     * Get the term week's number given a week name
     * <p>
     * A term week number is defined as the number of weeks since the start of the academic year, not the ISO week number
     * </p>
     *
     * @param weekName week's name
     * @return term week's number
     */
    int getTermWeekNumber(String weekName);


    /**
     * Get start date for a week name in the current academic year
     *
     * @param weekName week's name
     * @return first day of the selected week in the current year
     */
    Date getWeekStartDate(String weekName);

    /**
     * Get start date for a week name in a specific academic year
     *
     * @param weekName        week's name
     * @param referentialDate any day in the academic year
     * @return first day of the selected week in the specified year
     */
    Date getWeekStartDate(String weekName, Date referentialDate);

    /**
     * Get start date for a week number in the current academic year
     *
     * @param termWeekNumber week's number
     * @return first day of the selected week in the current year
     */
    Date getWeekStartDate(int termWeekNumber);

    /**
     * Get start date for a week number in a specific academic year
     *
     * @param termWeekNumber  week's number
     * @param referentialDate any day in the academic year
     * @return first day of the selected week in the specified year
     */
    Date getWeekStartDate(int termWeekNumber, Date referentialDate);


    /**
     * Get end date for a week name in the current academic year
     *
     * @param weekName week's name
     * @return last day of the selected week in the current year
     */
    Date getWeekEndDate(String weekName);

    /**
     * Get end date for a week name in a specific academic year
     *
     * @param weekName        week's name
     * @param referentialDate any day in the academic year
     * @return last day of the selected week in the specified year
     */
    Date getWeekEndDate(String weekName, Date referentialDate);

    /**
     * Get end date for a week number in the current academic year
     *
     * @param termWeekNumber week's number
     * @return last day of the selected week in the current year
     */
    Date getWeekEndDate(int termWeekNumber);

    /**
     * Get end date for a week number in a specific academic year
     *
     * @param termWeekNumber  week's number
     * @param referentialDate any day in the academic year
     * @return last day of the selected week in the specified year
     */
    Date getWeekEndDate(int termWeekNumber, Date referentialDate);

    /**
     * Get a map matching term week numbers to week names
     *
     * @return week names
     */
    Map<Integer, String> getWeekNames();
}
