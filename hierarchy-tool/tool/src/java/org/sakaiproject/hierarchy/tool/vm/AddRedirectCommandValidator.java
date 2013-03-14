package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validator for adding a redirect.
 */
public class AddRedirectCommandValidator extends BaseSiteValidator {

	@SuppressWarnings("rawtypes")
	public boolean supports(Class clazz) {
		return AddRedirectCommand.class.isAssignableFrom(clazz);
	}

	public void validate(Object target, Errors errors) {
		if (target instanceof AddRedirectCommand) {
			AddRedirectCommand command = (AddRedirectCommand) target;
			checkName(errors, command.getName());
			checkTitle(errors, command.getTitle());
			ValidationUtils.rejectIfEmpty(errors, "url", "validator.url.empty");
		}
	}

}