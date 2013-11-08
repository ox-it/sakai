/**
 * Copyright (c) 2011 University of Bolton
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, 
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * The above copyright notice and this permission notice shall be included in all copies 
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.xcri.util;

import org.jdom2.Element;
import org.jdom2.filter.AbstractFilter;

/**
 * Filters HTML to highlight tags that might be potentially dangerous.
 */
public class ContentSecurityFilter extends AbstractFilter<Element> {

	private static final long serialVersionUID = 2626349759528661311L;
	
	private static final String elementNames[] = {"IMG", "SCRIPT", "EMBED", "OBJECT", "FRAME", "FRAMESET", "IFRAME", "META", "LINK"};

	/**
	 * {@inheritDoc}
	 */
	public Element filter(Object content) {
		if (content instanceof Element){
			Element element = (Element)content;
			String name = element.getName().toUpperCase();
			for (String elementName: elementNames)
				if (elementName.equals(name)) return element;
		}
		return null;
	}

}
