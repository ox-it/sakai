package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.validation.Errors;

public class ReuseSiteValidator extends BaseSiteValidator {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean supports(Class clazz) {
		return NewSiteCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object object, Errors errors) {
		NewSiteCommand command = (NewSiteCommand) object;
		checkName(errors, command.getName());
	}

}
