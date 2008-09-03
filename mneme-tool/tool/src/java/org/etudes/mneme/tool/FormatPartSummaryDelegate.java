/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.etudes.mneme.api.DrawPart;
import org.etudes.mneme.api.ManualPart;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolDraw;

/**
 * The "FormatPartSummary" format delegate for the mneme tool.
 */
public class FormatPartSummaryDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(FormatPartSummaryDelegate.class);

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public String format(Context context, Object value)
	{
		// value is a part
		if (value == null) return null;
		if (!(value instanceof Part)) return value.toString();
		Part part = (Part) value;

		if (part instanceof ManualPart)
		{
			// manual-part-summary=Manually Selected Questions ({0})
			Object[] args = new Object[1];
			args[0] = ((ManualPart) part).getNumQuestions().toString();
			return context.getMessages().getFormattedMessage("manual-part-summary", args);
		}

		if (part instanceof DrawPart)
		{
			// draw-part-summary=Random from Pool{0} ({1}): {2}
			DrawPart p = (DrawPart) part;
			Object[] args = new Object[3];

			args[0] = ((p.getDraws().size() == 1) ? "" : "s");
			args[1] = p.getNumQuestions().toString();

			StringBuffer buf = new StringBuffer();
			for (PoolDraw draw : p.getDraws())
			{
				Pool pool = draw.getPool();
				if (pool != null)
				{
					buf.append(pool.getTitle());
					buf.append(", ");
				}
			}
			if (buf.length() > 0) buf.setLength(buf.length() - 2);
			args[2] = buf.toString();

			return context.getMessages().getFormattedMessage("draw-part-summary", args);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object formatObject(Context context, Object value)
	{
		return value;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();
		M_log.info("init()");
	}
}
