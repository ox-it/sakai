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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test Shuffling.
 */
public class ShuffleTest extends TestCase
{
	/** Logger. */
	private static final Log log = LogFactory.getLog(ShuffleTest.class);

	/**
	 * @param arg0
	 */
	public ShuffleTest(String arg0)
	{
		super(arg0);
	}

	protected List<String> shuffle1(List<String> seq, int draw, int unique)
	{
		long seed = Integer.toString(unique).hashCode();
		// System.out.println(unique + " - " + seed);
		// long seed = unique;

		List<String> rv = new ArrayList<String>(seq);
		Collections.shuffle(rv, new Random(seed));

		if (draw < rv.size())
		{
			rv = rv.subList(0, draw);
		}
		return rv;
	}
	
	protected List<String> shuffle1b(List<String> seq, int draw, int unique)
	{
		long seed = Integer.toString(unique).hashCode();
		// System.out.println(unique + " - " + seed);
		// long seed = unique;

		List<String> rv = new ArrayList<String>(seq);
		if (rv.size() == 2) rv.add(null);
		Collections.shuffle(rv, new Random(seed));
		while (rv.remove(null));
		if (draw < rv.size())
		{
			rv = rv.subList(0, draw);
		}
		return rv;
	}


	protected List<String> shuffle2(List<String> seq, int draw, int unique)
	{
		long seed = Integer.toString(unique).hashCode();
		Random rnd = new Random(seed);
		List<String> rv = new ArrayList<String>();
		for (int i = 0; i < draw; i++)
		{
			int x = rnd.nextInt(seq.size()) + 1;
			rv.add(Integer.toString(x));
		}

		return rv;
	}

	protected List<String> shuffle3(List<String> seq, int draw, int unique)
	{
		long seed = Integer.toString(unique).hashCode();
		Random rnd = new Random(seed);

		List<String> rv = new ArrayList<String>(seq);
		for (int i = 0; i < seq.size(); i++)
		{
			// pick a random other position to swap with
			int x = rnd.nextInt(seq.size());
			if (x != i)
			{
				String hold = rv.get(x);
				rv.set(x, rv.get(i));
				rv.set(i, hold);
			}
		}

		if (draw < rv.size())
		{
			rv = rv.subList(0, draw);
		}
		return rv;
	}

	protected List<String> shuffle4(List<String> seq, int draw, int unique)
	{
		long seed = Integer.toString(unique).hashCode();
		Random rnd = new Random(seed);

		List<String> rv = new ArrayList<String>(seq);

		// pick a random to go forward or backward
		if (rnd.nextBoolean())
		{
			for (int i = 0; i < seq.size(); i++)
			{
				// pick a random other position to swap with
				int x = rnd.nextInt(seq.size());
				if (x != i)
				{
					String hold = rv.get(x);
					rv.set(x, rv.get(i));
					rv.set(i, hold);
				}
			}
		}
		else
		{
			for (int i = seq.size()-1; i >=0; i--)
			{
				// pick a random other position to swap with
				int x = rnd.nextInt(seq.size());
				if (x != i)
				{
					String hold = rv.get(x);
					rv.set(x, rv.get(i));
					rv.set(i, hold);
				}
			}
		}
		if (draw < rv.size())
		{
			rv = rv.subList(0, draw);
		}
		return rv;
	}

	protected int npk(int n, int k)
	{
		int last = n - k;
		int rv = n--;
		while (n > last)
		{
			rv = rv * n--;
		}
		return rv;
	}

	public void testxxx() throws Exception
	{
		int size = 2;
		int draw = 1;
		int sample = 50;
		// int unique = 52134;
		// int unique = 0;
		int unique = 107341;
		//Random uniqueRnd = new Random(0);

		// setup a list
		List<String> seq = new ArrayList<String>();
		for (int i = 1; i <= size; i++)
		{
			seq.add(Integer.toString(i));
		}

		Map<List<String>, Integer> results = new HashMap<List<String>, Integer>();

		// run through the size
		for (int i = 0; i < sample; i++)
		{
			// shuffle
			List<String> shufled = shuffle1b(seq, draw, unique++);
			//unique += uniqueRnd.nextInt();

			// find the results
			Integer count = results.get(shufled);
			if (count == null)
			{
				count = new Integer(1);
			}
			else
			{
				count = new Integer(count.intValue() + 1);
			}

			results.put(shufled, count);
		}

		System.out.println("size: " + size + "  draw: " + draw + "  samples: " + sample + "  nPk: " + npk(size, draw) + "  seq: " + seq);
		System.out.println(results.size() + " permutations generated");
		for (Map.Entry<List<String>, Integer> entry : results.entrySet())
		{
			List<String> list = entry.getKey();
			Integer count = entry.getValue();
			System.out.println(count.toString() + " : " + list.toString());
		}
	}

	/**
	 * @param arg0
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @param arg0
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
}
