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
    //private final SortedMap<String, String> invalidLetterGrades;

    @Getter
    private final SortedMap<String, String> invalidNumericGrades;

    public GradeValidationReport()
    {
        //invalidLetterGrades = new ConcurrentSkipListMap<>();
        invalidNumericGrades = new ConcurrentSkipListMap<>();
    }

    //public void addInvalidLetterGrade( String userEID, String grade )
    //{
    //    invalidLetterGrades.put( userEID, grade );
    //}

    public void addInvalidNumericGrade( String userEID, String grade )
    {
        invalidNumericGrades.put( userEID, grade );
    }
}
