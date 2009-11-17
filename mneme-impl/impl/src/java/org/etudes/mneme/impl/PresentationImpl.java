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

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.etudes.mneme.api.Changeable;
import org.etudes.mneme.api.Presentation;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.util.StringUtil;

/**
 * PresentationImpl implements Presentation
 */
public class PresentationImpl implements Presentation
{
	protected List<Reference> attachments = new ArrayList<Reference>();

	protected transient Changeable owner = null;

	protected String text = null;

	/**
	 * Construct.
	 */
	public PresentationImpl(Changeable owner)
	{
		this.owner = owner;
	}

	/**
	 * Construct.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	public PresentationImpl(PresentationImpl other, Changeable owner)
	{
		this(owner);
		set(other);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAttachment(Reference reference)
	{
		this.attachments.add(reference);
		if (this.owner != null) this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Reference> getAttachments()
	{
		return this.attachments;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsEmpty()
	{
		return ((text == null) && attachments.isEmpty());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText()
	{
		return this.text;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAttachment(Reference reference)
	{
		for (Iterator i = this.attachments.iterator(); i.hasNext();)
		{
			Reference ref = (Reference) i.next();
			if (ref.getReference().equals(reference.getReference()))
			{
				i.remove();
				if (this.owner != null) this.owner.setChanged();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttachments(List<Reference> references)
	{
		this.attachments = new ArrayList<Reference>();
		if (references != null)
		{
			this.attachments.addAll(references);
		}

		if (this.owner != null) this.owner.setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setText(String text)
	{
		text = StringUtil.trimToNull(text);

		if (!Different.different(this.text, text)) return;

		this.text = text;

		if (this.owner != null) this.owner.setChanged();
	}

	/**
	 * Set as a copy of the other.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	protected void set(PresentationImpl other)
	{
		this.attachments = new ArrayList<Reference>(other.attachments.size());
		this.attachments.addAll(other.attachments);
		this.text = other.text;
	}
}
