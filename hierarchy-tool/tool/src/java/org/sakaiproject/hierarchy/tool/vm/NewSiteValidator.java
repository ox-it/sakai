package org.sakaiproject.hierarchy.tool.vm;


import org.springframework.validation.Errors;

public class NewSiteValidator extends BaseSiteValidator {

	public boolean supports(Class clazz) {
		return NewSiteCommand.class.isAssignableFrom(clazz);
	}

	public void validate(Object object, Errors errors) {
		NewSiteCommand command = (NewSiteCommand) object;
		checkTitle(errors, command.getTitle());
		checkName(errors, command.getName());
	}

}
