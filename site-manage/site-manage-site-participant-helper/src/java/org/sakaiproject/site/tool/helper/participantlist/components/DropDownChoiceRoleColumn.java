package org.sakaiproject.site.tool.helper.participantlist.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.tool.helper.participantlist.model.Participant;
import org.sakaiproject.site.tool.helper.participantlist.service.ParticipantService;

/**
 *
 * @author mweston4
 */
public abstract class DropDownChoiceRoleColumn extends AbstractColumn<Participant>
{
    private final ParticipantService participantService;

    public DropDownChoiceRoleColumn(IModel<String> displayModel, String sortProperty)
    {
        super(displayModel, sortProperty);
        participantService = new ParticipantService();
    }

    @Override
    public void populateItem(Item<ICellPopulator<Participant>> cellItem, String componentId, IModel<Participant> rowModel)
    {
        String userRole = rowModel.getObject().getRole();

        if (!participantService.isMyWorkspace() && participantService.allowUpdateSiteMembership())
        {
            boolean hasAllowedRole = false;

            List<Role> roles = participantService.getAllowedRoles();
            for (Role r : roles)
            {
                if (userRole.equals(r.getId()))
                {
                    hasAllowedRole = true;
                }
            }
            if (hasAllowedRole)
            {
                cellItem.add(new DropDownChoiceRoleColumn.DropDownChoicePanel(componentId, newDropDownChoiceModel(rowModel)));
            }
            else
            {
                cellItem.add(new Label(componentId, userRole));
            }
        }
        else
        {
            cellItem.add(new Label(componentId, userRole));
        }
    }

    protected DropDownChoice newDropDownChoice(String id, IModel<String> dropDownModel)
    {
        String userRole = dropDownModel.getObject();
        boolean hasRestrictedRole = false;
        List<String> optionsList = new ArrayList<>();
        List<Role> allowedRoles = participantService.getAllowedRoles();

        for (Role r : allowedRoles)
        {
            if (r.getId().equalsIgnoreCase(userRole) && r.isProviderOnly())
            {
                hasRestrictedRole = true;
            }
        }

        for (Role r : allowedRoles)
        {
            if (!(r.isProviderOnly() || hasRestrictedRole))
            {
                optionsList.add(r.getId());
            }
        }

        return new DropDownChoice("dropdwnRole", dropDownModel,optionsList);
    }

    protected abstract IModel<String> newDropDownChoiceModel(IModel<Participant> rowModel);

    private class DropDownChoicePanel extends Panel
    {
        public DropDownChoicePanel(String id, IModel<String> dropDownModel)
        {
            super(id);
            add(newDropDownChoice("dropdwnRole",dropDownModel));
        }
    }
}
