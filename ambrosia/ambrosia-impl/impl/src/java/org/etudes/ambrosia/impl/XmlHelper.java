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

package org.etudes.ambrosia.impl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML processing support
 */
public class XmlHelper
{
	/**
	 * Find a clild node that is an Element and has a specified tag name. Finds the first and only one.
	 * 
	 * @param xml
	 *        The DOM tree to search.
	 * @param name
	 *        The name to search for.
	 * @return The Element that is a child with this name, or null if none are found.
	 */
	public static Element getChildElementNamed(Element xml, String name)
	{
		// sub-element configuration
		NodeList childNodes = xml.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element) node;

				// confirm
				if (element.getTagName().equals(name))
				{
					return element;
				}
			}
		}

		return null;
	}
}
