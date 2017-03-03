package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.model.ResourceModel;

/**
 *
 * @author mweston4
 * @param <T>
 */
public class CheckBoxHeaderPanel<T> extends Panel
{
    public CheckBoxHeaderPanel(String id)
    {
        super(id);
    }

    @Override
    protected void onBeforeRender()
    {
        if (!hasBeenRendered())
        {
            add(new CheckGroupSelector("checkAll"));
            add(new Label("removeLabel", new ResourceModel("participantlist.remove").getObject()));
        }

        super.onBeforeRender();
    }
}
