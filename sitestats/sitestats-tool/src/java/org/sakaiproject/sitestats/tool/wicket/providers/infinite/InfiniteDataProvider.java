package org.sakaiproject.sitestats.tool.wicket.providers.infinite;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

/**
 * @author plukasew
 */
public interface InfiniteDataProvider<T> extends IDetachable
{
	PagedInfiniteIterator<? extends T> iterator(long first, long count);

	IModel<T> model(T object);
}
