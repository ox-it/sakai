package org.sakaiproject.hierarchy.tool.vm;

import java.util.regex.Pattern;

import org.sakaiproject.hierarchy.tool.vm.NewSiteCommand.Method;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class NewSiteValidator implements Validator {

	private int maxLength = 12;
	private int generateLength = 10;

	public boolean supports(Class clazz) {
		return NewSiteCommand.class.isAssignableFrom(clazz);
	}

	public void validate(Object object, Errors errors) {
		NewSiteCommand command = (NewSiteCommand) object;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title",
				"validator.title.empty");
		if (Method.CUSTOM.equals(command.getMethod())) {
			String url = command.getName();
			if (url == null || url.length() == 0) {
				errors.rejectValue("name", "validator.name.empty");
			} else {
				if (url.length() > maxLength) {
					errors.rejectValue("name", "validator.name.too.long",
							new Object[] { Integer.toString(maxLength) }, null);
				}
				if (!Pattern.matches("[a-z0-9][_a-z0-9]*", url)) {
					errors.rejectValue("name", "validator.name.bad.characters");
				}
			}
		} else if (Method.AUTOMATIC.equals(command.getMethod())) {
			String name = generateName(command.getTitle());
			if (name == null || name.length() == 0) {
				errors.rejectValue("name", "error.name.generation");
				command.setMethod(Method.CUSTOM);
			} else {
				command.setName(name);
			}
		}

	}

	protected String generateName(String title) {
		if (title == null) {
			return null;
		}
		String tmp = title;
		tmp = tmp.toLowerCase();
		tmp = tmp.replaceAll("[^a-z,0-9,_ ]", "");
		tmp = tmp.replaceAll(" ", "_");
		tmp = tmp.replaceAll("_+", "_");
		tmp = tmp.substring(0, (tmp.length() > generateLength) ? 10 : tmp
				.length());
		if (tmp.endsWith("_")) {
			tmp = tmp.substring(0, tmp.length() - 1);
		}
		return tmp;
	}

}
