package org.sakaiproject.ckeditor.config;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

import java.util.*;

/**
 * Provides server config variables to CKEditor
 *
 * @author Ben Holmes (bdvholmes@gmail.com)
 *
 */
public class CKEditorConfigEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, CoreEntityProvider, Outputable, ActionsExecutable {

    public final static String ENTITY_PREFIX = "ckeditor-config";
    
    private ServerConfigurationService _configService;
    
    public void setServerConfigurationService(ServerConfigurationService configService) {
        _configService = configService;
    }

    // AutoRegisterEntityProvider
    @Override
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    // CoreEntityProvider
    @Override
    public boolean entityExists(String id) {
        return true;
    }

    // Outputable
    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    /**
     *  Lists blocked ck plugins as specified in the server config
     */
    @EntityCustomAction(action = "listBlockedPlugins", viewKey = EntityView.VIEW_LIST)
    public List<String> getBlockedPlugins(EntityView view, Map<String, Object> params) {

        List<String> blockedPlugins = new ArrayList<String>();
        
        String[] plugins = _configService.getStrings("ckeditor.config.blockedPlugins");
        if (plugins != null) {
            blockedPlugins.addAll(Arrays.asList(plugins));
        }

        return blockedPlugins;
    }

}
