package org.sakaiproject.hierarchy.tool.vm;

import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public abstract class BaseSiteValidator implements Validator
{

	private int maxNameLength = 12;
	private int maxTitleLength = 20;

	public BaseSiteValidator()
	{
		super();
	}

	protected void checkTitle(Errors errors, NewSiteCommand command)
	{
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title",
				"validator.title.empty");
		if (command.getTitle() != null && command.getTitle().length() > maxTitleLength) {
			errors.rejectValue("title", "validator.title.too.long", new Object[] {maxTitleLength}, null);
		}
	}

	protected void checkName(Errors errors, NewSiteCommand command)
	{
		String url = command.getName();
		if (url == null || url.length() == 0) {
			errors.rejectValue("name", "validator.name.empty");
		} else {
			if (url.length() > maxNameLength) {
				errors.rejectValue("name", "validator.name.too.long",
						new Object[] { Integer.toString(maxNameLength) }, null);
			}
			if (!Pattern.matches("[a-z0-9][_a-z0-9]*", url)) {
				errors.rejectValue("name", "validator.name.bad.characters");
			}
		}

	}

	public int getMaxTitleLength()
	{
		return maxTitleLength;
	}

	public void setMaxTitleLength(int maxTitleLength)
	{
		this.maxTitleLength = maxTitleLength;
	}

}