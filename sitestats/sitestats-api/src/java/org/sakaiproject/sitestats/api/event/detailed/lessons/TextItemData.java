package org.sakaiproject.sitestats.api.event.detailed.lessons;

import java.util.Objects;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * @author bjones86
 */
public class TextItemData implements ResolvedEventData
{
    // Member variables
    private final String html;
    private final PageData parentPage;

    /**
     * Constructor
     * @param html
     * @param parentPage must not be null
     */
    public TextItemData( String html, PageData parentPage )
    {
        this.html           = html;
        this.parentPage     = Objects.requireNonNull( parentPage );
    }

    // Getters
    public String getHTML()             { return html; }
    public PageData getParentPage()     { return parentPage; }
}
