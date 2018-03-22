package org.sakaiproject.gradebookng.business.importExport;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;

import org.sakaiproject.gradebookng.business.model.ImportedCell;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedColumn.Type;
import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.util.FormattedText;

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
    private final GradebookNgBusinessService bus;

    public GradeValidator(GradebookNgBusinessService service)
    {
        bus = service;
    }

    /**
     * Validate the grades contained within the list of imported rows.
     *
     * @param rows the list of data parsed from the input file
     * @param columns the list of parsed columns, so we can access the column type for non-DPC validation
     * @param isSourceDPC true if the input file was DPC, false otherwise
     * @param isContextAnonymous true if input file uses GradingIDs to indentify students; ignored if isSourceDPC is true
     * @return the {@link GradeValidationReport}
     */
    public GradeValidationReport validate( List<ImportedRow> rows, List<ImportedColumn> columns, boolean isSourceDPC, boolean isContextAnonymous )
    {
        report = new GradeValidationReport();

        if( isSourceDPC )
        {
            for( ImportedRow row : rows )
            {
                String columnTitle = ImportGradesHelper.DPC_DEFAULT_GRADE_ITEM_TITLE;
                validateGrade( columnTitle, row.getStudentNumber(), row.getCellMap().get( columnTitle ).getScore() );
            }
        }
        else
        {
            for( ImportedColumn column : columns )
            {
                Type columnType = column.getType();
                String columnTitle = column.getColumnTitle();
                if( columnType == Type.GB_ITEM_WITH_POINTS || columnType == Type.GB_ITEM_WITHOUT_POINTS )
                {
                    for( ImportedRow row : rows )
                    {
                        ImportedCell cell = row.getCellMap().get( columnTitle );
                        if( cell != null )
                        {
                            String studentIdentifier = isContextAnonymous ? row.getAnonID() : row.getStudentEid();
                            validateGrade( columnTitle, studentIdentifier, cell.getScore() );
                        }
                    }
                }
            }
        }

        return report;
    }

    /**
     * Validates the given grade for the user. Grades are expected to be numeric.
     *
     * @param userID
     * @param grade
     */
    private void validateGrade( String columnTitle, String userID, String grade )
    {
        // Empty grades are valid
        if( StringUtils.isBlank( grade ) )
        {
            return;
        }

        // Convert back to user's locale for display/validation purposes
        String userDecimalSeparator = FormattedText.getDecimalSeparator();
        if( ",".equals( userDecimalSeparator ) )
        {
            grade = grade.replace( ".", userDecimalSeparator );
        }

        // OWLTODO: when/if letter grades are introduce, determine if grade is numeric
        // or alphabetical here and call/write the appropriate business service method.

        if( !bus.isValidNumericGrade( grade ) )
        {
            report.addInvalidNumericGrade( columnTitle, userID, grade );
        }
    }
}
