/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.importer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XPathHelper {
	
	public static List selectNodes(String expression, Object doc) {

		if (doc == null) return new ArrayList();
		List nodes = null;
		try {
			XPath xpath = createNamespacedXPath(expression, doc);
			nodes = xpath.selectNodes(doc);
		} catch (JaxenException e) {
			return null;
		}
		return nodes;  
	}

	public static Node selectNode(String expression, Object doc) {
		if (doc == null) return null;
		Node node = null;
		try {
			XPath xpath = createNamespacedXPath(expression, doc);
			node = (Node) xpath.selectSingleNode(doc);
		} catch (JaxenException e) {
			return null;
		}
		return node;
	}

	public static String getNodeValue(String expression, Node node) {
		if (node == null) return "";
		String value = null;
		try {
			XPath xpath = createNamespacedXPath(expression, node);
			value = xpath.stringValueOf(node);
		} catch (JaxenException e) {
			return null;
		}
		return value;
	}

	private static XPath createNamespacedXPath(String expression, Object obj) {

		/* Difficulties with Jaxen and namespaces are that:
		 * 1) if the XPath expression includes a prefix, you need to set a namespace context first, and
		 * 2) once the document parser has been set to be namespace aware (necessary in order to handle multiple namespaces)
		 * all XPath expressions have to include prefixes, even for the default namespace, and that prefix needs to be associated
		 *  with the default namespace URI (and an empty string doesn't work.)
		 * So, simplest solution seems to add a (random) prefix to any expressions that don't have any prefixes
		 * (and ignore any expressions with a mixture of prefixed and non-prefixed )
		 * */
		Document doc;
		if (obj instanceof Document)
			doc = (Document) obj;
		else if (obj instanceof Node)
			doc = ((Node) obj).getOwnerDocument();
		else return null; //TODO handle null in calling methods...

		try {
			XPath xpath = new DOMXPath(expression);
			SimpleNamespaceContext context = new SimpleNamespaceContext();
			// add all namespaces and prefixes in the document:
			context.addElementNamespaces(xpath.getNavigator(), doc.getDocumentElement());
			xpath.setNamespaceContext(context);
			if (!expression.contains(":"))
			{
				String defNS = context.translateNamespacePrefixToUri("");
				context.addNamespace("df", defNS);
				StringTokenizer tk = new StringTokenizer(expression, "/", true);
				if (tk.hasMoreTokens()) {
					StringBuffer prefixedExpression = new StringBuffer();
					while (tk.hasMoreTokens()) {
						String token = tk.nextToken();
						if (token.equals(".") || token.equals("/") || token.startsWith("@"))
							prefixedExpression.append(token);
						else
							prefixedExpression.append("df:" + token);
					}
					if (prefixedExpression.length() > 0)
						expression = prefixedExpression.toString();
				}
				else
					expression = "df:" + expression;
				xpath = new DOMXPath(expression);
				xpath.setNamespaceContext(context);
			}
			return xpath;

		} catch (JaxenException e) {
			return null; // TODO handle in calling method.
		}
	}
}
