package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CreateSiteValidator implements Validator {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean supports(Class clazz) {
		return CreateSiteCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object object, Errors errors) {
		CreateSiteCommand command = (CreateSiteCommand) object;
		if (!command.isCancelled()) {
			if (command.getName() == null || command.getName().length() == 0) {
				errors.reject("validator.name.required");
			}
			if (command.getSiteId() == null || command.getSiteId().length() == 0) {
				errors.reject("validator.siteId.required");
			}
		}

	}

}
