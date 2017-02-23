package org.sakaiproject.sitestats.tool.wicket.providers.infinite;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;

/**
 * @author plukasew
 */
public interface SortableInfiniteDataProvider<T, S> extends InfiniteDataProvider<T>, ISortStateLocator<S>
{

}
