package org.sakaiproject.search.response.filter;

import org.sakaiproject.search.api.SearchResult;

/**
 * Filter for search results, allowing to massage the returned result.
 *
 * @author Colin Hebert
 */
public interface SearchItemFilter {
    /**
     * Filters a result.
     *
     * @param result original result.
     * @return massaged result.
     */
    SearchResult filter(SearchResult result);
}
