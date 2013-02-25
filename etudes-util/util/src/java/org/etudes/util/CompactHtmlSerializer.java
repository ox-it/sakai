/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2012 Etudes, Inc.
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

// based on org.htmlcleaner.CompactHtmlSerializer:

/**  Copyright (c) 2006-2007, Vladimir Nikic
 All rights reserved.

 Redistribution and use of this software in source and binary forms, 
 with or without modification, are permitted provided that the following 
 conditions are met:

 * Redistributions of source code must retain the above
 copyright notice, this list of conditions and the
 following disclaimer.

 * Redistributions in binary form must reproduce the above
 copyright notice, this list of conditions and the
 following disclaimer in the documentation and/or other
 materials provided with the distribution.

 * The name of HtmlCleaner may not be used to endorse or promote 
 products derived from this software without specific prior
 written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.

 You can contact Vladimir Nikic by sending e-mail to
 nikic_vladimir@yahoo.com. Please include the word "HtmlCleaner" in the
 subject line.
 **/

package org.etudes.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.htmlcleaner.BaseToken;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CommentNode;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlSerializer;
import org.htmlcleaner.TagNode;

public class CompactHtmlSerializer extends HtmlSerializer
{
	private int openPreTags = 0;

	// Note: title and body need to be considered block for our logic; the blank string is what body looks like when we are doing fragment
	protected Set<String> blockElements = new HashSet<String>(Arrays.asList("address", "article", "audio", "blockquote", "canvas", "dd", "div", "dl",
			"fieldset", "figcaption", "figure", "footer", "form", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hgroup", "hr", "noscript", "ol",
			"output", "p", "pre", "section", "table", "tfoot", "ul", "video", "title", "body", ""));

	public CompactHtmlSerializer(CleanerProperties props)
	{
		super(props);
	}

	@SuppressWarnings("rawtypes")
	protected boolean isFirstTagOrContentNode(List nodes, int index)
	{
		// no tag or content nodes before - true
		// is there a tag or content node before index? If so, the node at index is not the first
		for (int i = 0; i < index; i++)
		{
			if (nodes.get(i) instanceof ContentNode)
			{
				// don't count empty content nodes
				String content = nodes.get(i).toString();
				boolean contentIsEmpty = (content.replaceAll("\\s+", "").length() == 0);
				if (contentIsEmpty) continue;

				return false;
			}

			if (nodes.get(i) instanceof TagNode) return false;
		}

		return true;
	}

	@SuppressWarnings("rawtypes")
	protected boolean isLastTagOrContentNode(List nodes, int index)
	{
		// no tag or content nodes after - true
		// is there a tag or content node after index? If so, the node at index is not the last
		for (int i = index + 1; i < nodes.size(); i++)
		{
			if (nodes.get(i) instanceof ContentNode)
			{
				// don't count empty content nodes
				String content = nodes.get(i).toString();
				boolean contentIsEmpty = (content.replaceAll("\\s+", "").length() == 0);
				if (contentIsEmpty) continue;

				return false;
			}

			if (nodes.get(i) instanceof TagNode) return false;
		}

		return true;
	}

	@SuppressWarnings("rawtypes")
	protected void serialize(TagNode tagNode, Writer writer) throws IOException
	{
		String tagName = tagNode.getName();
		boolean isBodyTag = "body".equalsIgnoreCase(tagName) || "".equalsIgnoreCase(tagName);
		boolean isPreTag = "pre".equalsIgnoreCase(tagName);
		if (isPreTag)
		{
			openPreTags++;
		}

		serializeOpenTag(tagNode, writer, false);

		List tagChildren = tagNode.getChildren();

		// in the body tag, we need to wrap consecutive non-block TagNodes or ContentNodes in a block tag <p>
		boolean wrapping = false;

		if (!isMinimizedTagSyntax(tagNode))
		{
			for (int i = 0; i < tagChildren.size(); i++)
			{
				Object item = tagChildren.get(i);
				if (item instanceof ContentNode)
				{
					String content = item.toString();

					if (openPreTags > 0)
					{
						writer.write(content);
					}
					else
					{
						if (content.length() > 0)
						{
							boolean contentIsEmpty = (content.replaceAll("\\s+", "").length() == 0);

							// if empty, skip unless it is between two TagNodes that are non-block tags
							if (contentIsEmpty)
							{
								// not surrounded by tags
								if ((i == 0) || (i == tagChildren.size() - 1)) continue;

								Object leftNode = tagChildren.get(i - 1);
								Object rightNode = tagChildren.get(i + 1);
								boolean leftIsNonBlock = ((leftNode instanceof TagNode) && (!this.blockElements.contains(((TagNode) leftNode)
										.getName())));
								boolean rightIsNonBlock = ((rightNode instanceof TagNode) && (!this.blockElements.contains(((TagNode) rightNode)
										.getName())));

								if (!(leftIsNonBlock && rightIsNonBlock)) continue;
							}

							// whitespace rules:
							// consecutive whitespace collapses into a single space
							// leading space is allowed unless: the node is the first (TagNode or non-blank ContentNode) and the parent is a block element
							// trailing space is allowed unless: the node is the last (TagNode or non-blank ContentNode) and the parent is a block element
							boolean firstNode = isFirstTagOrContentNode(tagChildren, i);
							boolean lastNode = isLastTagOrContentNode(tagChildren, i);
							boolean parentIsBlock = this.blockElements.contains(tagNode.getName().toLowerCase());
							boolean hasLeadingSpace = Character.isWhitespace(content.charAt(0));
							boolean hasTrailingSpace = Character.isWhitespace(content.charAt(content.length() - 1));

							// trim, then add back
							content = content.trim();
							if (hasLeadingSpace && !(firstNode && parentIsBlock)) content = " " + content;
							if (hasTrailingSpace && !(lastNode && parentIsBlock)) content = content + " ";

							// collapse consecutive white space with a single space
							content = content.replaceAll("\\s+", " ");
						}

						if (!dontEscape(tagNode)) content = escapeText(content);

						if (content.length() != 0)
						{

							// if in body and not wrapping, start wrapping
							if (isBodyTag && !wrapping)
							{
								wrapping = true;
								writer.write("<p>");
							}

							writer.write(content);
						}
					}
				}
				else if (item instanceof CommentNode)
				{
					String content = ((CommentNode) item).getCommentedContent().trim();
					writer.write(content);
				}
				else if (item instanceof BaseToken)
				{
					// if in body and not wrapping, and this is a non-block node, start wrapping
					if (isBodyTag && (item instanceof TagNode))
					{
						boolean isBlock = this.blockElements.contains(((TagNode) item).getName());

						// if not wrapping and this is a non-block TagNode, start wrapping
						if ((!wrapping) && (!isBlock))
						{
							wrapping = true;
							writer.write("<p>");
						}

						// if wrapping and this is a block node, stop wrapping
						else if (wrapping && isBlock)
						{
							writer.write("</p>");
							wrapping = false;
						}
					}

					// once we see a TagNode, we no longer skip empty ContentNode children in the body tag
					((BaseToken) item).serialize(this, writer);
				}
			}

			// if wrapping, stop wrapping
			if (wrapping)
			{
				writer.write("</p>");
				wrapping = false;
			}

			serializeEndTag(tagNode, writer, false);
			if (isPreTag)
			{
				openPreTags--;
			}
		}
	}
}
