package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

public class AddRedirectController {

	/**
	 * Command class for adding a new redirect.
	 */
	public static class AddRedirectCommand {
		private String name;
		private String title;
		private String url;
		private boolean appendPath;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public boolean isAppendPath() {
			return appendPath;
		}

		public void setAppendPath(boolean appendPath) {
			this.appendPath = appendPath;
		}
	}

	/**
	 * Validator for adding a redirect.
	 */
	public static class AddRedirectCommandValidator extends BaseSiteValidator {

		@SuppressWarnings("rawtypes")
		public boolean supports(Class clazz) {
			return AddRedirectCommand.class.isAssignableFrom(clazz);
		}

		public void validate(Object target, Errors errors) {
			if (target instanceof AddRedirectCommand) {
				AddRedirectCommand command = (AddRedirectCommand) target;
				checkName(errors, command.getName());
				checkTitle(errors, command.getTitle());
				ValidationUtils.rejectIfEmpty(errors, "url",
						"validator.url.empty");
			}
		}

	}
}
