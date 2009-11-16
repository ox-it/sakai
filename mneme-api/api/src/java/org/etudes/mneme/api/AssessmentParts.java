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

/**
 * AssessmentParts contain the details of the parts of an assessment.
 */
public interface AssessmentParts
{
	/**
	 * Add a hybrid (draw and manual) part.
	 * 
	 * @return The new part.
	 */
	Part addPart();

	/**
	 * Access the continuous numbering flag that controls the numbering of questions across part boundaries.
	 * 
	 * @return TRUE if numbering is continuous across the part boundaries, FALSE if numbering resets for each part.
	 */
	Boolean getContinuousNumbering();

	/**
	 * Access the part detail with this id.
	 * 
	 * @return The part detail with this id, or null if not found.
	 */
	PartDetail getDetailId(String id);

	/**
	 * Access all the part details in a single ordered (by part and sequence) list.
	 * 
	 * @return The list of all part details.
	 */
	List<PartDetail> getDetails();

	/**
	 * Get all the pools drawn from in the assessment parts, and their counts, sorted.
	 * 
	 * @return The List of draws.
	 */
	List<PoolDraw> getDraws(PoolService.FindPoolsSort sort);

	/**
	 * Get a list of draws for each pool specified - for those that are already in this part, set the non-null numQuestions.<br />
	 * The draws are virtual, not in the Parts.
	 * 
	 * @param context
	 *        The context.
	 * @param sort
	 *        The sort criteria (from the PoolService).
	 * @param search
	 *        The search criteria.
	 * @return A list of draws for each pool.
	 */
	List<PoolDraw> getDrawsForPools(String context, PoolService.FindPoolsSort sort, String search);

	/**
	 * Access the first part
	 * 
	 * @return the first part, of null if there are none.
	 */
	Part getFirst();

	/**
	 * Check if any valid parts have zero points.
	 * 
	 * @return TRUE if any valid parts have zero points, FALSE if not.
	 */
	Boolean getHasZeroPointParts();

	/**
	 * Check if the assessment parts are valid; i.e. exist and all have >0 questions
	 * 
	 * @return TRUE if the assessment parts are valid, FALSE if not.
	 */
	Boolean getIsValid();

	/**
	 * Access the count of questions randomly drawn from pools in all parts.
	 * 
	 * @return The count of questions randomly drawn in all parts.
	 */
	Integer getNumDrawQuestions();

	/**
	 * Access the number of parts.
	 * 
	 * @return The number of parts.
	 */
	Integer getNumParts();

	/**
	 * Access the count of questions in all parts.
	 * 
	 * @return The count of questions in all parts.
	 */
	Integer getNumQuestions();

	/**
	 * Access one of the part, by id.
	 * 
	 * @param id
	 *        The part id.
	 * @return the part, or null if not found.
	 */
	Part getPart(String id);

	/**
	 * Access the parts.
	 * 
	 * @return A List of the parts.
	 */
	List<Part> getParts();

	/**
	 * Access all the part details in a single ordered (by part and sequence) list.<br />
	 * If a part is empty, include a phantom detail for it.
	 * 
	 * @return The list of all part details.
	 */
	List<PartDetail> getPhantomDetails();

	/**
	 * Access one of the questions, by question id.
	 * 
	 * @param questionId
	 *        The question id.
	 * @return the question, or null if not found.
	 */
	Question getQuestion(String questionId);

	/**
	 * Access the questions across all parts in delivery order.
	 * 
	 * @return The questions across all parts in delivery order.
	 */
	List<Question> getQuestions();

	/**
	 * Access the show-presentation setting; this controls the display of each part's presentation.<br />
	 * If not specifically set, the value will be FALSE if all the parts are defined with no title and no<br />
	 * descriptions, and if the continuous numbering is set to FALSE.
	 * 
	 * @return TRUE to show the part presentations, FALSE to not show them.
	 */
	Boolean getShowPresentation();

	/**
	 * Access the count of parts.
	 * 
	 * @return The count of parts.
	 */
	Integer getSize();

	/**
	 * Access the sum of all possible points for this assessment.
	 * 
	 * @return The sum of all possible points for this assessment.
	 */
	Float getTotalPoints();

	/**
	 * Get a virtual draw for this pool, set to the same count as one of our draws if we have one, else set to 0.<br />
	 * The draw is virtual, not a detail of a Part.
	 * 
	 * @return The virtual PoolDraw for this pool.
	 */
	PoolDraw getVirtualDraw(Pool pool);

	/**
	 * Move the details from wherever part they reside into the destination part.
	 * 
	 * @param detailIds
	 *        The detail ids array.
	 * @param destination
	 *        The destination part.
	 */
	void moveDetails(String[] detailIds, Part destination);

	/**
	 * Remove the detail with this detail id, from any part.
	 * 
	 * @param id
	 *        The detail id.
	 */
	void removeDetail(String id);

	/**
	 * Remove any parts that have no title, no description, and no questions.
	 */
	void removeEmptyParts();

	/**
	 * Remove this part.
	 * 
	 * @param part
	 *        The part to remove.
	 */
	void removePart(Part part);

	/**
	 * Set the continuous numbering flag that controls the numbering of questions across part boundaries.
	 * 
	 * @param setting
	 *        TRUE if numbering is continuous across the part boundaries, FALSE if numbering resets for each part.
	 */
	void setContinuousNumbering(Boolean setting);

	/**
	 * Reorder the existing parts to match this order.<br />
	 * Any parts not listed remain in their order following this list.<br />
	 * Any parts in the list not matching existing parts are ignored.
	 * 
	 * @param partIds
	 *        A list of the part ids in order.
	 */
	void setOrder(String[] detailIds);

	/**
	 * Set the show-presentation setting; this controls the display of each part's presentation.
	 * 
	 * @param setting
	 *        TRUE to show the part presentations, FALSE to not show them.
	 */
	void setShowPresentation(Boolean setting);

	/**
	 * Apply the set of draws in the list to the parts.<br />
	 * If the draw has no count, and the pool is in a part, remove it.<br />
	 * If the draw has a count, and the pool is already drawn in a part, make sure the count matches.<br />
	 * Any additional draws not in parts get added to the indicated part.
	 * 
	 * @param draws
	 *        The list of (virtual) draws.
	 * @param partForNewDraws
	 *        The part to get any new draws.
	 */
	void updateDraws(List<PoolDraw> draws, Part partForNewDraws);
}
