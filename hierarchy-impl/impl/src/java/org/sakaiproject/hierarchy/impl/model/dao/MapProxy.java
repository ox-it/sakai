package org.sakaiproject.hierarchy.impl.model.dao;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class MapProxy implements Map
{
	protected abstract void load();
	protected Map target = null;
	protected Map original = null;
	public int size()
	{
		load();
		return target.size();
	}
	public boolean isEmpty()
	{
		load();
		return target.isEmpty();
	}
	public boolean containsKey(Object arg0)
	{
		load();
		return target.containsKey(arg0);
	}
	public boolean containsValue(Object arg0)
	{
		load();
		return target.containsValue(arg0);
	}
	public Object get(Object arg0)
	{
		load();
		return target.get(arg0);
	}
	public Object put(Object arg0, Object arg1)
	{
		load();
		return target.put(arg0,arg1);
	}
	public Object remove(Object arg0)
	{
		load();
		return target.remove(arg0);
	}
	public void putAll(Map arg0)
	{
		target.putAll(arg0);			
	}
	public void clear()
	{
		target.clear();
		
	}
	public Set keySet()
	{
		return target.keySet();
	}
	public Collection values()
	{
		return target.values();
	}
	public Set entrySet()
	{
		return target.entrySet();
	}

}
