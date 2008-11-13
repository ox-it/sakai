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

package org.etudes.ambrosia.api;

import org.apache.commons.fileupload.FileItem;

/**
 * UiPropertyReference accesses a particular selector of a model entity. If the reference is not set, we attempt to just get the object itself.<br />
 * Nested references are supported. For example, "foo.bar" means call the entity.getFoo(), and with the result of that, call getBar().<br />
 * Missing values can be set to return a specified message.
 */
public interface PropertyReference
{
	/**
	 * Add another property to combine into a formatted display.
	 * 
	 * @param property
	 *        The property to add.
	 * @return self.
	 */
	PropertyReference addProperty(PropertyReference property);

	/**
	 * Access the encoding reference.
	 * 
	 * @param context
	 *        The context.
	 * @param focus
	 *        The selected object.
	 * @param index
	 *        the index of the selected object in its collection.
	 */
	String getEncoding(Context context, Object focus, int index);

	/**
	 * Access both the entity and property references (entity . property) as a full reference.
	 * 
	 * @param context
	 *        The context.
	 * @return The full reference.
	 */
	String getFullReference(Context context);

	/**
	 * Access a type for the property reference.
	 * 
	 * @return The type.
	 */
	String getType();

	/**
	 * Get selector value of the entity in the context.
	 * 
	 * @param context
	 *        The UI contet, containing the available entities.
	 * @param focus
	 *        Entity object to use if no entity reference is defined.
	 * @return The selector value of the indicated object in an appropriate output display string.
	 */
	String read(Context context, Object focus);

	/**
	 * Get selector value of the entity in the context, as a raw Object, not formatted
	 * 
	 * @param context
	 *        The UI contet, containing the available entities.
	 * @param focus
	 *        Entity object to use if no entity reference is defined.
	 * @return The selector value of the indicated object as a raw Object, not formatted
	 */
	Object readObject(Context context, Object focus);

	/**
	 * Set the message selector for a format text.
	 * 
	 * @param format
	 *        the message selector for a format text.
	 * @return self.
	 */
	PropertyReference setFormat(String format);

	/**
	 * Set a format delegate to do the formatting.
	 * 
	 * @param formatter
	 *        The format delegate.
	 * @return self.
	 */
	PropertyReference setFormatDelegate(FormatDelegate formatter);

	/**
	 * Set the reference for encoding the index.
	 * 
	 * @param indexRef
	 *        The reference for encoding the index.
	 * @return self.
	 */
	PropertyReference setIndexReference(String indexRef);

	/**
	 * Set the message selector for the text to use if the selector value cannot be found, or is null.
	 * 
	 * @param text
	 *        The message selector.
	 * @return self.
	 */
	PropertyReference setMissingText(String text);

	/**
	 * Set the possible values (other than null) that are considered "missing"
	 * 
	 * @param values
	 *        The missing values.
	 * @return self.
	 */
	PropertyReference setMissingValues(String... values);

	/**
	 * Set both the entity and property reference (entity . property) from a full reference.
	 * 
	 * @param fullReference
	 *        The full reference.
	 * @return self.
	 */
	PropertyReference setReference(String fullReference);

	/**
	 * Write the (commons file upload) FileItem values into the referenced property.
	 * 
	 * @param context
	 *        The UI contet, containing the available entities.
	 * @param value
	 *        The value to write.
	 */
	void write(Context context, FileItem value);

	/**
	 * Write the values into the referenced property.
	 * 
	 * @param context
	 *        The UI contet, containing the available entities.
	 * @param value
	 *        The value to write.
	 */
	void write(Context context, String... value);
}
