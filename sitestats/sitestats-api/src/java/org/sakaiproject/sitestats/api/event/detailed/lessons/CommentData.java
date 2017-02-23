package org.sakaiproject.sitestats.api.event.detailed.lessons;

import java.util.Date;
import java.util.Objects;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * @author bjones86
 */
public class CommentData implements ResolvedEventData
{
    // Member variables
    private final String    author;
    private final String    comment;
    private final PageData  parent;
    private final Date      timePosted;

    /**
     * Constructor
     * @param author
     * @param comment
     * @param parent
     * @param timePosted
     */
    public CommentData( String author, String comment, PageData parent, Date timePosted )
    {
        this.author         = author;
        this.comment        = comment;
        this.parent         = Objects.requireNonNull( parent );
        this.timePosted     = timePosted;
    }

    // Getters
    public String     getAuthor()         { return this.author; }
    public String     getComment()        { return this.comment; }
    public PageData   getParentPage()     { return this.parent; }
    public Date       getTimePosted()     { return this.timePosted; }
}
