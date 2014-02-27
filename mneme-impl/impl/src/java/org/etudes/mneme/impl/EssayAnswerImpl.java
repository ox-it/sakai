/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2013 Etudes, Inc.
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

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.TypeSpecificAnswer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.util.StringUtil;

/**
 * EssayAnswerImpl handles answers for the essay question type.
 */
public class EssayAnswerImpl implements TypeSpecificAnswer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(EssayAnswerImpl.class);

	/** The answer this is a helper for. */
	protected transient Answer answer = null;

	/** The String answer as entered by the user. */
	protected String answerData = null;

	/** The String answer as entered by the user and subsequently marked up by an evaluation. */
	protected String answerEvaluated = null;

	/** Dependency: AttachmentService. */
	protected AttachmentService attachmentService = null;

	/** Set when the answer has been changed. */
	protected boolean changed = false;

	/** The uploaded file references. */
	protected List<Reference> uploads = new ArrayList<Reference>();

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 */
	public EssayAnswerImpl(Answer answer, AttachmentService attachmentService)
	{
		this.answer = answer;
		this.attachmentService = attachmentService;
	}

	/**
	 * Construct.
	 * 
	 * @param answer
	 *        The answer this is a helper for.
	 * @param other
	 *        The other to copy.
	 */
	public EssayAnswerImpl(Answer answer, EssayAnswerImpl other)
	{
		this.answer = answer;
		this.answerData = other.answerData;
		this.answerEvaluated = other.answerEvaluated;
		this.uploads.addAll(other.uploads);
		this.changed = other.changed;
		this.attachmentService = other.attachmentService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearIsChanged()
	{
		this.changed = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone(Answer answer)
	{
		try
		{
			// get an exact, bit-by-bit copy
			Object rv = super.clone();

			// deep copy
			((EssayAnswerImpl) rv).uploads = new ArrayList<Reference>(this.uploads);

			((EssayAnswerImpl) rv).answer = answer;

			return rv;
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void consolidate(String destination)
	{
		// check for remove (delivery)
		if (destination.startsWith("STAY_REMOVE:"))
		{
			String[] parts = StringUtil.splitFirst(destination, ":");
			if (parts.length == 2)
			{
				String[] parts2 = StringUtil.splitFirst(parts[1], ":");
				if (parts2.length == 2)
				{
					String refStr = parts2[1];
					for (Iterator<Reference> i = this.uploads.iterator(); i.hasNext();)
					{
						Reference ref = i.next();
						if (ref.getReference().equals(refStr))
						{
							i.remove();
							this.attachmentService.removeAttachment(ref);

							this.changed = true;
						}
					}
				}
			}
		}

		// check for delete annotation (grading)
		else if (destination.startsWith("STAY_DELETE_ANNOTATION:"))
		{
			String[] parts = StringUtil.splitFirst(destination, ":");
			if (parts.length == 2)
			{
				// if our id is selected, clear our annotation
				String aid = parts[1];
				if (this.answer.getId().equals(aid))
				{
					this.answerEvaluated = null;
					this.changed = true;
				}
			}
		}
	}

	/**
	 * @return The answerData.
	 */
	public String getAnswerData()
	{
		return this.answerData;
	}

	/**
	 * @return The answer data as evaluated.
	 */
	public String getAnswerEvaluated()
	{
		// if we have no evaluation yet, start with the raw answer data
		if (this.answerEvaluated == null)
		{
			return this.answerData;
		}

		return this.answerEvaluated;
	}

	/**
	 * @return The answer data as evaluated, and specially formatted
	 */
	public String getAnswerEvaluatedFormatted()
	{
		// if we have no evaluation yet, start with the raw answer data
		if (this.answerEvaluated == null)
		{
			return this.answerData;
		}

		// format the evaluation
		return EvaluationImpl.formatEvaluation(this.answerEvaluated);
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getAutoScore()
	{
		// there is no auto scoring for essays, unless they are unanswered
		if (!this.getIsAnswered().booleanValue()) return Float.valueOf(0f);

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getCompletelyCorrect()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPartiallyCorrect()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getData()
	{
		String[] rv = new String[2 + this.uploads.size()];
		rv[0] = this.answerData;
		rv[1] = this.answerEvaluated;
		int i = 2;
		for (Reference ref : this.uploads)
		{
			rv[i++] = ref.getReference();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsAnswered()
	{
		return ((((EssayQuestionImpl) this.answer.getQuestion().getTypeSpecificQuestion()).getSubmissionType() == EssayQuestionImpl.SubmissionType.none)
				|| (this.answerData != null) || (!this.uploads.isEmpty()));
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsChanged()
	{
		return this.changed;
	}

	/**
	 * Access the already uploaded items as attachment references.
	 * 
	 * @return The List of References to the already uploaded items.
	 */
	public List<Reference> getUploaded()
	{
		return new ArrayList<Reference>(this.uploads);
	}

	/**
	 * Set the answerData
	 * 
	 * @param answerData
	 *        . Must be well formed HTML or plain text.
	 */
	public void setAnswerData(String answerData)
	{
		answerData = StringUtil.trimToNull(answerData);

		this.answerData = answerData;
		this.changed = true;
	}

	/**
	 * Set the answer data marked up with evaluation
	 * 
	 * @param answerData
	 *        . Must be well formed HTML or plain text.
	 */
	public void setAnswerEvaluated(String evaluated)
	{
		evaluated = StringUtil.trimToNull(evaluated);

		this.answerEvaluated = evaluated;
		this.changed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setData(String[] data)
	{
		this.answerData = null;
		this.answerEvaluated = null;
		this.uploads.clear();

		if ((data != null) && (data.length > 0))
		{
			this.answerData = data[0];

			// if data[1] is not a reference, it is the answerEvaluated (references must start with a slash)
			if (data.length > 1)
			{
				int start = 1;
				if ((data[1] == null) || (!data[1].startsWith("/")))
				{
					this.answerEvaluated = data[1];
					start = 2;
				}

				for (int index = start; index < data.length; index++)
				{
					Reference ref = this.attachmentService.getReference(data[index]);
					uploads.add(ref);
				}
			}
		}
	}

	/**
	 * Accept a file upload from the user.
	 * 
	 * @param file
	 *        The file.
	 */
	public void setUpload(FileItem file)
	{
		// put the attachment in a unique folder to avoid name conflicts (no thumbs for images)
		Reference reference = this.attachmentService.addAttachment(AttachmentService.MNEME_APPLICATION, this.answer.getSubmission().getAssessment()
				.getContext(), AttachmentService.SUBMISSIONS_AREA + "/" + this.answer.getSubmission().getId(),
				AttachmentService.NameConflictResolution.alwaysUseFolder, file, false, AttachmentService.REFERENCE_ROOT);
		if (reference != null)
		{
			this.uploads.add(reference);
			this.changed = true;
		}
	}
}
