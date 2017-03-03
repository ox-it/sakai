package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.ToolManager;

/**
 * Utility class to append the setMainFrameHeightWithMax JavaScript
 * based on the current placement's frame ID.
 * 
 * @author bjones86, plukasew
 */
public class FrameResizer
{
    private static final ToolManager TOOL_MANAGER = (ToolManager) ComponentManager.get( ToolManager.class );
    private static final String PLACEMENT_ID_SEP = "-";
    private static final String FRAME_ID_PREFIX = "Main";
    private static final String FRAME_ID_SEP_SUB = "x";
    private static final String FRAME_HEIGHT_UNLIMITED = "-1";

    /**
     * Add the setMainFrameHeightWithMax JavaScript to the target given
     * @param target 
     *          the target component to add the JavaScript to
     */
    public static void appendMainFrameResizeJs( AjaxRequestTarget target )
    {
        String placementID = TOOL_MANAGER == null ? "" : TOOL_MANAGER.getCurrentPlacement().getId();
        String frameID = FRAME_ID_PREFIX + placementID.replaceAll( PLACEMENT_ID_SEP, FRAME_ID_SEP_SUB );
        target.appendJavascript("setMainFrameHeightWithMax('" + frameID + "', " + FRAME_HEIGHT_UNLIMITED + ");");
    }
}
