package uk.ac.ox.oucs.oxam.pages;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

/**
 * This is a validator that warns the user once if they change the value of a field.
 * A resubmission of the form means the error isn't displayed and the submission 
 * can happen. This is written as a validator so that when the error message is 
 * generated it gets displayed back to the user.
 * 
 * The alternative would to have been have the onSubmit() method of the form check that 
 * the form is valid before doing anything and have an onModelChanged override on the 
 * form components that flagged an error, but this adds complexity/dependencies between 
 * remote chunks of the code.
 * 
 * @author buckett
 *
 * @param <T>
 */
public class ChangeValidator<T> extends AbstractValidator<T> {
	
	private static final long serialVersionUID = 1L;
	
	private FormComponent<T> original;
	private boolean warned = false;
	private String resourceKey = null;
	
	public ChangeValidator(FormComponent<T> form) {
		original = form;
	}
	
	public ChangeValidator(FormComponent<T> form, String resourceKey) {
		this(form);
		this.resourceKey = resourceKey;
	}

	@Override
	protected void onValidate(IValidatable<T> validatable) {
		// This works because at validation time the model hasn't yet been updated.
		if (!warned && original.getModelObject() != null && !original.getModelObject().equals(validatable.getValue())) {
			error(validatable);
			warned = true;
		}
	}
	
	@Override
	protected String resourceKey() {
		return (resourceKey==null)?super.resourceKey():resourceKey;
	}
	
}