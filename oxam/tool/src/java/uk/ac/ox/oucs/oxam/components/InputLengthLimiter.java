package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Simple behaviour to limit the length of inputs to a hard max.
 */
public class InputLengthLimiter extends AbstractBehavior implements IBehavior {

    private final int length;

    public InputLengthLimiter(int length) {
        this.length = length;
    }

    @Override
    public void onComponentTag(final Component component, final ComponentTag tag)
    {
        if (isEnabled(component))
        {
            String length = Integer.toString(this.length);
            tag.getAttributes().put("maxlength", length);
            tag.getAttributes().put("size", length);
        }
    }
}
