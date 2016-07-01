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

package org.etudes.ambrosia.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.UserInfoPropertyReference;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
 * UiUserInfoPropertyReference handles user id values by providing some user information for the display.
 */
public class UiUserInfoPropertyReference extends UiPropertyReference implements UserInfoPropertyReference
{
	/** If set, include the eid with the display. */
	protected boolean disambiguate = false;

	/** The user info we want. */
	protected UserInfoPropertyReference.Selector selector = UserInfoPropertyReference.Selector.displayName;

	/**
	 * No-arg constructor.
	 */
	public UiUserInfoPropertyReference()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiUserInfoPropertyReference(UiServiceImpl service, Element xml)
	{
		// property reference stuff
		super(service, xml);

		// selector
		String selector = StringUtil.trimToNull(xml.getAttribute("selector"));
		if ("DISPLAYNAME".equals(selector)) setSelector(Selector.displayName);
		if ("SORTNAME".equals(selector)) setSelector(Selector.sortName);

		// disambiguate
		String disambiguate = StringUtil.trimToNull(xml.getAttribute("disambiguate"));
		if (disambiguate != null)
		{
			setDisambiguate(Boolean.parseBoolean(disambiguate));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return "userInfo";
	}

	/**
	 * {@inheritDoc}
	 */
	public UserInfoPropertyReference setDisambiguate(boolean setting)
	{
		this.disambiguate = setting;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserInfoPropertyReference setSelector(UserInfoPropertyReference.Selector property)
	{
		this.selector = property;
		return this;
	}

	/**
	 * Format the user object using our configured selector.
	 * 
	 * @param user
	 *        The user object.
	 * @return The formatted string
	 */
	protected String fmt(User user)
	{
		String rv = null;
		switch (this.selector)
		{
			case displayName:
			{
				rv = user.getDisplayName();
				break;
			}
			case sortName:
			{
				rv = user.getSortName();
				break;
			}
		}

		if (this.disambiguate)
		{
			rv = rv + " (" + user.getDisplayId().trim() + ")";
		}

		return Validator.escapeHtml(rv);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String format(Context context, Object value)
	{
		if (value == null) return super.format(context, value);

		// deal with a collection of ids
		if (value instanceof Collection)
		{
			Collection ids = (Collection) value;
			List<User> users = UserDirectoryService.getUsers(ids);

			// sort - by user sort name
			if (this.selector == UserInfoPropertyReference.Selector.sortName)
			{
				Collections.sort(users, new Comparator()
				{
					public int compare(Object arg0, Object arg1)
					{
						int rv = ((User) arg0).getSortName().compareTo(((User) arg1).getSortName());
						return rv;
					}
				});
			}

			// or by user display name
			else if (this.selector == UserInfoPropertyReference.Selector.displayName)
			{
				Collections.sort(users, new Comparator()
				{
					public int compare(Object arg0, Object arg1)
					{
						int rv = ((User) arg0).getDisplayName().compareTo(((User) arg1).getDisplayName());
						return rv;
					}
				});
			}

			StringBuilder rv = new StringBuilder();
			for (User user : users)
			{
				rv.append(fmt(user));

				// TODO: offer comma separated horizontal option?
				rv.append("<br />");
			}

			// get rid of the trailing '<br />'
			if (rv.length() > 0)
			{
				rv.setLength(rv.length() - 6);
			}

			return rv.toString();
		}

		// or a single one
		if (!(value instanceof String)) return super.format(context, value);

		try
		{
			User user = UserDirectoryService.getUser((String) value);
			return fmt(user);
		}
		catch (UserNotDefinedException e)
		{
			return Validator.escapeHtml((String) value);
		}
	}
}
