package org.sakaiproject.gradebookng.business.importExport;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;

/**
 * Used to validate grades in an imported file.
 *
 * OWLTODO: if letter grades are enabled in the future, more work will be necessary here
 * to perform the validation against the letter grade mapping scale.
 * 
 * @author plukasew, bjones86
 */
public class GradeValidator
{
    private GradeValidationReport report;

    public GradeValidator() {}

    /**
     * Validate the grades contained within the list of imported rows.
     *
     * @param rows the list of data parsed from the input file
     * @param isSourceDPC true if the input file was DPC, false otherwise
     * @return the {@link GradeValidationReport}
     */
    public GradeValidationReport validate( List<ImportedRow> rows, boolean isSourceDPC )
    {
        report = new GradeValidationReport();
        for( ImportedRow row : rows )
        {
            String userID;
            if( isSourceDPC )
            {
                userID = row.getStudentNumber();
            }
            else
            {
                userID = row.getStudentEid();
            }

            validateGrade( userID, row.getCellMap().get( ImportGradesHelper.DPC_DEFAULT_GRADE_ITEM_TITLE ).getScore() );
        }

        return report;
    }

    /**
     * Validates the given grade for the user.
     *
     * @param userID
     * @param grade
     */
    private void validateGrade( String userID, String grade )
    {
        // Empty grades are valid
        if( StringUtils.isBlank( grade ) )
        {
            return;
        }

        if( !isValidNumericGrade( grade ) )
        {
            report.addInvalidNumericGrade( userID, grade );
        }
    }

    /**
     * Determines if the given grade is a valid numeric grade.
     * 
     * @param grade
     * @return
     */
    private boolean isValidNumericGrade( String grade )
    {
        boolean valid = false;
        try
        {
            if( DataConverter.convertStringToDouble( grade ) >= 0 )
            {
                valid = true;
            }
        }
        catch( ParseException ex ) {}

        return valid;
    }
}
