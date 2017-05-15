package org.sakaiproject.gradebookng.business.exception;

/**
 * Thrown when revealed data / anonymous data is leaked into the opposing context
 */
public class AnonymousConstraintViolationException extends GbException
{
	private static final long serialVersionUID = 1L;

	public AnonymousConstraintViolationException(String message)
	{
		super(message);
	}
}
