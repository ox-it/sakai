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

package org.etudes.ambrosia.impl;

import java.io.PrintWriter;
import java.util.List;

import org.etudes.ambrosia.api.BarChart;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;

/**
 * UiBarChart implements BarChart
 */
public class UiBarChart extends UiComponent implements BarChart
{
	/** Data samples to chart. */
	protected PropertyReference data = null;

	/** Height in pixels of the chart frame. */
	protected int height = 300;

	/** Extra data sample to mark. */
	protected PropertyReference mark = null;

	/** The message selector for the title. */
	protected Message title = null;

	/** Width in pixels of the chart frame. */
	protected int width = 400;

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		PrintWriter response = context.getResponseWriter();

		// generate id
		// int id = context.getUniqueId();

		// get data
		Double[] data = null;
		if (this.data != null)
		{
			Object o = this.data.readObject(context, focus);
			if (o != null)
			{
				if (o instanceof List)
				{
					data = new Double[((List) o).size()];
					for (int i = 0; i < data.length; i++)
					{
						Object e = ((List) o).get(i);
						if (e instanceof Double)
						{
							data[i] = (Double) e;
						}
						else if (e instanceof Float)
						{
							data[i] = new Double(((Float) e).doubleValue());
						}
						else if (e instanceof Integer)
						{
							data[i] = new Double(((Integer) e).doubleValue());
						}
					}
				}

				else if (o.getClass().isArray())
				{
					data = (Double[]) o;
				}

				else if (o instanceof String)
				{
					String[] parts = ((String) o).split(",");
					data = new Double[parts.length];
					for (int i = 0; i < parts.length; i++)
					{
						data[i] = Double.parseDouble(parts[i]);
					}
				}
			}
		}

		// give each data item an equal share of the width
		int dataWidth = this.width / data.length;

		// TODO: deal with dataWidth < 1 or so

		// let the bigest data value match our hight
		double max = 0;
		for (Double d : data)
		{
			if (d > max) max = d;
		}

		// to adjust the data value to height
		double heightAdjust = ((double) this.height) / max;

		// read the mark index, if any
		Integer markIndex = null;
		if (this.mark != null)
		{
			String value = this.mark.read(context, focus);
			if (value != null)
			{
				markIndex = new Integer(value);
			}
		}

		// our frame
		response.println("<div style=\"clear:both; overflow:hidden; width:" + this.width + "px; height:" + this.height
				+ "px; border-bottom:solid #000000; border-left:solid #000000;\">");

		// our contrasting colors
		String color1 = "#C0C0C0";
		String color2 = "#808080";
		String markColor = "#0000CC";
		boolean color = true;

		// the data
		int index = 0;
		for (Double d : data)
		{
			// adjust the 0 height to have at least a pixel, else they don't show.
			int h = ((int) (d * heightAdjust));
			if (h < 1) h = 1;

			String colorToUse = (color ? color1 : color2);
			color = !color;

			// if this is the marked index, change color
			if (markIndex != null)
			{
				if (markIndex.intValue() == index)
				{
					colorToUse = markColor;
				}
			}

			response.println("<div style=\"width:" + dataWidth + "px; height:" + h + "px; background-color:" + colorToUse
					+ "; float:left; position:relative; top:" + (this.height - h) + "px;\"></div>");

			index++;
		}

		response.println("</div>");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public BarChart setData(PropertyReference data)
	{
		this.data = data;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public BarChart setHeight(int height)
	{
		this.height = height;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public BarChart setMarkIndex(PropertyReference data)
	{
		this.mark = data;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public BarChart setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public BarChart setWidth(int width)
	{
		this.width = width;
		return this;
	}
}
