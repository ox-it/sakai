/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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

package org.etudes.ambrosia.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Container;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Fragment;
import org.etudes.ambrosia.api.FragmentDelegate;
import org.etudes.ambrosia.api.RenderListener;
import org.etudes.ambrosia.api.UiService;
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * UiContext implements Context.
 */
public class UiContext implements Context
{
	public class MultiMessages implements InternationalizedMessages
	{
		List<InternationalizedMessages> messages = new ArrayList<InternationalizedMessages>();

		public MultiMessages()
		{
		}

		/**
		 * {@inheritDoc}
		 */
		public void clear()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean containsKey(Object arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean containsValue(Object arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public Set entrySet()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object get(Object arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public String getFormattedMessage(String key, Object[] args)
		{
			for (InternationalizedMessages msgs : this.messages)
			{
				if (msgs.getString(key, null) != null)
				{
					return msgs.getFormattedMessage(key, args);
				}
			}

			return this.messages.get(this.messages.size() - 1).getFormattedMessage(key, args);
		}

		/**
		 * {@inheritDoc}
		 */
		public Locale getLocale()
		{
			return this.messages.get(this.messages.size() - 1).getLocale();
		}

		/**
		 * {@inheritDoc}
		 */
		public String getString(String key)
		{
			for (InternationalizedMessages msgs : this.messages)
			{
				if (msgs.getString(key, null) != null)
				{
					return msgs.getString(key);
				}
			}

			return this.messages.get(this.messages.size() - 1).getString(key);
		}

		/**
		 * {@inheritDoc}
		 */
		public String getString(String key, String dflt)
		{
			for (InternationalizedMessages msgs : this.messages)
			{
				if (msgs.getString(key, null) != null)
				{
					return msgs.getString(key, dflt);
				}
			}

			return this.messages.get(this.messages.size() - 1).getString(key, dflt);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEmpty()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public Set keySet()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Pop off a set of messages from the front of the list.
		 */
		public void pop()
		{
			this.messages.remove(0);
		}

		/**
		 * Push a new set of messages onto the front of the list.
		 * 
		 * @param msgs
		 *        The messages.
		 */
		public void push(InternationalizedMessages msgs)
		{
			this.messages.add(0, msgs);
		}

		/**
		 * {@inheritDoc}
		 */
		public Object put(Object arg0, Object arg1)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void putAll(Map arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object remove(Object arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public int size()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection values()
		{
			throw new UnsupportedOperationException();
		}
	}

	/** A print write to use to collect output rather than send it to the real writer. */
	protected PrintWriter collectingWriter = null;

	/** we might need to nest collecting calls . */
	protected Stack<PrintWriter> collectingWriterStack = new Stack<PrintWriter>();

	/** The stream collecting the bytes while collecting. */
	protected ByteArrayOutputStream collectionStream = null;

	/** we might need to nest collecting calls . */
	protected Stack<ByteArrayOutputStream> collectionStreamStack = new Stack<ByteArrayOutputStream>();

	/** The tool destination of the request. */
	protected String destination = null;

	protected String docsPath = null;

	/** Set of listeners for edit component render notices. */
	protected Set<RenderListener> editRenderListeners = new HashSet<RenderListener>();

	/** named objects and encoding references. */
	protected Map<String, String> encodings = new HashMap<String, String>();

	/** The list of form element ids for the focus path. */
	protected List<String> focusIds = new ArrayList<String>();

	/** The name of the form that wraps the entire interface. */
	protected String formName = null;

	/** unique in-context id. */
	protected int id = 0;

	/** Internationalized messages. */
	protected MultiMessages messages = new MultiMessages();

	/** named objects and values. */
	protected Map<String, Object> objects = new HashMap<String, Object>();

	/** If the post was expected or not. */
	protected boolean postExpected = false;

	/** The "current" destination when this request came in (i.e. where we just were). */
	protected String previousDestination = null;

	/** Registrations made by components. */
	protected Map<String, String> registrations = new HashMap<String, String>();

	/** Collect various javascript. */
	protected StringBuffer scriptCode = new StringBuffer();

	/** The stream collecting the bytes behind the secondaryWriter. */
	protected ByteArrayOutputStream secondaryStream = null;

	/** A print write to use to collect a secondary output stream. */
	protected PrintWriter secondaryWriter = null;

	/** The UiService. */
	protected UiService service = null;

	/** The top component in the interface being rendered. */
	protected Component ui = null;

	/** Place to collect validation javascript code. */
	protected StringBuffer validationCode = new StringBuffer();

	/** The writer on the response output stream. */
	protected PrintWriter writer = null;

	/**
	 * Construct.
	 * 
	 * @param service
	 *        The UiService.
	 */
	public UiContext(UiService service)
	{
		this.service = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addEditComponentRenderListener(RenderListener listener)
	{
		this.editRenderListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addFocusId(String id)
	{
		this.focusIds.add(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMessages(InternationalizedMessages msgs)
	{
		if (msgs == null) return;
		this.messages.push(msgs);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addScript(String code)
	{
		this.scriptCode.append(code);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addValidation(String validation)
	{
		this.validationCode.append(validation);
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear()
	{
		objects.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearGlobalFragment(String id, String toolId)
	{
		FragmentDelegate delegate = this.service.getFragmentDelegate(id, toolId);
		if (delegate == null) return;

		// pop the fragment's messages onto the stack
		InternationalizedMessages msgs = delegate.getMessages();
		if (msgs != null) this.messages.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	public void editComponentRendered(String id)
	{
		for (RenderListener listener : this.editRenderListeners)
		{
			listener.componentRendered(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Component> findComponents(String id)
	{
		if (this.ui == null) return new ArrayList<Component>();

		if (this.ui instanceof Container) return ((Container) this.ui).findComponents(id);

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(String name)
	{
		if (name == null) return null;

		return objects.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCollected()
	{
		if (this.collectionStream == null) return null;

		String rv = null;

		// close the stream
		try
		{
			// flush
			this.collectingWriter.flush();

			// read
			rv = this.collectionStream.toString();

			// close the writer
			this.collectingWriter.close();

			// close the stream
			this.collectionStream.close();
		}
		catch (IOException e)
		{
		}

		// clear out of collecting mode
		this.collectionStream = this.collectionStreamStack.empty() ? null : this.collectionStreamStack.pop();
		this.collectingWriter = this.collectingWriterStack.empty() ? null : this.collectingWriterStack.pop();

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDestination()
	{
		return this.destination;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDocsPath()
	{
		return this.docsPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEncoding(String name)
	{
		if (name == null) return null;

		return encodings.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getFocusIds()
	{
		return this.focusIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFormName()
	{
		return this.formName;
	}

	/**
	 * {@inheritDoc}
	 */
	public Fragment getGlobalFragment(String id, String toolId, Object focus)
	{
		FragmentDelegate delegate = this.service.getFragmentDelegate(id, toolId);
		if (delegate == null) return null;

		// push the fragment's messages onto the stack
		InternationalizedMessages msgs = delegate.getMessages();
		if (msgs != null) this.messages.push(msgs);

		return delegate.getFragment(this, focus);
	}

	/**
	 * {@inheritDoc}
	 */
	public InternationalizedMessages getMessages()
	{
		return messages;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getPostExpected()
	{
		return this.postExpected;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPreviousDestination()
	{
		return this.previousDestination;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRegistration(String componentId)
	{
		return registrations.get(componentId);
	}

	/**
	 * {@inheritDoc}
	 */
	public PrintWriter getResponseWriter()
	{
		if (this.collectingWriter != null) return this.collectingWriter;

		return this.writer;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getScript()
	{
		if (this.scriptCode.length() == 0) return null;

		return this.scriptCode.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSecondaryCollected()
	{
		if (this.secondaryStream == null) return "";

		String rv = "";

		// close the stream
		try
		{
			// flush
			this.secondaryWriter.flush();

			// read
			rv = this.secondaryStream.toString();

			// close the writer
			this.secondaryWriter.close();

			// close the stream
			this.secondaryStream.close();
		}
		catch (IOException e)
		{
		}

		// clear out of secondary collection mode
		this.secondaryStream = null;
		this.secondaryWriter = null;

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public PrintWriter getSecondaryResponseWriter()
	{
		if (this.secondaryWriter != null) return this.secondaryWriter;

		return getResponseWriter();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getUi()
	{
		return this.ui;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getUniqueId()
	{
		return id++;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrl(String url)
	{
		if (url == null) return null;

		if (url.startsWith("!"))
		{
			return get("sakai.server.url") + url.substring(1);
		}

		return get("sakai.return.url") + url;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValidation()
	{
		if (this.validationCode.length() == 0) return null;

		return this.validationCode.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public void popMessages()
	{
		this.messages.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(String name, Object value)
	{
		if (name == null) return;

		if (value == null)
		{
			objects.remove(name);
			encodings.remove(name);
		}
		else
		{
			objects.put(name, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(String name, Object value, String encoding)
	{
		if (name == null) return;

		if (value == null)
		{
			objects.remove(name);
			encodings.remove(name);
		}
		else
		{
			objects.put(name, value);
			encodings.put(name, encoding);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void register(String componentId, String value)
	{
		registrations.put(componentId, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(String name)
	{
		objects.remove(name);
		encodings.remove(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeEditComponentRenderListener(RenderListener listener)
	{
		this.editRenderListeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCollecting()
	{
		if (this.collectingWriter != null) this.collectingWriterStack.push(this.collectingWriter);
		if (this.collectionStream != null) this.collectionStreamStack.push(this.collectionStream);

		this.collectionStream = new ByteArrayOutputStream();
		this.collectingWriter = new PrintWriter(this.collectionStream);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDocsPath(String docsArea)
	{
		this.docsPath = docsArea;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFormName(String name)
	{
		this.formName = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPostExpected(boolean expected)
	{
		this.postExpected = expected;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPreviousDestination(String destination)
	{
		this.previousDestination = destination;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setResponseWriter(PrintWriter writer)
	{
		this.writer = writer;
	}

	public void setSecondaryCollecting()
	{
		if (this.secondaryStream == null)
		{
			this.secondaryStream = new ByteArrayOutputStream();
			this.secondaryWriter = new PrintWriter(this.secondaryStream);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUi(Component ui)
	{
		this.ui = ui;
	}
}
