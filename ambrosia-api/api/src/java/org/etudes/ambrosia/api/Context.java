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

package org.etudes.ambrosia.api;

import java.io.PrintWriter;
import java.util.List;

import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * Context contains the runtime specific information needed with the Component tree to work a response.<br />
 * This is a set of internationalized messages that the UI tree references, along with a map of named objects that the UI tree references.
 */
public interface Context
{
	/** The name of the Boolean value for fragment or not in the context objects. */
	static final String FRAGMENT = "sakai:fragment";

	/**
	 * Add another listener for edit component rendering.
	 * 
	 * @param listener
	 *        The listener.
	 */
	void addEditComponentRenderListener(RenderListener listener);

	/**
	 * Add a form element id to the focus path (for on-load focus)
	 * 
	 * @param id
	 *        The form element id.
	 */
	void addFocusId(String id);

	/**
	 * Add another internationalized messages (this goes in front of any already defined).
	 * 
	 * @param msgs
	 *        The internationalized messages.
	 */
	void addMessages(InternationalizedMessages msgs);

	/**
	 * Add some javascript code.
	 * 
	 * @param validation
	 *        Some javascript code.
	 */
	void addScript(String code);

	/**
	 * Add some javascript code for an on-submit validation for the interface.
	 * 
	 * @param validation
	 *        The javascript validation code fragment.
	 */
	void addValidation(String validation);

	/**
	 * Clear the objects from the context.
	 */
	void clear();

	/**
	 * Clear the messges that were pushed when getGlobalFragment was called.
	 * 
	 * @param id
	 *        The fragment delegate id.
	 * @param toolId
	 *        The tool id.
	 */
	void clearGlobalFragment(String id, String toolId);

	/**
	 * Register that an edit component with this id has just been rendered.
	 * 
	 * @param id
	 *        The edit component's render id.
	 */
	void editComponentRendered(String id);

	/**
	 * Find the components in the interface being rendered with this id.
	 * 
	 * @param id
	 *        The id to search for.
	 * @return The components in the interface that has this id, or an empty list if not found.
	 */
	List<Component> findComponents(String id);

	/**
	 * Access the named object's value in the context.
	 * 
	 * @param name
	 *        The object name.
	 * @return The named object's value in the context, or null if missing.
	 */
	Object get(String name);

	/**
	 * Get any text collected while in collecting mode.
	 * 
	 * @return Any text collected while in collecting mode.
	 */
	String getCollected();

	/**
	 * Access the tool destination of the current request.
	 * 
	 * @return The tool destination of the current request.
	 */
	String getDestination();

	/**
	 * Access the content hosting root for documents available for embedding in component displays.
	 * 
	 * @return The content hosting root for documents available for embedding in component displays.
	 */
	String getDocsPath();

	/**
	 * Access the named object's encoding in the context.
	 * 
	 * @param name
	 *        The object name.
	 * @return The named object's encoding in the context, or null if missing.
	 */
	String getEncoding(String name);

	/**
	 * Access the collected focus ids.
	 * 
	 * @return The list of focus ids, or an empty list if there are none.
	 */
	List<String> getFocusIds();

	/**
	 * Access the name of the form that wraps the entire interface.
	 * 
	 * @return The interface's form name.
	 */
	String getFormName();

	/**
	 * Find a global fragment with these ids.
	 * 
	 * @param id
	 *        The fragment delegate id.
	 * @param toolId
	 *        The tool id.
	 * @param focus
	 *        The current focus object.
	 * @return The global fragment with these ids, or null if not found.
	 */
	Fragment getGlobalFragment(String id, String toolId, Object focus);

	/**
	 * Access the internationalized messages.
	 * 
	 * @return The internationalized messages.
	 */
	InternationalizedMessages getMessages();

	/**
	 * Check if the post was expected or not.
	 * 
	 * @return true if the post was expected, or false if not (or if this was not a post).
	 */
	boolean getPostExpected();

	/**
	 * Acces the tool destination that we were in just before the current request.
	 * 
	 * @return The tool destination that we were in just before the current request.
	 */
	String getPreviousDestination();

	/**
	 * Find the registered value for this component id.
	 * 
	 * @param componentId
	 *        The component id.
	 * @return The registered value, or null if nothing has been registered.
	 */
	String getRegistration(String componentId);

	/**
	 * Access the writer over the response stream.
	 * 
	 * @return The writer over the response stream.
	 */
	PrintWriter getResponseWriter();

	/**
	 * Access the javascript code collected for the interface.
	 * 
	 * @return The javascript code collected for the interface, or null if there was none.
	 */
	String getScript();

	/**
	 * Get any text collected on the secondary stream. Cancels the secondary stream collection.
	 * 
	 * @return The text, or an empty string if nothing was collected.
	 */
	String getSecondaryCollected();

	/**
	 * Access the secondary output writer.
	 * 
	 * @return The secondary output writer if defined, otherwise the primary.
	 */
	PrintWriter getSecondaryResponseWriter();

	/**
	 * Get the top component of the interface being rendered.
	 * 
	 * @return The top component of the interface being rendered.
	 */
	Component getUi();

	/**
	 * Get a number to use in making a unique (in-context) id.
	 * 
	 * @return The unique id number.
	 */
	int getUniqueId();

	/**
	 * Format a full url for this path. If the path begins with "!", it is server relative, else application relative.
	 * 
	 * @param url
	 *        The url path.
	 * @return The full URL.
	 */
	String getUrl(String url);

	/**
	 * Access the javascript code collected for on-submit validation for the interface.
	 * 
	 * @return The javascript code collected for on-submit validation for the interface, or null if there was none.
	 */
	String getValidation();

	/**
	 * Remove the most recently added messages from the stack.
	 */
	void popMessages();

	/**
	 * Add an object to context.
	 * 
	 * @param name
	 *        The object name.
	 * @param value
	 *        The object value.
	 */
	void put(String name, Object value);

	/**
	 * Add an object to context with a value and an encoding.
	 * 
	 * @param name
	 *        The object name.
	 * @param value
	 *        The object value.
	 * @param encoding
	 *        The encode - decode value to use instead of the object name when encoding for later decode.
	 */
	void put(String name, Object value, String encoding);

	/**
	 * Register this value for this component id.
	 * 
	 * @param componentId
	 *        The component's id.
	 * @param value
	 *        The value.
	 */
	void register(String componentId, String value);

	/**
	 * Remove the named object from the context.
	 * 
	 * @param name
	 *        The name of the object to remove.
	 */
	void remove(String name);

	/**
	 * Remove thislistener for edit component rendering.
	 * 
	 * @param listener
	 *        The listener.
	 */
	void removeEditComponentRenderListener(RenderListener listener);

	/**
	 * Go into collecting mode : any text that would be sent out is instead collected.
	 */
	void setCollecting();

	/**
	 * Set the tool destination for the current request.
	 * 
	 * @param destination
	 *        The tool destination for the current request.
	 */
	void setDestination(String destination);

	/**
	 * Set the content hosting root for documents available for embedding in component displays.
	 * 
	 * @param docsArea
	 *        The content hosting root for documents available for embedding in component displays.
	 */
	void setDocsPath(String docsArea);

	/**
	 * Set the name for the form that wraps the entire interface.
	 * 
	 * @param name
	 *        The form name.
	 */
	void setFormName(String name);

	/**
	 * Set the post expected flag.
	 * 
	 * @param expected
	 *        The post expected flag.
	 */
	void setPostExpected(boolean expected);

	/**
	 * Set the previous tool destination.
	 * 
	 * @param destination
	 *        The the previous tool destination.
	 */
	void setPreviousDestination(String destination);

	/**
	 * Go into secondary collecting mode : any text that would be sent to the secondary writer is separately collected.
	 */
	void setSecondaryCollecting();

	/**
	 * Set the top component of the interface being rendered.
	 * 
	 * @param ui
	 *        The top component of the interface being rendered.
	 */
	void setUi(Component ui);
}
