package org.sakaiproject.gradebookng.business.util;

import org.apache.commons.lang.time.StopWatch;

import lombok.extern.slf4j.Slf4j;

/**
 * Stopwatch extension that times pieces of the logic to determine impact on any modifications.
 */
@Slf4j
public class GbStopWatch extends StopWatch
{
	private String context;
	
	public GbStopWatch()
	{
		this("");
	}
	
	public GbStopWatch(String context)
	{
		this.context = context;
		start();
	}
	
	public void time(final String msg)
	{
		if (context.isEmpty())
		{
			stop();
			log.debug("Time for [" + msg + "] was: " + getTime() + "ms");
			reset();
			start();
		}
		else
		{
			timeWithContext(context, msg);
		}
	}

	public void timeWithContext(final String ctx, final String msg)
	{
		stop();
		log.debug("Time for [" + ctx + "].[" + msg + "] was: " + getTime() + "ms");
		reset();
		start();
	}
}
