package org.sakaiproject.content.impl;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Matthew Buckett
 */
public class TestHtmlPageFilter {

	private HtmlPageFilter filter;
	private ContentResource cr;

	@Before
	public void setUp() throws Exception {
		filter = new HtmlPageFilter();
		filter.setEnabled(true);
		cr = mock(ContentResource.class);
	}

	private void make(String type, String addHtml) {
		BaseResourcePropertiesEdit edit = new BaseResourcePropertiesEdit();
		when(cr.getContentType()).thenReturn(type);
		when(cr.getProperties()).thenReturn(edit);
		edit.addProperty(ResourceProperties.PROP_ADD_HTML, addHtml);
	}

	private void makeGood() {
		make("text/html", "yes");
	}

	@Test
	public void testIsFilter() {
		makeGood();
		assertTrue(filter.isFiltered(cr));
	}

	@Test
	public void testIsFilteredDisabled() {
		filter.setEnabled(false);
		makeGood();
		assertFalse(filter.isFiltered(cr));
	}

	@Test
	public void testIsFilteredNo() {
		make("text/html", "no");
		assertFalse(filter.isFiltered(cr));
	}

	@Test
	public void testIsFilteredYes() {
		make("text/html", "yes");
		assertTrue(filter.isFiltered(cr));
	}

	@Test
	public void testIsFilteredAuto() {
		make("text/html", "auto");
		assertTrue(filter.isFiltered(cr));
	}

	@Test
	public void testIsFilteredStandards() {
		make("text/html", "standards");
		assertTrue(filter.isFiltered(cr));
	}

	@Test
	public void testIsFilteredNull() {
		make("text/html", null);
		assertTrue(filter.isFiltered(cr));
	}

	@Test
	public void testIsFilteredPlainText() {
		make("text/plain", "yes");
		assertFalse(filter.isFiltered(cr));
	}




}
