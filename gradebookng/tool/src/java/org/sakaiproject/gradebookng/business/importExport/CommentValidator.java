package org.sakaiproject.gradebookng.business.importExport;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.gradebookng.business.model.ImportedCell;

import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedColumn.Type;
import org.sakaiproject.gradebookng.business.model.ImportedRow;

/**
 * Used to validate comments in an imported file. Comments are invalid if they
 * exceed 500 characters in length.
 * 
 * @author bjones86
 */
public class CommentValidator
{
    public static final int MAX_COMMENT_LENGTH = 500;
    private CommentValidationReport report;

    public CommentValidator() {}

    public CommentValidationReport validate( List<ImportedRow> rows, List<ImportedColumn> columns, boolean isContextAnonymous )
    {
        report = new CommentValidationReport();

        for( ImportedColumn column : columns )
        {
            if( column.getType() == Type.COMMENTS )
            {
                String columnTitle = column.getColumnTitle();
                for( ImportedRow row : rows )
                {
                    ImportedCell cell = row.getCellMap().get( columnTitle );
                    if( cell != null )
                    {
                        String studentIdentifier = isContextAnonymous ? row.getAnonID() : row.getStudentEid();
                        validateComment( columnTitle, studentIdentifier, cell.getComment() );
                    }
                }
            }
        }

        return report;
    }

    /**
     * Validates the given comment. Comment must not exceed 500 characters.
     *
     * @param userID
     * @param comment
     */
    private void validateComment( String columnTitle, String userID, String comment )
    {
        if( StringUtils.length( comment ) > MAX_COMMENT_LENGTH )
        {
            report.addInvalidComment( columnTitle, userID, comment );
        }
    }
}
