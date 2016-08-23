package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.Validator;

import java.util.Map;

/**
 * Validator for email address in profile
 * uses Wicket's EmailAddressValidator for email format check and kernel's
 * Validator to check if the email address is of local domain or not.
 */
public class EmailValidator extends AbstractValidator {
	private static final long serialVersionUID = 1L;
	private static final String OFFICIAL_USER_NAME = "Official Username";

	public void onValidate(IValidatable validatable) {
		String emailText = (String)validatable.getValue();
		Map map = super.variablesMap(validatable);
		//Check first if the email address is of correct format
		if (isValidEmailAddress(emailText)) {
			//check if email address contains any of accepted local/official hosts
			if(!Validator.isAllowedLocalEmailDomain(emailText)) {
				map.put("invalidAccount", ServerConfigurationService.getString("invalidNonOfficialAccountString"));
				map.put("localAccount", ServerConfigurationService.getString("localUserAccount", OFFICIAL_USER_NAME));
				error(validatable, getClass().getSimpleName() + ".profile.emailbaddomain", map);
			}
		} else {
			error(validatable, getClass().getSimpleName() + ".invalid", map);
		}
	}

	private boolean isValidEmailAddress(final String emailAddress) {
		Validatable validatable = new Validatable(emailAddress);
		EmailAddressValidator.getInstance().validate(validatable);
		return validatable.isValid();
	}

}
