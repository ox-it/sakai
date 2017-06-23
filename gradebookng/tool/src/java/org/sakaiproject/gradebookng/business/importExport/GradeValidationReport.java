package org.sakaiproject.gradebookng.business.importExport;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import lombok.Getter;

/**
 * Contains the data relevant to grade validation: invalid number and letter grades.
 * 
 * @author plukasew, bjones86
 */
public class GradeValidationReport
{
    //@Getter
    //private final SortedMap<String, SortedMap<String, String>> invalidLetterGrades;

    @Getter
    private final SortedMap<String, SortedMap<String, String>> invalidNumericGrades;

    public GradeValidationReport()
    {
        //invalidLetterGrades = new ConcurrentSkipListMap<>();
        invalidNumericGrades = new ConcurrentSkipListMap<>();
    }

    //public void addInvalidLetterGrade( String columnTitle, String userEID, String grade )
    //{
    //}

    public void addInvalidNumericGrade( String columnTitle, String userEID, String grade )
    {
        SortedMap<String, String> columnInvalidGradesMap = invalidNumericGrades.get( columnTitle );
        if( columnInvalidGradesMap == null )
        {
            columnInvalidGradesMap = new ConcurrentSkipListMap<>();
            columnInvalidGradesMap.put( userEID, grade );
            invalidNumericGrades.put( columnTitle, columnInvalidGradesMap );
        }
        else
        {
            columnInvalidGradesMap.put( userEID, grade );
        }
    }
}
