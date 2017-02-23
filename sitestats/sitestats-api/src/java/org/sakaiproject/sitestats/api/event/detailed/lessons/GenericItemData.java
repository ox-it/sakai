package org.sakaiproject.sitestats.api.event.detailed.lessons;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Catch-all generic item data object, should be eventually phased out once all item types are explicitly handled
 * @author bjones86
 */
public class GenericItemData implements ResolvedEventData
{
    // Member variables
    private final String title;
    private final PageData parentPage;  // OWLTODO: Java 8 Optional?

    public static final GenericItemData DELETED_ITEM = new GenericItemData( "", null );

    /**
     * Constructor
     * @param title
     * @param parentPage
     */
    public GenericItemData( String title, PageData parentPage )
    {
        this.title          = title;
        this.parentPage     = parentPage;
    }

    // Getters
    public String getTitle()             { return title; }
    public PageData getParentPage()      { return parentPage; }
}
