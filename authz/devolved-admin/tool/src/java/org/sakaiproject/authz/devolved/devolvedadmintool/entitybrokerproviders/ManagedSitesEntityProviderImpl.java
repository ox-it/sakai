package org.sakaiproject.authz.devolved.devolvedadmintool.entitybrokerproviders;

import org.sakaiproject.authz.api.DevolvedSakaiSecurity;
import org.sakaiproject.authz.devolved.devolvedadmintool.entitybrokerproviders.model.ManagedEntitySite;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import java.util.*;

public class ManagedSitesEntityProviderImpl extends AbstractEntityProvider
		implements AutoRegisterEntityProvider, ActionsExecutable, Describeable, EntityProvider, Outputable {

	public final static String ENTITY_PREFIX = "managed-sites";
	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON, Formats.XML, Formats.HTML };
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private DevolvedSakaiSecurity devolvedSakaiSecurity;
	public void setDevolvedSakaiSecurity(DevolvedSakaiSecurity devolvedSakaiSecurity) {
		this.devolvedSakaiSecurity = devolvedSakaiSecurity;
	}

	/**
	 * /managed-sites/[siteid]/sites.json (e.g. /managed-sites/test-admin/sites.json)
	 */
	@EntityCustomAction (action = "sites", viewKey = EntityView.VIEW_SHOW)
	public Object getManagedSites(EntityView view) throws IdUnusedException, PermissionException { 
		String id = view.getEntityReference().getId();
		// Check site exists and user has permission to access the site.
		try {
			siteService.getSiteVisit(id);
		} catch (IdUnusedException ie) {
			throw new EntityNotFoundException("Cannot find site by siteId", id);
		} catch (PermissionException pe) {
			throw new SecurityException ();
		}

		List<Entity> sites = devolvedSakaiSecurity.findUsesOfAdmin("/site/" + id);

		List<ManagedEntitySite> managedEntitySites = new ArrayList<>();
		for (Entity entity : sites) {
			ManagedEntitySite es = new ManagedEntitySite((Site)entity, false);
			managedEntitySites.add(es);
		}
		return managedEntitySites;
	}
}