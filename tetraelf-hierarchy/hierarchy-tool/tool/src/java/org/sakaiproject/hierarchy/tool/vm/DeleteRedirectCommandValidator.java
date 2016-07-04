package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for deleting a redirect.
 */
public class DeleteRedirectCommandValidator implements Validator{

	@SuppressWarnings("rawtypes")
	@Override
	public boolean supports(Class clazz) {
		return DeleteRedirectCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "id", "validator.nothing.selected");
	}

}