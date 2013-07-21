/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009, 2013 Etudes, Inc.
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

package org.etudes.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.etudes.util.api.Translation;

/**
 * TranslationImpl implements Translation
 */
public class TranslationImpl implements Translation
{
	/** The from value. */
	protected String from = null;

	/** The to value. */
	protected String to = null;

	/**
	 * Construct.
	 * 
	 * @param from
	 *        The from value.
	 * @param to
	 *        The to value.
	 */
	public TranslationImpl(String from, String to)
	{
		this.from = from;
		this.to = to;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFrom()
	{
		return this.from;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTo()
	{
		return this.to;
	}

	/**
	 * {@inheritDoc}
	 */
	public String reverseTranslate(String target)
	{
		if (target == null) return null;
		if (this.from == null) return target;
		if (this.to == null) return target;

		String rv = Pattern.compile(this.to.toString(), Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(target)
				.replaceAll(Matcher.quoteReplacement(this.from.toString()));

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFrom(String from)
	{
		this.from = from;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTo(String to)
	{
		this.to = to;
	}

	/**
	 * {@inheritDoc}
	 */
	public String translate(String target)
	{
		if (target == null) return null;
		if (this.from == null) return target;
		if (this.to == null) return target;

		String rv = Pattern.compile(this.from.toString(), Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(target)
				.replaceAll(Matcher.quoteReplacement(this.to.toString()));

		return rv;
	}
}
