package org.sakaiproject.gradebookng.business.importExport;

import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

/**
 * Utility for converting data to differing formats.
 * 
 * @author plukasew, bjones86
 */
public class DataConverter
{
    // DPC constants
    private static final String DOUBLE_QUOTE = "\"";
    public  static final String TAB_CHAR = "\t";

    private static final String SAK_PROP_CSV_DELIMITER = "csv.separator";
    private static final String SAK_PROP_CSV_DELIMITER_DEFAULT = ",";
    private static String csvDelimiter;

    private static NumberFormat numberFormat;

    /**
     * Get a {@link NumberFormat} object for the current context's locale.
     *
     * @return {@link NumberFormat}
     */
    public static NumberFormat getNumberFormat()
    {
        if( numberFormat == null )
        {
            numberFormat = NumberFormat.getInstance( new ResourceLoader().getLocale() );
        }

        return numberFormat;
    }

    /**
     * Get the defined CSV delimiter specified in sakai.properties
     * 
     * @return The configured CSV delimiter string
     */
    public static String getCsvDelimiter()
    {
        if( csvDelimiter == null )
        {
            csvDelimiter = ServerConfigurationService.getString( SAK_PROP_CSV_DELIMITER, SAK_PROP_CSV_DELIMITER_DEFAULT );
        }

        return csvDelimiter;
    }

    /**
     * Determines if the given string is "empty".
     *
     * @param line
     * @return
     */
    public static boolean isEmptyLine( String line )
    {
        String ignoreChars = getCsvDelimiter() + DOUBLE_QUOTE + TAB_CHAR;
        return StringUtils.trimToEmpty( line.replaceAll( "[" + ignoreChars + "]", "" ) ).isEmpty();
    }

    /**
     * Get the given string as a {@link Double} representation.
     * 
     * @param doubleAsString
     * @return
     * @throws ParseException
     */
    public static Double convertStringToDouble( String doubleAsString ) throws ParseException
    {
        Double scoreAsDouble = null;
        if( doubleAsString != null )
        {
            Number numericScore = getNumberFormat().parse( doubleAsString.trim() );
            scoreAsDouble = numericScore.doubleValue();
        }

        return scoreAsDouble;
    }
}
