package uk.ac.ox.oucs.oxam.pages;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This interface is for looking up facet results to transform them to nicer values.
 * So for example we look a facet value of DCOS and transiform it into a nicer name.
 * 
 * @author buckett
 *
 * @param <T> The type of things that will be resolved.
 */
abstract class Resolver<T> {
	
	/**
	 * The resolver supports loading all the values early in one go and then caches them.
	 */
	protected Map<String, T> cached;

	public Map<String, T> load(List<String> values) {
		Map<String, T> latestPapers = lookup(values);
		if (cached == null) {
			cached = latestPapers;
		} else {
			cached.putAll(latestPapers);
		}
		return cached;
	}
	
	/**
	 * This looks up one values and returns it's display value.
	 * This is so that we can have a map of filters.
	 * @param value
	 * @return
	 */
	public String lookupDisplay(String value) {
		T paper = null;
		if (cached != null) {
			paper = cached.get(value);
		}
		if (paper == null) {
			load(Collections.singletonList(value));
		}
		paper = cached.get(value);
		return (paper != null)?display(paper):null;
	}
	
	/**
	 * Display one of the resolved values.
	 */
	abstract String display(T value);
	
	/**
	 * Resolve all the values, this is so we can load all the data we need in one go.
	 * @param values The values to lookup.
	 * @return
	 */
	abstract protected Map<String, T> lookup(List<String> values);
	
}