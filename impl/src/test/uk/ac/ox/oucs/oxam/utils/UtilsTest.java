package uk.ac.ox.oucs.oxam.utils;

import static uk.ac.ox.oucs.oxam.utils.Utils.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testJoinPaths() {
		assertEquals("", joinPaths("", ""));
		assertEquals("/", joinPaths("", "/"));
		assertEquals("", joinPaths("/", ""));
		
		assertEquals("/dir/file.txt", joinPaths("/", "/dir", "/file.txt"));
		assertEquals("dir/file.txt", joinPaths("/", "dir", "/file.txt"));
		assertEquals("dir/file.txt", joinPaths("/", "dir", "file.txt"));
	}

}
