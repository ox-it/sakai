/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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

package org.etudes.mneme.api;

import java.util.List;

import org.sakaiproject.entity.api.Reference;

/**
 * Presentation defines a rich text with attachments.
 */
public interface Presentation
{
	/**
	 * Add this reference as another attachment.
	 * 
	 * @param reference
	 *        The attachment reference string.
	 */
	void addAttachment(Reference reference);

	/**
	 * Access the attachments for the presentation, each a reference string.
	 * 
	 * @return The list of attachment references, or an empty list if there are none.
	 */
	List<Reference> getAttachments();

	/**
	 * Check if there is nothing defined for this presentation.
	 * 
	 * @return TRUE if the text and attachments are empty, FALSE if there is something defined in either place.
	 */
	Boolean getIsEmpty();

	/**
	 * Access the rich text (html) part of the presentation.
	 * 
	 * @return The rich text (html) part of the presentation.
	 */
	String getText();

	/**
	 * Remove the attachment that matches this reference string.
	 * 
	 * @param reference
	 *        The attachment to remove.
	 */
	void removeAttachment(Reference reference);

	/**
	 * Set the attachments to these references.
	 * 
	 * @param references
	 *        The list of attachment references.
	 */
	void setAttachments(List<Reference> references);

	/**
	 * Set the rich text (html) part of the presentation.
	 * 
	 * @param text
	 *        The rich text (html) part of the presentation. Must be well formed HTML or plain text.
	 */
	void setText(String text);
}
