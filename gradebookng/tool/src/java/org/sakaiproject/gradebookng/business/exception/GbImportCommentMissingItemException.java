package org.sakaiproject.gradebookng.business.exception;

import lombok.Getter;

/**
 * An exception indicating that a comment column was imported but it was missing the corresponding gb item
 */
public class GbImportCommentMissingItemException extends GbException {

	@Getter
	private final String columnTitle;

	public GbImportCommentMissingItemException(final String message, final String title) {
		super(message);
		columnTitle = title;
	}
}
