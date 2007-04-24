package org.sakaiproject.hierarchy.impl;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A lazy map that wraps all the containing map enties.
 * Doesn't track if it is already loaded.
 * @author buckett
 *
 */
public class WrappingMap extends AbstractMap {

	private Map originals;
	private Map wrappers;
	private Wrapper wrapper;
	
	public WrappingMap(Map originals, Wrapper wrapper) {
		this.originals = originals;
		this.wrappers = new HashMap();
		this.wrapper = wrapper;
	}
	
	public void clear() {
		originals.clear();
		wrappers.clear();
	}
	public boolean containsKey(Object key) {
		return originals.containsKey(key);
	}
	public boolean containsValue(Object value) {
		loadAll();
		return wrappers.containsValue(value);
	}
	public Set entrySet() {
		loadAll();
		return wrappers.entrySet();
	}
	public boolean equals(Object o) {
		loadAll();
		return wrappers.equals(o);
	}
	public Object get(Object key) {
		load(key);
		return wrappers.get(key);
	}
	public int hashCode() {
		return originals.hashCode();
	}
	public boolean isEmpty() {
		return originals.isEmpty();
	}
	public Set keySet() {
		return originals.keySet();
	}
	public Object put(Object key, Object value) {
		return originals.put(key, value);
	}
	public void putAll(Map t) {
		originals.putAll(t);
	}
	public Object remove(Object key) {
		load(key);
		Object object = wrappers.remove(key);
		originals.remove(key);
		return object;
	}
	public int size() {
		return originals.size();
	}
	public Collection values() {
		loadAll();
		return wrappers.values();
	}
	
	private void load(Object key) {
		Object value = originals.get(key);
		if (value != null) {
			value = wrapper.wrap(value);
			wrappers.put(key,value);
		}
	}
	
	private void loadAll() {
		Iterator it = originals.keySet().iterator();
		while (it.hasNext()) {
			load(it.next());
		}
	}
	
	/**
	 * Interface that knows how to wrap an object up.
	 * @author buckett
	 */
	public interface Wrapper {
		Object wrap(Object object);
	}
}
