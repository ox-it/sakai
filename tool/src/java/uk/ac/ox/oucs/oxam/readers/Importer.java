package uk.ac.ox.oucs.oxam.readers;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class Importer {
	
	private Validator validator;

	public void setValidatorFactory(ValidatorFactory validator) {
		this.validator = validator.getValidator();
	}

	public Import newImport() {
		return new Import(this);
	}
	
	public <T> Set<ConstraintViolation<T>> validate(T object) {
		return validator.validate(object);
	}
	
}
