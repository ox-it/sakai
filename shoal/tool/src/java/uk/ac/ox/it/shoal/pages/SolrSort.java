package uk.ac.ox.it.shoal.pages;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.List;

/**
 * This displays a sort box for the search.
 */
public class SolrSort extends Panel {

    public SolrSort(String id, PageParameters pp) {
        super(id);
        DropDownChoice<Order> choice = new DropDownChoice<Order>("order",
                new Model<>(pp.get("order").toEnum(Order.class, null)),
                new ListModel<>(Arrays.asList(Order.values())),
                new EnumChoiceRenderer<>(this)) {

            public String getInputName() {
                return "order";
            }
        };
        List<INamedParameters.NamedPair> pairs = pp.getAllNamed();
        // This persists the current parameters in the form
        RepeatingView repeating = new RepeatingView("hidden");
        // When changing the sort, don't keep the current page order the order.
        pairs.stream().filter(p -> !("order".equals(p.getKey()) || "items".equals(p.getKey()))).forEach(pair -> {
            WebMarkupContainer element = new WebMarkupContainer(repeating.newChildId()) {
                @Override
                public void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);
                    tag.getAttributes().put("name", pair.getKey());
                    tag.getAttributes().put("value", pair.getValue());

                }
            };
            repeating.add(element);
        });
        choice.setLabel(new ResourceModel("Order.label"));

        add(repeating);
        add(choice);
    }
}
