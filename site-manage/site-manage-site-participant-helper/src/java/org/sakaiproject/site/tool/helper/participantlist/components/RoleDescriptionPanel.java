package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.tool.helper.participantlist.service.ParticipantService;

/**
 * Describes the roles that participants can have for a given site
 * @author mweston4
 */
public class RoleDescriptionPanel extends Panel
{
    private final ParticipantService participantService;

    public RoleDescriptionPanel(String id)
    {
        super(id);
        this.participantService = new ParticipantService();

        add(new Label("roleDescriptionTitle", new ResourceModel("role.description.title")));
        RepeatingView repeatingRolesView = new RepeatingView("repeatingRoles");

        int i = 0;
        for (Role role: participantService.getAllRoles())
        {
            Loop.LoopItem loopItem = new Loop.LoopItem(i);
            i++;

            // OWL-936 - hide .auth and .anon from instructors  --bbailla2
            if (!".anon".equals(role.getId()) && !".auth".equals(role.getId()))
            {
                repeatingRolesView.add(loopItem);
                loopItem.add(new Label("repeatingRoleId", role.getId()));

                String roleDescription;
                if(role.getDescription() != null && !role.getDescription().isEmpty())
                {
                    roleDescription = role.getDescription();
                }
                else
                {
                    String roleId = role.getId();
                    roleId = roleId.replaceAll(" ", "");
                    roleDescription = new ResourceModel("role.description." + roleId,"No Description").getObject();
                }

                loopItem.add(new Label("repeatingRoleDescription",roleDescription));
            }
        }

        repeatingRolesView.setRenderBodyOnly(true);
        add(repeatingRolesView);
    }
}
