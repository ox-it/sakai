package org.sakaiproject.gradebookng.business.importExport;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import lombok.Getter;

/**
 * Contains the data related to heading validation (duplicated headings, invalid headings, empty headings).
 * 
 * @author plukasew, bjones86
 */
public class ImportHeadingValidationReport
{
    @Getter
    private final SortedSet<String> duplicateHeadings;

    @Getter
    private final SortedSet<String> invalidHeadings;

    @Getter
    private int blankHeaderTitleCount;

    public ImportHeadingValidationReport()
    {
        duplicateHeadings = new ConcurrentSkipListSet<>();
        invalidHeadings = new ConcurrentSkipListSet<>();
        blankHeaderTitleCount = 0;
    }

    public void addDuplicateHeading( String heading )
    {
        duplicateHeadings.add( heading );
    }

    public void addInvalidHeading( String heading )
    {
        invalidHeadings.add( heading );
    }

    public void incrementBlankHeaderTitleCount()
    {
        ++blankHeaderTitleCount;
    }
}
