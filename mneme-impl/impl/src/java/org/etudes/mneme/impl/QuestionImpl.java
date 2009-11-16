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

package org.etudes.mneme.impl;

import java.util.List;

import org.etudes.mneme.api.Attribution;
import org.etudes.mneme.api.Ordering;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolGetService;
import org.etudes.mneme.api.Presentation;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionGetService;
import org.etudes.mneme.api.QuestionPick;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionUnscoredQuestionService;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;

/**
 * QuestionImpl implements Question
 */
public class QuestionImpl implements Question
{
	public class MyAssessmentOrdering implements Ordering<Question>
	{
		protected QuestionImpl question = null;

		public MyAssessmentOrdering(QuestionImpl question)
		{
			this.question = question;
		}

		/**
		 * {@inheritDoc}
		 */
		public Boolean getIsFirst()
		{
			if (question.partContext == null) return true;

			if (!question.getPart().getOrdering().getIsFirst()) return false;

			return question.getPartOrdering().getIsFirst();
		}

		/**
		 * {@inheritDoc}
		 */
		public Boolean getIsLast()
		{
			if (question.partContext == null) return true;

			if (!question.getPart().getOrdering().getIsLast()) return false;

			return question.getPartOrdering().getIsLast();
		}

		/**
		 * {@inheritDoc}
		 */
		public Question getNext()
		{
			if (question.partContext == null) return null;

			Question rv = question.getPartOrdering().getNext();
			if (rv != null) return rv;

			Part part = question.getPart().getOrdering().getNext();
			if (part == null) return null;

			return part.getFirstQuestion();
		}

		/**
		 * {@inheritDoc}
		 */
		public Integer getPosition()
		{
			if (question.partContext == null) return new Integer(1);

			// position in this part
			int pos = question.getPartOrdering().getPosition();

			// count up questions in preceeding parts
			for (Part part : question.getPart().getAssessment().getParts().getParts())
			{
				if (part.equals(question.partContext)) break;
				pos += part.getNumQuestions();
			}

			return pos;
		}

		/**
		 * {@inheritDoc}
		 */
		public Question getPrevious()
		{
			if (question.partContext == null) return null;

			Question rv = question.getPartOrdering().getPrevious();
			if (rv != null) return rv;

			Part part = question.getPart().getOrdering().getPrevious();
			if (part == null) return null;

			return part.getLastQuestion();
		}
	}

	public class MyPartOrdering implements Ordering<Question>
	{
		protected QuestionImpl question = null;

		public MyPartOrdering(QuestionImpl question)
		{
			this.question = question;
		}

		/**
		 * {@inheritDoc}
		 */
		public Boolean getIsFirst()
		{
			if (question.partContext == null) return true;

			List<QuestionPick> questions = ((PartImpl) question.getPart()).getQuestionPickOrder();
			if (question.getId().equals(questions.get(0).getQuestionId())) return true;

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Boolean getIsLast()
		{
			if (question.partContext == null) return true;

			List<QuestionPick> questions = ((PartImpl) question.getPart()).getQuestionPickOrder();
			if (question.getId().equals(questions.get(questions.size() - 1).getQuestionId())) return true;

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Question getNext()
		{
			if (question.partContext == null) return null;

			List<QuestionPick> questions = ((PartImpl) question.getPart()).getQuestionPickOrder();
			int index = 0;
			for (QuestionPick pick : questions)
			{
				if (pick.getQuestionId().equals(question.getId()))
				{
					break;
				}
				index++;
			}
			if (index == questions.size() - 1) return null;

			// TODO: set the question context (pool? from question?)
			return question.questionService.getQuestion(questions.get(index + 1).getQuestionId());
		}

		/**
		 * {@inheritDoc}
		 */
		public Integer getPosition()
		{
			if (question.partContext == null) return new Integer(1);

			List<QuestionPick> questions = ((PartImpl) question.getPart()).getQuestionPickOrder();
			int index = 0;
			for (QuestionPick pick : questions)
			{
				if (pick.getQuestionId().equals(question.getId()))
				{
					break;
				}
				index++;
			}

			return index + 1;
		}

		/**
		 * {@inheritDoc}
		 */
		public Question getPrevious()
		{
			if (question.partContext == null) return null;

			List<QuestionPick> questions = ((PartImpl) question.getPart()).getQuestionPickOrder();
			int index = 0;
			for (QuestionPick pick : questions)
			{
				if (pick.getQuestionId().equals(question.getId()))
				{
					break;
				}
				index++;
			}
			if (index == 0) return null;

			// TODO: set context (pool? from question?)
			return question.questionService.getQuestion(questions.get(index - 1).getQuestionId());
		}
	}

	protected MyAssessmentOrdering assessmentOrdering = new MyAssessmentOrdering(this);

	/** Track changes. */
	protected transient ChangeableImpl changed = new ChangeableImpl();

	protected String context = null;

	protected AttributionImpl createdBy = new AttributionImpl(null);

	protected Boolean explainReason = Boolean.FALSE;

	protected String feedback = null;

	protected String hints = null;

	protected Boolean historical = Boolean.FALSE;

	protected String id = null;

	/** Stays TRUE until an end-user change to the object occurs, showing it was actually initially set. */
	protected Boolean mint = Boolean.TRUE;

	protected AttributionImpl modifiedBy = new AttributionImpl(null);

	protected transient Part partContext = null;

	/** the PartDetail context for this instance of the question. */
	protected transient PartDetail partDetailContext = null;

	protected MyPartOrdering partOrdering = new MyPartOrdering(this);

	protected String poolId = null;

	/** Dependency: PoolGetService. */
	protected transient PoolGetService poolService = null;

	protected PresentationImpl presentation = null;

	protected TypeSpecificQuestion questionHandler = null;

	/** Dependency: QuestionGetService. */
	protected transient QuestionGetService questionService = null;

	protected transient Submission submissionContext = null;

	/** Dependency: SubmissionUnscoredQuestionService. */
	protected transient SubmissionUnscoredQuestionService submissionService = null;

	protected Boolean survey = Boolean.FALSE;

	protected boolean surveyChanged = false;

	protected String type = null;

	/**
	 * Construct.
	 */
	public QuestionImpl()
	{
		this.presentation = new PresentationImpl(this.changed);
	}

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public QuestionImpl(QuestionImpl other)
	{
		set(other);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		// two PartImpls are equals if they have the same id
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		if ((this.id == null) || (((QuestionImpl) obj).id == null)) return false;
		return this.id.equals(((QuestionImpl) obj).id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Ordering<Question> getAssessmentOrdering()
	{
		return this.assessmentOrdering;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContext()
	{
		return this.context;
	}

	/**
	 * {@inheritDoc}
	 */
	public Attribution getCreatedBy()
	{
		return this.createdBy;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		String rv = this.questionHandler.getDescription();
		if (rv == null) return rv;

		// strip any html
		rv = FormattedText.convertFormattedTextToPlaintext(rv);

		// trim to max length
		if (rv.length() > 255) rv = rv.substring(0, 255);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getExplainReason()
	{
		return this.explainReason;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFeedback()
	{
		return this.feedback;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasCorrect()
	{
		// survey marked questions do not support correct answers
		if (this.survey) return Boolean.FALSE;

		// otherwise check with the type handler
		return this.questionHandler.getHasCorrect();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasPoints()
	{
		// survey marked questions do not support points
		if (this.survey) return Boolean.FALSE;

		// otherwise check with the type handler
		return this.questionHandler.getHasPoints();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getHasUnscoredSubmissions()
	{
		// survey questions do not have unscored submissions
		if (this.survey.booleanValue()) return Boolean.FALSE;

		if (this.partContext != null)
		{
			return this.submissionService.getAssessmentQuestionHasUnscoredSubmissions(this.partContext.getAssessment(), this);
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHints()
	{
		return this.hints;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsChanged()
	{
		return this.changed.getChanged() || this.surveyChanged;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsHistorical()
	{
		return this.historical;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsSurvey()
	{
		// if marked survey
		if (this.survey) return Boolean.TRUE;

		// if not, be survey if the this type always is
		return this.questionHandler.getIsSurvey();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsValid()
	{
		return this.questionHandler.getIsValid();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMint()
	{
		return this.mint;
	}

	/**
	 * {@inheritDoc}
	 */
	public Attribution getModifiedBy()
	{
		return this.modifiedBy;
	}

	/**
	 * {@inheritDoc}
	 */
	public Part getPart()
	{
		return this.partContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public Ordering<Question> getPartOrdering()
	{
		return this.partOrdering;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getPoints()
	{
		if (!getHasPoints()) return Float.valueOf(0f);

		if (this.partDetailContext != null)
		{
			// get the points from the detail
			Float partPoints = this.partDetailContext.getQuestionPoints();
			if (partPoints != null)
			{
				return partPoints;
			}
		}

		// use the pool value if not overridden in a detail
		return getPool().getPoints();
	}

	/**
	 * {@inheritDoc}
	 */
	public Pool getPool()
	{
		return this.poolService.getPool(this.poolId);
	}

	/**
	 * {@inheritDoc}
	 */
	public Presentation getPresentation()
	{
		return this.presentation;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTypeName()
	{
		return this.questionHandler.getPlugin().getTypeName();
	}

	/**
	 * {@inheritDoc}
	 */
	public TypeSpecificQuestion getTypeSpecificQuestion()
	{
		return this.questionHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return this.id == null ? "null".hashCode() : this.id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean matches(Question other)
	{
		if (different(this.getExplainReason(), other.getExplainReason())) return Boolean.FALSE;
		if (different(this.getFeedback(), other.getFeedback())) return Boolean.FALSE;
		if (different(this.getHints(), other.getHints())) return Boolean.FALSE;
		if (different(this.getIsSurvey(), other.getIsSurvey())) return Boolean.FALSE;
		if (different(this.getPresentation().getText(), other.getPresentation().getText())) return Boolean.FALSE;
		if (different(this.getType(), other.getType())) return Boolean.FALSE;

		String[] data1 = this.getTypeSpecificQuestion().getData();
		String[] data2 = other.getTypeSpecificQuestion().getData();
		if (data1.length != data2.length) return Boolean.FALSE;
		for (int i = 0; i < data1.length; i++)
		{
			if (different(data1[i], data2[i])) return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChanged()
	{
		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExplainReason(Boolean explainReason)
	{
		if (explainReason == null) throw new IllegalArgumentException();
		if (!Different.different(explainReason, this.explainReason)) return;

		this.explainReason = explainReason;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFeedback(String feedback)
	{
		feedback = StringUtil.trimToNull(feedback);

		if (!Different.different(feedback, this.feedback)) return;

		this.feedback = feedback;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHints(String hints)
	{
		hints = StringUtil.trimToNull(hints);

		if (!Different.different(hints, this.hints)) return;

		this.hints = hints;

		this.changed.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIsSurvey(Boolean isSurvey)
	{
		if (isSurvey == null) throw new IllegalArgumentException();
		if (!Different.different(isSurvey, this.survey)) return;

		this.survey = isSurvey;

		// this.changed.setChanged();
		this.surveyChanged = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPool(Pool pool)
	{
		if (pool == null) throw new IllegalArgumentException();
		if (!Different.different(pool.getId(), this.poolId)) return;

		this.poolId = pool.getId();
		this.context = pool.getContext();

		this.changed.setChanged();
	}

	/**
	 * Dependency: PoolGetService.
	 * 
	 * @param service
	 *        The PoolGetService.
	 */
	public void setPoolService(PoolGetService service)
	{
		this.poolService = service;
	}

	/**
	 * Dependency: QuestionGetService.
	 * 
	 * @param service
	 *        The QuestionGetService.
	 */
	public void setQuestionService(QuestionGetService service)
	{
		this.questionService = service;
	}

	/**
	 * Dependency: SubmissionUnscoredQuestionService.
	 * 
	 * @param service
	 *        The SubmissionUnscoredQuestionService.
	 */
	public void setSubmissionService(SubmissionUnscoredQuestionService service)
	{
		this.submissionService = service;
	}

	/**
	 * Clear the changed settings.
	 */
	protected void clearChanged()
	{
		this.changed.clearChanged();
		this.surveyChanged = false;
	}

	/**
	 * Clear the mint setting.
	 */
	protected void clearMint()
	{
		this.mint = Boolean.FALSE;
	}

	/**
	 * Compare two objects for differences, either may be null
	 * 
	 * @param a
	 *        One object.
	 * @param b
	 *        The other object.
	 * @return true if the object are different, false if they are the same.
	 */
	protected boolean different(Object a, Object b)
	{
		// if both null, they are the same
		if ((a == null) && (b == null)) return false;

		// if either are null (they both are not), they are different
		if ((a == null) || (b == null)) return true;

		// now we know neither are null, so compare
		return (!a.equals(b));
	}

	/**
	 * Check if there was a survey change but no other change
	 * 
	 * @return TRUE if there was a survey change but no other change, FALSE if not.
	 */
	protected Boolean getSurveyOnlyChanged()
	{
		return this.surveyChanged && (!this.changed.getChanged());
	}

	/**
	 * Initialize the context.
	 * 
	 * @param context
	 *        The context.
	 */
	protected void initContext(String context)
	{
		this.context = context;
	}

	/**
	 * Establish the historical flag.
	 * 
	 * @param historical
	 *        The historical flag.
	 */
	protected void initHistorical(Boolean historical)
	{
		this.historical = historical;
	}

	/**
	 * Initialize the id.
	 * 
	 * @param id
	 *        The id.
	 */
	protected void initId(String id)
	{
		this.id = id;
	}

	/**
	 * Establish the mint flag.
	 * 
	 * @param mint
	 *        The mint flag.
	 */
	protected void initMint(Boolean mint)
	{
		this.mint = mint;
	}

	/**
	 * Initialize the part context for this question - the part this question instance was created to support.
	 * 
	 * @param part
	 *        The Part.
	 */
	protected void initPartContext(Part part)
	{
		this.partContext = part;
	}

	/**
	 * Initialize the part detail context for this question - the part detail this question instance was created to support.
	 * 
	 * @param partDetail
	 *        The Part Detail.
	 */
	protected void initPartDetailContext(PartDetail partDetail)
	{
		this.partDetailContext = partDetail;
	}

	/**
	 * Establish the pool id.
	 * 
	 * @param poolId
	 *        The pool id.
	 */
	protected void initPool(String poolId)
	{
		this.poolId = poolId;
	}

	/**
	 * Initialize the Pool id.
	 * 
	 * @param id
	 *        The pool id.
	 */
	protected void initPoolId(String id)
	{
		this.poolId = id;
	}

	/**
	 * Initialize the submission context for this question - the submission this question instance was created to support.
	 * 
	 * @param submission
	 *        The submission.
	 */
	protected void initSubmissionContext(Submission submission)
	{
		this.submissionContext = submission;
	}

	/**
	 * Establish the type.
	 * 
	 * @param type
	 *        The type.
	 */
	protected void initType(String type)
	{
		this.type = type;
	}

	/**
	 * Establish the type-specific question handler.
	 * 
	 * @param questionHandler
	 *        The type-specific question handler.
	 */
	protected void initTypeSpecificQuestion(TypeSpecificQuestion questionHandler)
	{
		this.questionHandler = questionHandler;
	}

	/**
	 * Set this assessment to be "historical" - used only for history by submissions.
	 */
	protected void makeHistorical()
	{
		if (this.historical) return;

		this.historical = Boolean.TRUE;
	}

	protected void set(QuestionImpl other)
	{
		if (other.questionHandler != null) this.questionHandler = (TypeSpecificQuestion) (other.questionHandler.clone(this));
		this.createdBy = new AttributionImpl((AttributionImpl) other.createdBy, null);
		this.context = other.context;
		this.explainReason = other.explainReason;
		this.feedback = other.feedback;
		this.hints = other.hints;
		this.historical = other.historical;
		this.id = other.id;
		this.mint = other.mint;
		this.modifiedBy = new AttributionImpl((AttributionImpl) other.modifiedBy, null);
		this.partDetailContext = other.partDetailContext;
		this.partContext = other.partContext;
		this.poolId = other.poolId;
		this.poolService = other.poolService;
		this.presentation = new PresentationImpl(other.presentation, this.changed);
		this.questionService = other.questionService;
		this.submissionContext = other.submissionContext;
		this.submissionService = other.submissionService;
		this.survey = other.survey;
		this.surveyChanged = other.surveyChanged;
		this.type = other.type;
	}
}
