package uk.ac.ox.it.shoal.components;

import org.apache.wicket.markup.html.form.ListChoice;

import java.util.List;

/**
 * Don't include a default option when none is selected. This is useful because when using the select2 JavaScript
 * library it presents the default option as a selectable option.
 */
public class NoDefaultListChoice<T> extends ListChoice<T> {

    public NoDefaultListChoice(String id, List<? extends T> values) {
        super(id, values);
    }

    @Override
    protected CharSequence getDefaultChoice(final String selectedValue) {
        // When there's no value form the model an empty string is returned.
        if ("".equals(selectedValue)) {
            return "";
        } else {
            return super.getDefaultChoice(selectedValue);
        }

    }
}
