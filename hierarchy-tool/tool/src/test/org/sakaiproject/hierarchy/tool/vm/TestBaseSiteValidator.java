package org.sakaiproject.hierarchy.tool.vm;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.web.bind.EscapedErrors;

import junit.framework.TestCase;


public class TestBaseSiteValidator extends TestCase {

	public void testPatterns() {
		
		String pattern = new NewSiteValidator().getPattern();
		
		assertTrue(Pattern.matches(pattern, "good"));
		assertTrue(Pattern.matches(pattern, "a"));
		assertTrue(Pattern.matches(pattern, "st-annes"));
		assertTrue(Pattern.matches(pattern, "st-annes_2"));
		assertFalse(Pattern.matches(pattern, "bad-"));
		assertFalse(Pattern.matches(pattern, "-bad-"));
		assertFalse(Pattern.matches(pattern, "bad-_"));
		assertFalse(Pattern.matches(pattern, "-"));
		assertFalse(Pattern.matches(pattern, "a&^S&A^D"));
	}
}
