/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.DerivedCache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.util.StringUtil;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * <p>
 * A Cache of objects with keys with a limited lifespan, with special handling for extra information in the usage event reference that is not used for
 * the cache key.
 * </p>
 */
public class SubmissionCacheImpl implements Cache, Runnable, Observer
{
	/**
	 * The cache entry. Holds a time stamped payload.
	 */
	protected class CacheEntry
	{
		/** The time (seconds) to keep this cached (0 means don't exipre). */
		protected int m_duration = 0;

		/** currentTimeMillis when this expires. */
		protected long m_expires = 0;

		/** Hard reference to the payload, if needed. */
		protected Object m_hardPayload = null;

		/**
		 * Construct to cache the payload for the duration.
		 * 
		 * @param payload
		 *        The thing to cache.
		 * @param duration
		 *        The time (seconds) to keep this cached.
		 */
		public CacheEntry(Object payload, int duration)
		{
			m_hardPayload = payload;
			m_duration = duration;
			reset();
		}

		/**
		 * Access the duration.
		 * 
		 * @return The time (seconds) before the entry expires.
		 */
		public int getDuration()
		{
			return m_duration;
		}

		/**
		 * Access the hard payload directly.
		 * 
		 * @return The hard payload.
		 */
		public Object getHardPayload()
		{
			return m_hardPayload;
		}

		/**
		 * Get the cached object.
		 * 
		 * @param key
		 *        The key for this entry (if null, we won't try to refresh if missing)
		 * @return The cached object.
		 */
		public Object getPayload(Object key)
		{
			return m_hardPayload;
		}

		/**
		 * Check for expiration.
		 * 
		 * @return true if expired, false if still good.
		 */
		public boolean hasExpired()
		{
			return ((m_duration > 0) ? (System.currentTimeMillis() > m_expires) : false);
		}

		/**
		 * If we have a duration, reset our expiration time.
		 */
		public void reset()
		{
			if (m_duration > 0) m_expires = System.currentTimeMillis() + (m_duration * 1000);
		}
	}

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SubmissionCacheImpl.class);

	/** The string that separates the cache key prefix of the event reference from the extra information that may follow. */
	protected String m_cleaver = null;

	/** If true, we are disabled. */
	protected boolean m_disabled = false;

	/** Constructor injected event tracking service. */
	protected EventTrackingService m_eventTrackingService = null;

	/** Count of access requests. */
	protected long m_getCount = 0;

	/** Count of access requests satisfied with a cached entry. */
	protected long m_hitCount = 0;

	/** Map holding cached entries. */
	protected Map m_map = null;

	/** Constructor injected memory service. */
	protected MemoryService m_memoryService = null;

	/** Count of things put into the cache. */
	protected long m_putCount = 0;

	/** The number of seconds to sleep between expiration checks. */
	protected long m_refresherSleep = 60;

	/** The string that all resources in this cache will start with. */
	protected String m_resourcePattern = null;

	/** The thread which runs the expiration check. */
	protected Thread m_thread = null;

	/** My thread's quit flag. */
	protected boolean m_threadStop = false;

	/**
	 * Construct the Cache. Event scanning if pattern not null - will expire entries.
	 * 
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for expiration.
	 * @param cleaver
	 *        The string that separates the cache key prefix of each reference from the extra following information.
	 */
	public SubmissionCacheImpl(MemoryService memoryService, EventTrackingService eventTrackingService, long sleep, String pattern, String cleaver)
	{
		// inject our dependencies
		m_memoryService = memoryService;
		m_eventTrackingService = eventTrackingService;

		m_map = new ConcurrentReaderHashMap();

		// register as a cacher
		m_memoryService.registerCacher(this);

		m_refresherSleep = sleep;
		m_resourcePattern = pattern;
		m_cleaver = cleaver;

		// start the expiration thread
		start();

		// register to get events - first, before others
		if (pattern != null)
		{
			m_eventTrackingService.addPriorityObserver(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void attachDerivedCache(DerivedCache cache)
	{
	}

	/**
	 * Clear all entries.
	 */
	public void clear()
	{
		m_map.clear();
		m_getCount = 0;
		m_hitCount = 0;
		m_putCount = 0;
	}

	/**
	 * Test for a non expired entry in the cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a non-expired cache entry, false if not.
	 */
	public boolean containsKey(Object key)
	{
		if (disabled()) return false;

		m_getCount++;

		// is it there?
		CacheEntry entry = (CacheEntry) m_map.get(key);
		if (entry != null)
		{
			// has it expired?
			if (entry.hasExpired())
			{
				// if so, remove it
				remove(key);
				return false;
			}
			m_hitCount++;
			return true;
		}

		return false;
	}

	/**
	 * Test for an entry in the cache - expired or not.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a cache entry, false if not.
	 */
	public boolean containsKeyExpiredOrNot(Object key)
	{
		if (disabled()) return false;

		// is it there?
		boolean rv = m_map.containsKey(key);

		m_getCount++;
		if (rv)
		{
			m_hitCount++;
		}

		return rv;
	}

	/**
	 * Clean up.
	 */
	public void destroy()
	{
		clear();

		// if we are not in a global shutdown
		if (!ComponentManager.hasBeenClosed())
		{
			// remove my registration
			m_memoryService.unregisterCacher(this);

			// remove my event notification registration
			m_eventTrackingService.deleteObserver(this);
		}

		// stop our expiration thread (if any)
		stop();
	}

	/**
	 * Disable the cache.
	 */
	public void disable()
	{
		m_disabled = true;
		m_eventTrackingService.deleteObserver(this);
		clear();
	}

	/**
	 * Is the cache disabled?
	 * 
	 * @return true if the cache is disabled, false if it is enabled.
	 */
	public boolean disabled()
	{
		return m_disabled;
	}

	/**
	 * Enable the cache.
	 */
	public void enable()
	{
		m_disabled = false;

		if (m_resourcePattern != null)
		{
			m_eventTrackingService.addPriorityObserver(this);
		}
	}

	/**
	 * Expire this object.
	 * 
	 * @param key
	 *        The cache key.
	 */
	public void expire(Object key)
	{
		if (disabled()) return;

		// remove it
		remove(key);
	}

	/**
	 * Get the non expired entry, or null if not there (or expired)
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found, or the entry has expired (Note: use containsKey() to remove this
	 *         ambiguity).
	 */
	public Object get(Object key)
	{
		if (disabled()) return null;

		// get it if there
		CacheEntry entry = (CacheEntry) m_map.get(key);
		if (entry != null)
		{
			// has it expired?
			if (entry.hasExpired())
			{
				// if so, remove it
				remove(key);
				return null;
			}
			return entry.getPayload(key);
		}

		return null;
	}

	/**
	 * Get all the non-expired non-null entries.
	 * 
	 * @return all the non-expired non-null entries, or an empty list if none.
	 */
	public List getAll()
	{
		List rv = new Vector();

		if (disabled()) return rv;
		if (m_map.isEmpty()) return rv;

		// for each entry in the cache
		for (Iterator iKeys = m_map.entrySet().iterator(); iKeys.hasNext();)
		{
			Map.Entry e = (Map.Entry) iKeys.next();
			CacheEntry entry = (CacheEntry) e.getValue();

			// skip expired
			if (entry.hasExpired()) continue;

			Object payload = entry.getPayload(e.getKey());

			// skip nulls
			if (payload == null) continue;

			// we'll take it
			rv.add(payload);
		}

		return rv;
	}

	/**
	 * Get all the non-expired non-null entries that are in the specified reference path. Note: only works with String keys.
	 * 
	 * @param path
	 *        The reference path.
	 * @return all the non-expired non-null entries, or an empty list if none.
	 */
	public List getAll(String path)
	{
		List rv = new Vector();

		if (disabled()) return rv;
		if (m_map.isEmpty()) return rv;

		// for each entry in the cache
		for (Iterator iKeys = m_map.entrySet().iterator(); iKeys.hasNext();)
		{
			Map.Entry e = (Map.Entry) iKeys.next();
			CacheEntry entry = (CacheEntry) e.getValue();

			// skip expired
			if (entry.hasExpired()) continue;

			Object payload = entry.getPayload(e.getKey());

			// skip nulls
			if (payload == null) continue;

			// take only if keys start with path, and have no SEPARATOR following other than at the end %%%
			String keyPath = referencePath((String) e.getKey());
			if (!keyPath.equals(path)) continue;

			// we'll take it
			rv.add(payload);
		}

		return rv;
	}

	/**
	 * Return a description of the cacher.
	 * 
	 * @return The cacher's description.
	 */
	public String getDescription()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("MnemeSubmissionCache");
		if (m_disabled)
		{
			buf.append(" disabled");
		}
		if (m_resourcePattern != null)
		{
			buf.append(" " + m_resourcePattern);
		}
		if (m_thread != null)
		{
			buf.append(" thread_sleep: " + m_refresherSleep);
		}

		buf.append("  puts:" + m_putCount + "  gets:" + m_getCount + "  hits:" + m_hitCount + "  hit%:"
				+ ((m_getCount > 0) ? "" + ((100l * m_hitCount) / m_getCount) : "n/a"));

		return buf.toString();
	}

	/**
	 * Get the entry, or null if not there (expired entries are returned, too).
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found. (Note: use containsKey() to remove this ambiguity).
	 */
	public Object getExpiredOrNot(Object key)
	{
		if (disabled()) return null;

		// is it there?
		CacheEntry entry = (CacheEntry) m_map.get(key);
		if (entry != null)
		{
			return entry.getPayload(key);
		}

		return null;
	}

	/**
	 * Get all the keys, eache modified to remove the resourcePattern prefix. Note: only works with String keys.
	 * 
	 * @return The List of keys converted from references to ids (String).
	 */
	public List getIds()
	{
		List rv = new Vector();

		for (Iterator it = m_map.keySet().iterator(); it.hasNext();)
		{
			String key = (String) it.next();
			int i = key.indexOf(m_resourcePattern);
			if (i != -1) key = key.substring(i + m_resourcePattern.length());
			rv.add(key);
		}

		return rv;
	}

	/**
	 * Get all the keys
	 * 
	 * @return The List of key values (Object).
	 */
	public List getKeys()
	{
		List rv = new Vector();
		rv.addAll(m_map.keySet());
		return rv;
	}

	/**
	 * Return the size of the cacher - indicating how much memory in use.
	 * 
	 * @return The size of the cacher.
	 */
	public long getSize()
	{
		return m_map.size();
	}

	/**
	 * Set the cache to hold events for later processing to assure an atomic "complete" load.
	 */
	public void holdEvents()
	{
	}

	/**
	 * Are we complete?
	 * 
	 * @return true if we have all the possible entries cached, false if not.
	 */
	public boolean isComplete()
	{
		return false;
	}

	/**
	 * Are we complete for one level of the reference hierarchy?
	 * 
	 * @param path
	 *        The reference to the completion level.
	 * @return true if we have all the possible entries cached, false if not.
	 */
	public boolean isComplete(String path)
	{
		return false;
	}

	/**
	 * Restore normal event processing in the cache, and process any held events now.
	 */
	public void processEvents()
	{
	}

	/**
	 * Cache an object - don't automatically exipire it.
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 */
	public void put(Object key, Object payload)
	{
		put(key, payload, 0);
	}

	/*************************************************************************************************************************************************
	 * Cacher implementation
	 ************************************************************************************************************************************************/

	/**
	 * Cache an object
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 */
	public void put(Object key, Object payload, int duration)
	{
		if (disabled()) return;

		m_map.put(key, new CacheEntry(payload, duration));
		m_putCount++;
	}

	/**
	 * Remove this entry from the cache.
	 * 
	 * @param key
	 *        The cache key.
	 */
	public void remove(Object key)
	{
		if (disabled()) return;

		CacheEntry entry = (CacheEntry) m_map.remove(key);
	}

	/**
	 * Clear out as much as possible anything cached; re-sync any cache that is needed to be kept.
	 */
	public void resetCache()
	{
		clear();
	}

	/*************************************************************************************************************************************************
	 * Runnable implementation
	 ************************************************************************************************************************************************/

	/**
	 * Run the expiration thread.
	 */
	public void run()
	{
		// since we might be running while the component manager is still being created and populated, such as at server
		// startup, wait here for a complete component manager
		ComponentManager.waitTillConfigured();

		// loop till told to stop
		while ((!m_threadStop) && (!Thread.currentThread().isInterrupted()))
		{
			long startTime = 0;
			try
			{
				if (M_log.isDebugEnabled())
				{
					startTime = System.currentTimeMillis();
					M_log.debug(this + ".checking ...");
				}

				// collect keys of expired entries in the cache
				List expired = new Vector();
				for (Iterator iKeys = m_map.entrySet().iterator(); iKeys.hasNext();)
				{
					Map.Entry e = (Map.Entry) iKeys.next();
					String key = (String) e.getKey();
					CacheEntry entry = (CacheEntry) e.getValue();

					// if it has expired
					if (entry.hasExpired())
					{
						expired.add(key);
					}
				}

				// for each expired, remove
				for (Iterator iKeys = expired.iterator(); iKeys.hasNext();)
				{
					String key = (String) iKeys.next();
					remove(key);
				}
			}
			catch (Throwable e)
			{
				M_log.warn(this + ": exception: ", e);
			}

			if (M_log.isDebugEnabled())
			{
				M_log.debug(this + ".done. Time: " + (System.currentTimeMillis() - startTime));
			}

			// take a small nap
			try
			{
				Thread.sleep(m_refresherSleep * 1000);
			}
			catch (Exception ignore)
			{
			}
		}
	}

	/**
	 * Set the cache to be complete, containing all possible entries.
	 */
	public void setComplete()
	{
	}

	/**
	 * Set the cache to be complete for one level of the reference hierarchy.
	 * 
	 * @param path
	 *        The reference to the completion level.
	 */
	public void setComplete(String path)
	{
	}

	/**
	 * This method is called whenever the observed object is changed. An application calls an <tt>Observable</tt> object's
	 * <code>notifyObservers</code> method to have all the object's observers notified of the change. default implementation is to cause the courier
	 * service to deliver to the interface controlled by my controller. Extensions can override.
	 * 
	 * @param o
	 *        the observable object.
	 * @param arg
	 *        an argument passed to the <code>notifyObservers</code> method.
	 */
	public void update(Observable o, Object arg)
	{
		if (disabled()) return;

		// arg is Event
		if (!(arg instanceof Event)) return;
		Event event = (Event) arg;

		// if this is just a read, not a modify event, we can ignore it
		if (!event.getModify()) return;

		String ref = event.getResource();

		// if this resource is not in my pattern of resources, we can ignore it
		if (!ref.startsWith(m_resourcePattern)) return;

		if (M_log.isDebugEnabled()) M_log.debug(this + ".update() [" + m_resourcePattern + "] resource: " + ref + " event: " + event.getEvent());

		// separate out the cache key from the reference
		String key = ref;
		if (m_cleaver != null)
		{
			key = StringUtil.splitFirst(ref, m_cleaver)[0];
		}

		// do we have this in our cache?
		Object oldValue = get(key);
		if (m_map.containsKey(key))
		{
			// invalidate our copy
			remove(key);
		}
	}

	/**
	 * Compute the reference path (i.e. the container) for a given reference.
	 * 
	 * @param ref
	 *        The reference string.
	 * @return The reference root for the given reference.
	 */
	protected String referencePath(String ref)
	{
		String path = null;

		// Note: there may be a trailing separator
		int pos = ref.lastIndexOf("/", ref.length() - 2);

		// if no separators are found, place it even before the root!
		if (pos == -1)
		{
			path = "";
		}

		// use the string up to and including that last separator
		else
		{
			path = ref.substring(0, pos + 1);
		}

		return path;
	}

	/*************************************************************************************************************************************************
	 * Observer implementation
	 ************************************************************************************************************************************************/

	/**
	 * Start the expiration thread.
	 */
	protected void start()
	{
		m_threadStop = false;

		m_thread = new Thread(this, getClass().getName());
		m_thread.setDaemon(true);
		m_thread.setPriority(Thread.MIN_PRIORITY + 2);
		m_thread.start();

	} // start

	/**
	 * Stop the expiration thread.
	 */
	protected void stop()
	{
		if (m_thread == null) return;

		// signal the thread to stop
		m_threadStop = true;

		// wake up the thread
		m_thread.interrupt();

		m_thread = null;

	} // stop
}
