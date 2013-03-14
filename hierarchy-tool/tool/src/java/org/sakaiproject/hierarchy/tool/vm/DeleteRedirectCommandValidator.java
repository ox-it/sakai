package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validator for deleting a redirect.
 */
public class DeleteRedirectCommandValidator {

	@SuppressWarnings("rawtypes")
	public boolean supports(Class clazz) {
		return DeleteRedirectCommand.class.isAssignableFrom(clazz);
	}

	public void validate(Object target, Errors errors) {
		ValidationUtils
				.rejectIfEmpty(errors, "id", "validator.nothing.selected");
	}

}