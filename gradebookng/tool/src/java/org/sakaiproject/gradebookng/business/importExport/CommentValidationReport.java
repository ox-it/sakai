package org.sakaiproject.gradebookng.business.importExport;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Getter;

/**
 * Contains the data relevant to comment validation: comments over 500 characters.
 *
 * @author bjones86
 */
public class CommentValidationReport
{
    @Getter
    private final SortedMap<String, SortedMap<String, String>> invalidComments;

    public CommentValidationReport()
    {
        invalidComments = new ConcurrentSkipListMap<>();
    }

    public void addInvalidComment( String columnTitle, String userIdentifier, String comment )
    {
        SortedMap<String, String> columnInvalidCommentsMap = invalidComments.get( columnTitle );
        if( columnInvalidCommentsMap == null )
        {
            columnInvalidCommentsMap = new ConcurrentSkipListMap<>();
            columnInvalidCommentsMap.put( userIdentifier, comment );
            invalidComments.put( columnTitle, columnInvalidCommentsMap );
        }
        else
        {
            columnInvalidCommentsMap.put( userIdentifier, comment );
        }
    }
}
