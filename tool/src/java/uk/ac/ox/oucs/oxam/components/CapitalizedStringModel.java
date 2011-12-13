package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.model.IModel;

public class CapitalizedStringModel implements IModel<String> {

	private static final long serialVersionUID = 1L;

	private final IModel<String> mNestedModel;

	public CapitalizedStringModel(IModel<String> nestedModel) {
		mNestedModel = nestedModel;
	}

	public String getObject() {
		String value = mNestedModel.getObject();
		return (value != null)?value.toUpperCase():null;
	}

	public void setObject(String object) {
		mNestedModel.setObject(object);
	}

	public void detach() {
		mNestedModel.detach();
	}

}