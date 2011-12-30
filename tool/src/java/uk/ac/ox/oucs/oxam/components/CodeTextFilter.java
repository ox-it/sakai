package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilter;
import org.apache.wicket.model.IModel;

public class CodeTextFilter<E> extends TextFilter<E> {

	private static final long serialVersionUID = 1L;

	public CodeTextFilter(String id, IModel<E> model, FilterForm<?> form) {
		super(id, model, form);
	}

}
