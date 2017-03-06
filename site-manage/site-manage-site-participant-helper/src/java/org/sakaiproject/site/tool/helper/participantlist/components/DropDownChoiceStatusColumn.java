package org.sakaiproject.site.tool.helper.participantlist.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.site.tool.helper.participantlist.model.Participant;

/**
 *
 * @author mweston4
 */
public abstract class DropDownChoiceStatusColumn extends AbstractColumn<Participant, String>
{
    public DropDownChoiceStatusColumn(IModel<String> displayModel, String sortProperty)
    {
        super(displayModel, sortProperty);
    }

    @Override
    public void populateItem(Item<ICellPopulator<Participant>> cellItem, String componentId, IModel<Participant> rowModel)
    {
        cellItem.add(new DropDownChoiceStatusColumn.DropDownChoicePanel(componentId, newDropDownChoiceModel(rowModel)));
    }

    protected DropDownChoice newDropDownChoice(String id, IModel<String> dropDownModel)
    {
        List<String> optionsList = new ArrayList<>();
        optionsList.add(new ResourceModel("participantlist.status.active").getObject());
        optionsList.add(new ResourceModel("participantlist.status.inactive").getObject());
        return new DropDownChoice("dropdwn", dropDownModel,optionsList);
    }

    protected abstract IModel<String> newDropDownChoiceModel(IModel<Participant> rowModel);

    private class DropDownChoicePanel extends Panel
    {
        public DropDownChoicePanel(String id, IModel<String> dropDownModel)
        {
            super(id);
            add(newDropDownChoice("dropdwn",dropDownModel));
        }
    }
}
