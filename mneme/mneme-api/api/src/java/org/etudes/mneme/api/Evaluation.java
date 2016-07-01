/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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

import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.entity.api.Reference;

/**
 * Evaluation holds a comment and a score from an evaluator for a submission or an answer.
 */
public interface Evaluation
{
	/**
	 * Accept a file upload for the attachments.
	 * 
	 * @param file
	 *        The file.
	 */
	public void setUpload(FileItem file);

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
	 * Access the user / date attribution for who made this evaluation and when.
	 * 
	 * @return The attribution.
	 */
	Attribution getAttribution();

	/**
	 * Access the rich text (html) comment.
	 * 
	 * @return The rich text (html) comment.
	 */
	String getComment();

	/**
	 * @return The rich text (html) comment specially formatted.
	 */
	String getCommentFormatted();

	/**
	 * Check if the evaluation is defined - score, comment or attachment.
	 * 
	 * @return TRUE if there is a score, comment or attachment, FALSE if none.
	 */
	Boolean getDefined();

	/**
	 * Access the evaluated flag.
	 * 
	 * @return TRUE if marked evaluated, FALSE if not.
	 */
	Boolean getEvaluated();

	/**
	 * Access the score.
	 * 
	 * @return The score.
	 */
	Float getScore();

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
	 * Set the rich text (html) comment.
	 * 
	 * @param comment
	 *        The rich text (html) comment. Must be well formed HTML or plain text.
	 */
	void setComment(String comment);

	/**
	 * Set the evaluated flag.
	 * 
	 * @param Boolean
	 *        TRUE if marked evaluated, FALSE if not.
	 */
	void setEvaluated(Boolean evaluated);

	/**
	 * Set the score.
	 * 
	 * @param score
	 *        The score.
	 */
	void setScore(Float score);
}
