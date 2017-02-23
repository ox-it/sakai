package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Immutable class to hold parameters for sorting in detailed events queries
 *
 * @author bjones86
 */
public class SortingParams
{
    private final String sortProp;
    private final boolean asc;

    /**
     * Constructor requiring all parameters
     *
     * @param sortProp the property to sort on
     * @param asc sorting order, true = asc, false = desc
     */
    public SortingParams( String sortProp, boolean asc )
    {
        this.sortProp = sortProp;
        this.asc = asc;
    }

    public String getSortProp()
    {
        return this.sortProp;
    }

    public boolean isAsc()
    {
        return this.asc;
    }
}
