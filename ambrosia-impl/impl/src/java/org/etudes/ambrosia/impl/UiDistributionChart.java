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

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.DistributionChart;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;

/**
 * UiDistributionChart implements DistributionChart
 */
public class UiDistributionChart extends UiComponent implements DistributionChart
{
	/** percent clumping - number of percents per bucket. */
	protected int clump = 1;

	/** Data samples to chart. */
	protected PropertyReference data = null;

	/** Height in pixels of the chart frame. */
	protected int height = 300;

	/** Data sample to mark. */
	protected PropertyReference mark = null;

	/** Maximum for any data (used to compute percents). */
	protected PropertyReference maxValue = null;

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

		// get raw data values
		Double[] values = null;
		if (this.data != null)
		{
			Object o = this.data.readObject(context, focus);
			if (o != null)
			{
				if (o instanceof List)
				{
					values = new Double[((List) o).size()];
					for (int i = 0; i < values.length; i++)
					{
						Object e = ((List) o).get(i);
						if (e instanceof Double)
						{
							values[i] = (Double) e;
						}
						else if (e instanceof Float)
						{
							values[i] = new Double(((Float) e).doubleValue());
						}
						else if (e instanceof Integer)
						{
							values[i] = new Double(((Integer) e).doubleValue());
						}
					}
				}

				else if (o.getClass().isArray())
				{
					values = (Double[]) o;
				}

				else if (o instanceof String)
				{
					String[] parts = ((String) o).split(",");
					values = new Double[parts.length];
					for (int i = 0; i < parts.length; i++)
					{
						values[i] = Double.parseDouble(parts[i]);
					}
				}
			}
		}

		// read the mark value, if any
		Double markValue = null;
		if (this.mark != null)
		{
			String value = this.mark.read(context, focus);
			if (value != null)
			{
				markValue = new Double(value);
			}
		}

		// read the max value
		Double maxValue = null;
		if (this.maxValue != null)
		{
			String value = this.maxValue.read(context, focus);
			if (value != null)
			{
				maxValue = new Double(value);
			}
		}

		// not set, take the max from the sample
		if (maxValue == null)
		{
			if (values != null)
			{
				double m = 0;
				for (Double d : values)
				{
					if (d.doubleValue() > m) m = d.doubleValue();
				}
				maxValue = new Double(m);
			}

			// no sample, set it to 0
			else
			{
				maxValue = new Double(0);
			}
		}

		// convert the raw values to %s based on the max value
		// collect them in the percent count buckets, 0 .. 100, clumped
		int buckets = (100 / this.clump) + 1;
		Double[] data = new Double[buckets];
		for (int i = 0; i < buckets; i++)
		{
			data[i] = new Double(0);
		}

		if (values != null)
		{
			for (Double v : values)
			{
				// convert to pct
				int index = (int) ((100d * v.doubleValue()) / maxValue.doubleValue());
				if (index < 0)
				{
					index = 0;
				}
				if (index > 100)
				{
					index = 100;
				}

				// which bucket
				int bucket = index / this.clump;

				// increment that bucket
				int count = data[bucket].intValue();
				count++;
				data[bucket] = new Double(count);
			}
		}

		// mark this one's score
		Integer markIndex = null;
		if (markValue != null)
		{
			int m = (int) ((100d * markValue.doubleValue()) / maxValue.doubleValue());
			if (m < 0) m = 0;
			if (m > 100) m = 100;

			markIndex = new Integer(m / this.clump);
		}

		// give each data item an equal share of the width
		int dataWidth = this.width / data.length;

		// TODO: deal with dataWidth < 1 or so

		// the chart height will accomodate a single count containing the entire sample
		double heightAdjust = 0;
		if (values != null)
		{
			heightAdjust = ((double) this.height) / values.length;
		}

		// else pick something
		else
		{
			heightAdjust = ((double) this.height) / 10;
		}

		// our frame
		response.println("<div style=\"clear:both; overflow:hidden; width:" + this.width + "px; height:" + this.height
				+ "px; border-bottom:thin solid #000000; border-left:thin solid #000000;\">");

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
			int h = ((int) (d.doubleValue() * heightAdjust));

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

			// make the 0 values reserve space but render not visibly
			if (h < 1)
			{
				h = 1;
				// TODO: we need to know the background color...
				colorToUse = "#FFFFFF";
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
	public DistributionChart setClump(int clump)
	{
		this.clump = clump;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart setData(PropertyReference data)
	{
		this.data = data;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart setHeight(int height)
	{
		this.height = height;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart setMark(PropertyReference data)
	{
		this.mark = data;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart setMax(PropertyReference data)
	{
		this.maxValue = data;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart setTitle(String selector, PropertyReference... references)
	{
		this.title = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart setWidth(int width)
	{
		this.width = width;
		return this;
	}
}
