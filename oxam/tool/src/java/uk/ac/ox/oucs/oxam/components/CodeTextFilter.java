package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilter;
import org.apache.wicket.model.IModel;

public class CodeTextFilter<E> extends TextFilter<E> {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a code filter.
	 * @param id component id
	 * @param model model for the underlying form component
	 * @param form filter form this filter will be added to
	 * @param i The size of the text box.
	 */
	public CodeTextFilter(String id, IModel<E> model, FilterForm<?> form, int i) {
		super(id, model, form);
		String value = Integer.toString(i);
		getFilter().add(new InputLengthLimiter(i));
	}

}
