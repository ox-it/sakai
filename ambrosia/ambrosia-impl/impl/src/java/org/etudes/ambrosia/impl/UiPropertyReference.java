/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2014 Etudes, Inc.
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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.FormatDelegate;
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.util.HtmlHelper;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * UiPropertyReference accesses a particular selector of a model entity. If the reference is not set, we attempt to just get the object itself.<br />
 * Nested references are supported. For example, "foo.bar" means call the entity.getFoo(), and with the result of that, call getBar().<br />
 * Missing values can be set to return a specified message.
 */
public class UiPropertyReference implements PropertyReference
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(UiPropertyReference.class);

	/** The entity reference. */
	protected String entityReference = null;

	/** The message selector for a format display. */
	protected String format = null;

	/** A delegate to do the formatting. */
	protected FormatDelegate formatDelegate = null;

	/** A ref for making an index in getEncoding. */
	protected String indexRef = null;

	/** The text (message selector) to use if a selector value cannot be found or is null. */
	protected String missingText = null;

	/** The list of values to be considered missing, along with null. */
	protected String[] missingValues = null;

	/** The list of other properties to combine into a formatted display. */
	protected List<PropertyReference> properties = new ArrayList<PropertyReference>();

	/** The entity selector reference. */
	protected String propertyReference = null;

	/**
	 * No-arg constructor.
	 */
	public UiPropertyReference()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiPropertyReference(UiServiceImpl service, Element xml)
	{
		String ref = StringUtil.trimToNull(xml.getAttribute("ref"));
		if (ref != null)
		{
			setReference(ref);
		}

		String formatDelegate = StringUtil.trimToNull(xml.getAttribute("delegate"));
		String tool = StringUtil.trimToNull(xml.getAttribute("tool"));
		if ((formatDelegate != null) || (tool != null))
		{
			FormatDelegate d = service.getFormatDelegate(formatDelegate, tool);
			if (d != null)
			{
				this.formatDelegate = d;
			}
			else
			{
				M_log.warn("missing delegate: " + formatDelegate + " tool: " + tool);
			}
		}

		String missingText = StringUtil.trimToNull(xml.getAttribute("missing"));
		if (missingText != null) setMissingText(missingText);

		Element settingsXml = XmlHelper.getChildElementNamed(xml, "missingValues");
		if (settingsXml != null)
		{
			List<String> missingValues = new ArrayList<String>();
			NodeList contained = settingsXml.getChildNodes();
			for (int i = 0; i < contained.getLength(); i++)
			{
				Node node = contained.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element valueXml = (Element) node;
					if ("missing".equals(valueXml.getTagName()))
					{
						String value = StringUtil.trimToNull(valueXml.getAttribute("value"));
						if (value != null) missingValues.add(value);
					}
				}
			}

			if (!missingValues.isEmpty())
			{
				this.missingValues = missingValues.toArray(new String[missingValues.size()]);
			}
		}

		// related references
		// use all the direct model references
		NodeList settings = xml.getChildNodes();
		for (int i = 0; i < settings.getLength(); i++)
		{
			Node node = settings.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element innerXml = (Element) node;
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) addProperty(pRef);
			}
		}

		ref = StringUtil.trimToNull(xml.getAttribute("indexRef"));
		if (ref != null)
		{
			setIndexReference(ref);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference addProperty(PropertyReference property)
	{
		properties.add(property);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEncoding(Context context, Object focus, int index)
	{
		// start with a full reference
		StringBuffer rv = new StringBuffer();
		rv.append(getFullReference(context));

		// add the special object selector
		rv.append(".[");

		// if we have an index ref, apply it to the focus for the index value
		if (this.indexRef != null)
		{
			Object o = getNestedValue(context, this.indexRef, focus, false);
			if (o != null) rv.append(encode(o.toString()));
		}

		else
		{
			// append the index
			rv.append(Integer.toString(index));
		}

		rv.append("]");

		return rv.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFullReference(Context context)
	{
		// if the entity reference has a encode value in context, use that instead of the entity reference value
		String entityRefName = this.entityReference;
		if (entityRefName != null)
		{
			String encoding = context.getEncoding(entityRefName);
			if (encoding != null)
			{
				entityRefName = encoding;
			}
		}

		// if the property ref has any sub-property reference, expand into an index (map) reference
		String ref = this.propertyReference;
		if ((ref != null) && (ref.indexOf(".{") != -1))
		{
			StringBuilder newRef = new StringBuilder();
			String[] nesting = ref.split("\\.");
			for (String s : nesting)
			{
				// sub-property reference
				if (s.startsWith("{") && s.endsWith("}"))
				{
					try
					{
						String propertiesIndex = s.substring(1, s.length() - 1);
						int i = Integer.parseInt(propertiesIndex);
						PropertyReference pref = this.properties.get(i);
						String index = pref.read(context, null);

						newRef.append(".[");
						newRef.append(index);
						newRef.append("]");
					}
					catch (NumberFormatException e)
					{
					}
					catch (IndexOutOfBoundsException e)
					{
					}
				}

				else
				{
					newRef.append(".");
					newRef.append(s);
				}
			}
			ref = newRef.toString();
		}
		else if (ref != null)
		{
			ref = "." + ref;
		}

		return (entityRefName == null ? "" : entityRefName) + (ref == null ? "" : ref);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String read(Context context, Object focus)
	{
		// read the object
		Object value = readObject(context, focus);

		// return the missing text if defined
		if (missing(value)) return missingValue(context);

		// format
		String formatted = format(context, value);

		// if we are not specially formatted, we are done
		if (this.format == null) return formatted;

		// use all properties for the format, starting with the main one
		Object[] args = new Object[1 + properties.size()];
		args[0] = formatted;
		int i = 1;
		for (PropertyReference prop : properties)
		{
			args[i++] = prop.read(context, focus);
		}

		// format the group
		String finalFormat = context.getMessages().getFormattedMessage(this.format, args);
		return finalFormat;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readObject(Context context, Object focus)
	{
		// wean off of null entity refs
		if (this.entityReference == null)
		{
			// using a format delegate it's ok
			if (this.formatDelegate == null)
			{
				M_log.warn("read: no entity reference: property reference: " + this.propertyReference);
			}
		}

		// use the focus if we don't have a reference defined
		Object entity = focus;

		if (this.entityReference != null)
		{
			entity = (Object) context.get(entityReference);
		}

		if (entity == null) return null;

		// pull out the value object
		Object value = getNestedValue(context, this.propertyReference, entity, false);

		// give the format delegate a shot at manipulating the object
		if (this.formatDelegate != null)
		{
			value = this.formatDelegate.formatObject(context, value);
		}

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference setFormat(String format)
	{
		this.format = format;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference setFormatDelegate(FormatDelegate formatter)
	{
		this.formatDelegate = formatter;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference setIndexReference(String indexRef)
	{
		this.indexRef = indexRef;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference setMissingText(String text)
	{
		this.missingText = text;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference setMissingValues(String... values)
	{
		missingValues = values;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference setReference(String fullReference)
	{
		// split out the entity and property references
		int pos = fullReference.indexOf(".");
		if (pos > -1)
		{
			this.entityReference = fullReference.substring(0, pos);
			this.propertyReference = fullReference.substring(pos + 1);
		}

		// if this is just an entity reference
		else
		{
			this.entityReference = fullReference;
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public void write(Context context, FileItem value)
	{
		// start with our entity from the contxt
		Object entity = null;
		if (this.entityReference != null)
		{
			entity = (Object) context.get(entityReference);
		}

		if (entity == null) return;

		// read all the way to one property short of the end - that's the object we are writing to
		Object target = getNestedValue(context, this.propertyReference, entity, true);

		// write value to the property of target that is the last dotted component of our property reference
		int pos = this.propertyReference.lastIndexOf(".");
		String lastProperty = this.propertyReference.substring(pos + 1);

		setFileValue(target, lastProperty, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void write(Context context, String... value)
	{
		// start with our entity from the context
		Object entity = null;
		if (this.entityReference != null)
		{
			entity = (Object) context.get(this.entityReference);
		}

		if (entity == null) return;

		// read all the way to one property short of the end - that's the object we are writing to
		Object target = getNestedValue(context, this.propertyReference, entity, true);

		// write value to the property of target that is the last dotted component of our property reference
		int pos = this.propertyReference.lastIndexOf(".");
		String lastProperty = this.propertyReference.substring(pos + 1);

		if (target == null)
		{
			M_log.warn("write: null target: " + this.entityReference + " . " + this.propertyReference);
		}
		else
		{
			setValue(target, lastProperty, value);
		}
	}

	/**
	 * Decode the encode()ed value.
	 * 
	 * @param value
	 *        The encode()ed value.
	 * @return The decoded value.
	 */
	protected String decode(String value)
	{
		if (value == null) return value;

		// if this is not encoded
		if (!value.startsWith("x")) return value;

		// decode each 4 character chunk, skipping the first character, as a hex value
		StringBuilder rv = new StringBuilder();
		for (int i = 1; i < value.length(); i += 4)
		{
			String next = value.substring(i, i + 4);
			int c = Integer.parseInt(next, 16);
			rv.append((char) c);
		}

		return rv.toString();
	}

	/**
	 * Encode the value to make it safe for a property reference - only alpha-numeric characters.
	 * 
	 * @param value
	 *        The value to encode.
	 * @return The encoded value.
	 */
	protected String encode(String value)
	{
		if (value == null) return value;

		StringBuilder rv = new StringBuilder();
		rv.append("x");

		// encode as a series of 4 character hex digits for each character
		for (int i = 0; i < value.length(); i++)
		{
			int c = value.charAt(i);
			String encoded = Integer.toString(c, 16);
			switch (encoded.length())
			{
				case 1:
				{
					rv.append("000");
					break;
				}
				case 2:
				{
					rv.append("00");
					break;
				}
				case 3:
				{
					rv.append("0");
					break;
				}
			}
			rv.append(encoded);
		}

		return rv.toString();
	}

	/**
	 * Format the value found into a display string.
	 * 
	 * @param context
	 *        The Context.
	 * @param value
	 *        The value.
	 * @return The value formatted into a display string.
	 */
	protected String format(Context context, Object value)
	{
		String rv = null;

		if (this.formatDelegate != null)
		{
			rv = this.formatDelegate.format(context, value);
		}
		else
		{
			if (value != null)
			{
				rv = value.toString();
			}
		}

		return StringUtil.trimToNull(rv);
	}

	/**
	 * Get the index item from the collection.
	 * 
	 * @param collection
	 *        The collection (Collection or array)
	 * @param index
	 *        The index.
	 * @return The indexed item from the collection, or null if not found or not a collection
	 */
	protected Object getIndexValue(Object collection, String index)
	{
		if (collection == null) return null;

		if (collection instanceof List)
		{
			int i = Integer.parseInt(index);
			List l = (List) collection;
			if ((i >= 0) && (i < l.size()))
			{
				return l.get(i);
			}
		}

		else if (collection.getClass().isArray())
		{
			int i = Integer.parseInt(index);
			Object[] a = (Object[]) collection;
			if ((i >= 0) && (i < a.length))
			{
				return a[i];
			}
		}

		else if (collection instanceof PopulatingSet)
		{
			// treat the index as an id...
			Object o = ((PopulatingSet) collection).assure(index);
			return o;
		}

		else if (collection instanceof Map)
		{
			// treat as a key
			Object o = ((Map) collection).get(index);
			return o;
		}

		return null;
	}

	/**
	 * Read the configured selector value from the entity. Support "." nesting of selector values.
	 * 
	 * @param entity
	 *        The entity to read from.
	 * @return The selector value object found, or null if not.
	 */
	protected Object getNestedValue(Context context, String ref, Object entity, boolean skipLast)
	{
		// if no property defined, used the entity
		if (ref == null) return entity;

		// if not nested, return a simple dereference (unless we are skiping the last, which in this is all, so return the entity)
		if (ref.indexOf(".") == -1)
		{
			if (skipLast) return entity;
			return getValue(context, entity, ref);
		}

		String[] nesting = ref.split("\\.");
		Object current = entity;
		for (String s : nesting)
		{
			// if last and we want to skip last, get out
			if ((skipLast) && (s == nesting[nesting.length - 1])) break;

			current = getValue(context, current, s);

			// early exit if we run out of values
			if (current == null) break;
		}

		return current;
	}

	/**
	 * Read the configured selector value from the entity.
	 * 
	 * @param context
	 *        The context.
	 * @param entity
	 *        The entity to read from.
	 * @param selector
	 *        The selector name.
	 * @return The selector value object found, or null if not.
	 */
	protected Object getValue(Context context, Object entity, String property)
	{
		// if no selector named, just use the entity
		if (property == null) return entity;

		// if the property is an index reference
		if (property.startsWith("[") && property.endsWith("]"))
		{
			return getIndexValue(entity, decode(property.substring(1, property.length() - 1)));
		}

		// another form of index, taking the index value from the nested references
		if (property.startsWith("{") && property.endsWith("}"))
		{
			try
			{
				String propertiesIndex = property.substring(1, property.length() - 1);
				int i = Integer.parseInt(propertiesIndex);
				PropertyReference ref = this.properties.get(i);
				String index = ref.read(context, entity);

				return getIndexValue(entity, index);
			}
			catch (NumberFormatException e)
			{
			}
			catch (IndexOutOfBoundsException e)
			{
			}
		}

		// form a "getFoo()" based getter method name
		StringBuffer getter = new StringBuffer("get" + property);
		getter.setCharAt(3, getter.substring(3, 4).toUpperCase().charAt(0));

		try
		{
			// use this form, providing the getter name and no setter, so we can support properties that are read-only
			PropertyDescriptor pd = new PropertyDescriptor(property, entity.getClass(), getter.toString(), null);
			Method read = pd.getReadMethod();
			Object value = read.invoke(entity, (Object[]) null);
			return value;
		}
		catch (IntrospectionException ie)
		{
			M_log.warn("getValue: method: " + property + " object: " + entity.getClass(), ie);
			return null;
		}
		catch (IllegalAccessException ie)
		{
			M_log.warn("getValue: method: " + property + " object: " + entity.getClass(), ie);
			return null;
		}
		catch (IllegalArgumentException ie)
		{
			M_log.warn("getValue: method: " + property + " object: " + entity.getClass(), ie);
			return null;
		}
		catch (InvocationTargetException ie)
		{
			M_log.warn("getValue: method: " + property + " object: " + entity.getClass(), ie);
			return null;
		}
	}

	/**
	 * Check if this value is considered a missing value.
	 * 
	 * @param value
	 *        The value to test.
	 * @return true if this is considered missing, false if not.
	 */
	protected boolean missing(Object value)
	{
		if (value == null) return true;

		if (value instanceof Collection)
		{
			if (((Collection) value).isEmpty()) return true;
		}

		String val = StringUtil.trimToNull(value.toString());
		if (val == null) return true;

		if ((this.missingValues != null) && (StringUtil.contains(this.missingValues, val))) return true;

		return false;
	}

	/**
	 * Return the value for when we are missing the value - either null or the missing value message.
	 * 
	 * @param context
	 *        The Context.
	 * @return The value for when we are missing the value - either null or the missing value message.
	 */
	protected String missingValue(Context context)
	{
		if (this.missingText == null) return null;

		return context.getMessages().getString(this.missingText);
	}

	/**
	 * Write the value for FileItem (commons file upload) values.
	 * 
	 * @param entity
	 *        The entity to write to.
	 * @param property
	 *        The property to set.
	 * @param value
	 *        The value to write.
	 */
	protected void setFileValue(Object entity, String property, FileItem value)
	{
		// form a "setFoo()" based setter method name
		StringBuffer setter = new StringBuffer("set" + property);
		setter.setCharAt(3, setter.substring(3, 4).toUpperCase().charAt(0));

		try
		{
			// use this form, providing the setter name and no getter, so we can support properties that are write-only
			PropertyDescriptor pd = new PropertyDescriptor(property, entity.getClass(), null, setter.toString());
			Method write = pd.getWriteMethod();
			Object[] params = new Object[1];

			Class[] paramTypes = write.getParameterTypes();
			if ((paramTypes != null) && (paramTypes.length == 1))
			{
				// single value boolean
				if (paramTypes[0] != FileItem.class)
				{
					M_log.warn("setFileValue: target not expecting FileItem: " + entity.getClass() + " " + property);
					return;
				}

				params[0] = value;
				write.invoke(entity, params);
			}
			else
			{
				M_log.warn("setFileValue: method: " + property + " object: " + entity.getClass() + " : no one parameter setter method defined");
			}
		}
		catch (IntrospectionException ie)
		{
			M_log.warn("setFileValue: method: " + property + " object: " + entity.getClass() + " : " + ie);
		}
		catch (IllegalAccessException ie)
		{
			M_log.warn("setFileValue: method: " + property + " object: " + entity.getClass() + " :" + ie);
		}
		catch (IllegalArgumentException ie)
		{
			M_log.warn("setFileValue: method: " + property + " object: " + entity.getClass() + " :" + ie);
		}
		catch (InvocationTargetException ie)
		{
			M_log.warn("setFileValue: method: " + property + " object: " + entity.getClass() + " :" + ie);
		}
	}

	/**
	 * Write the value.
	 * 
	 * @param entity
	 *        The entity to write to.
	 * @param property
	 *        The property to set.
	 * @param value
	 *        The value to write.
	 */
	protected void setValue(Object entity, String property, String[] valueSource)
	{
		// form a "setFoo()" based setter method name
		StringBuffer setter = new StringBuffer("set" + property);
		setter.setCharAt(3, setter.substring(3, 4).toUpperCase().charAt(0));

		// unformat the values - in any are invalid, give up
		String[] value = null;
		try
		{
			if (valueSource != null)
			{
				value = new String[valueSource.length];
				for (int i = 0; i < valueSource.length; i++)
				{
					value[i] = StringUtil.trimToNull(unFormat(valueSource[i]));
				}
			}
		}
		catch (IllegalArgumentException e)
		{
			return;
		}

		try
		{
			// use this form, providing the setter name and no getter, so we can support properties that are write-only
			PropertyDescriptor pd = new PropertyDescriptor(property, entity.getClass(), null, setter.toString());
			Method write = pd.getWriteMethod();
			Object[] params = new Object[1];
			params[0] = null;

			Class[] paramTypes = write.getParameterTypes();
			if ((paramTypes != null) && (paramTypes.length == 1))
			{
				// single value boolean
				if (paramTypes[0] == Boolean.class)
				{
					params[0] = ((value != null) && (value[0] != null)) ? Boolean.valueOf(StringUtil.trimToZero(value[0])) : null;
				}

				// multiple value boolean
				else if (paramTypes[0] == Boolean[].class)
				{
					if (value != null)
					{
						Boolean[] values = new Boolean[value.length];
						for (int i = 0; i < value.length; i++)
						{
							values[i] = Boolean.valueOf(StringUtil.trimToZero(value[i]));
						}
						params[0] = values;
					}
				}

				// single value long
				else if (paramTypes[0] == Long.class)
				{
					params[0] = ((value != null) && (value[0] != null)) ? Long.valueOf(StringUtil.trimToZero(value[0])) : null;
				}

				// multiple value long
				else if (paramTypes[0] == Long[].class)
				{
					if (value != null)
					{
						Long[] values = new Long[value.length];
						for (int i = 0; i < value.length; i++)
						{
							values[i] = Long.valueOf(StringUtil.trimToZero(value[i]));
						}
						params[0] = values;
					}
				}

				// single value int
				else if (paramTypes[0] == Integer.class)
				{
					params[0] = ((value != null) && (value[0] != null)) ? Integer.valueOf(StringUtil.trimToZero(value[0])) : null;
				}

				// multiple value int
				else if (paramTypes[0] == Integer[].class)
				{
					if (value != null)
					{
						Integer[] values = new Integer[value.length];
						for (int i = 0; i < value.length; i++)
						{
							values[i] = Integer.valueOf(StringUtil.trimToZero(value[i]));
						}
						params[0] = values;
					}
				}

				// single value Date
				else if (paramTypes[0] == Date.class)
				{
					// assume a long ms format
					params[0] = ((value != null) && (value[0] != null)) ? new Date(Long.parseLong(value[0])) : null;
				}

				// multiple value Date
				else if (paramTypes[0] == Date[].class)
				{
					if (value != null)
					{
						Date[] values = new Date[value.length];
						for (int i = 0; i < value.length; i++)
						{
							// assume a long ms format
							values[i] = new Date(Long.parseLong(value[i]));
						}
						params[0] = values;
					}
				}

				// single value string
				else if (paramTypes[0] == String.class)
				{
					params[0] = ((value != null) && (value[0] != null)) ? StringUtil.trimToNull(value[0]) : null;
				}

				// multiple value string
				else if (paramTypes[0] == String[].class)
				{
					// trim it
					if (value != null)
					{
						for (int i = 0; i < value.length; i++)
						{
							value[i] = StringUtil.trimToNull(value[i]);
						}
					}

					params[0] = value;
				}

				// single value enum
				else if (paramTypes[0].isEnum())
				{
					if ((value == null) || (value[0] == null))
					{
						params[0] = null;
					}
					else
					{
						Object[] constants = paramTypes[0].getEnumConstants();
						if (constants != null)
						{
							// see if value matches any of these
							for (Object o : constants)
							{
								if (o.toString().equals(value[0]))
								{
									params[0] = o;
									break;
								}
							}
						}
					}
				}

				// single value float
				else if (paramTypes[0] == Float.class)
				{
					params[0] = ((value != null) && (value[0] != null)) ? Float.valueOf(StringUtil.trimToZero(value[0])) : null;
				}

				// multiple value float
				else if (paramTypes[0] == Float[].class)
				{
					if (value != null)
					{
						Float[] values = new Float[value.length];
						for (int i = 0; i < value.length; i++)
						{
							values[i] = Float.valueOf(StringUtil.trimToZero(value[i]));
						}
						params[0] = values;
					}
				}

				// multiple value string in list
				else if (paramTypes[0] == List.class)
				{
					if (value != null)
					{
						// trim it into a List
						List valueList = new ArrayList(value.length);
						if (value != null)
						{
							for (int i = 0; i < value.length; i++)
							{
								String v = StringUtil.trimToNull(value[i]);
								if (v != null)
								{
									valueList.add(v);
								}
							}
						}

						params[0] = valueList;
					}
				}

				// multiple value string in set
				else if (paramTypes[0] == Set.class)
				{
					if (value != null)
					{
						// trim it into a List
						Set valueSet = new HashSet(value.length);
						for (int i = 0; i < value.length; i++)
						{
							String v = StringUtil.trimToNull(value[i]);
							if (v != null)
							{
								valueSet.add(v);
							}
						}

						params[0] = valueSet;
					}
				}

				// TODO: other types
				else
				{
					M_log.warn("setValue: unhandled setter parameter type - not set: " + paramTypes[0]);
					return;
				}

				write.invoke(entity, params);
			}
			else
			{
				M_log.warn("setValue: method: " + property + " object: " + entity.getClass() + " : no one parameter setter method defined");
			}
		}
		catch (NumberFormatException ie)
		{
		}
		catch (IntrospectionException ie)
		{
			M_log.warn("setValue: method: " + property + " object: " + entity.getClass(), ie);
		}
		catch (IllegalAccessException ie)
		{
			M_log.warn("setValue: method: " + property + " object: " + entity.getClass(), ie);
		}
		catch (IllegalArgumentException ie)
		{
			M_log.warn("setValue: method: " + property + " object: " + entity.getClass(), ie);
		}
		catch (InvocationTargetException ie)
		{
			M_log.warn("setValue: method: " + property + " object: " + entity.getClass(), ie);
		}
	}

	/**
	 * UnFormat the value, essentially reversing the format() call.
	 * 
	 * @param value
	 *        The formatted value to unformat.
	 * @return The unformatted value.
	 */
	protected String unFormat(String value)
	{
		// deal with special UNICODE characters
		value = HtmlHelper.stripBadEncodingCharacters(value);

		return value;
	}
}
