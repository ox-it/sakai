package uk.ac.ox.oucs.oxam.readers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import uk.ac.ox.oucs.oxam.readers.SheetExporter.Order;


public class OrderTest {

	@Test
	public void testSimple() {
		Order one = new Order(1);
		Order two = new Order(2);
		List<Order> list = new ArrayList<Order>();
		list.add(two);
		list.add(one);
		Collections.sort(list);
		assertEquals(one, list.get(0));
		assertEquals(two, list.get(1));
	}
	
	@Test
	public void testComplex() {
		Order one = new Order(1);
		Order two = new Order(2);
		Order twoOne = new Order(two, 1);
		Order twoTwo = new Order(two, 2);
		Order twoFour = new Order(two, 4);
		Order twoTwoTwo = new Order(twoTwo, 2);
		List<Order> list = new ArrayList<Order>();
		list.add(twoTwoTwo);
		list.add(twoFour);
		list.add(twoOne);
		list.add(one);
		Collections.sort(list);
		assertEquals(one, list.get(0));
		assertEquals(twoFour, list.get(list.size()-1));
	}
	
	@Test
	public void testOther() {
		Set<Order> set = new TreeSet<Order>();
		set.add(new Order("17.1"));
		set.add(new Order("1.2"));
		set.add(new Order("1.7"));
		Iterator<Order> it = set.iterator();
		assertEquals("1.2", it.next().toString());
		assertEquals("1.7", it.next().toString());
		assertEquals("17.1", it.next().toString());
	}

}
