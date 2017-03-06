package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.sakaiproject.site.tool.helper.participantlist.model.Participant;
import org.sakaiproject.site.tool.helper.participantlist.service.ParticipantService;

/**
 *
 * @author mweston4
 */
public class CheckBoxColumn extends AbstractColumn<Participant, String>
{
    private final ParticipantService participantService;

    public CheckBoxColumn(IModel<String> displayModel)
    {
        super(displayModel);
        participantService = new ParticipantService();
    }

    @Override
    public Component getHeader(String componentId)
    {
        return new CheckBoxHeaderPanel(componentId);
    }

    @Override
    public void populateItem(Item<ICellPopulator<Participant>> cellItem, String componentId, IModel<Participant> rowModel)
    {
        Participant p = rowModel.getObject();
        if (p.getRemove() && (!participantService.isMyWorkspace() || !participantService.getSiteUserId().equals(p.getUniqName())))
        {
            cellItem.add(new CheckPanel(componentId, rowModel));
        }
        else
        {
            cellItem.add(new Label(componentId, ""));
        }
    }

    protected Check newCheckBox(String id, IModel<Participant> checkModel)
    {
        return new Check("check", checkModel);
    }

    private class CheckPanel extends Panel
    {
        public CheckPanel(String id, IModel<Participant> checkModel)
        {
            super(id);
            add(newCheckBox("check",checkModel));
        }
    }
}
