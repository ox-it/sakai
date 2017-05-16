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
	 * /managed-sites/sites.json?adminSite=[siteid] (e.g. /managed-sites/sites.json?adminSite=test-admin)
	 */
	@EntityCustomAction (action = "sites", viewKey = EntityView.VIEW_LIST)
	public Object getManagedSites(EntityView view, Map<String, Object> params) throws IdUnusedException, PermissionException {
		if (!params.containsKey("adminSite")) {
			throw new IllegalArgumentException("Must include the name of an admin site in order to show information of sites managed by that admin site.");
		}
		String adminSite = (String) params.get("adminSite");
		// Check site exists and user has permission to access the site.
		try {
			siteService.getSiteVisit(adminSite);
		} catch (IdUnusedException ie) {
			throw new EntityNotFoundException("Cannot find site by siteId", adminSite);
		} catch (PermissionException pe) {
			throw new SecurityException ();
		}

		List<Entity> sites = devolvedSakaiSecurity.findUsesOfAdmin("/site/" + adminSite);

		List<ManagedEntitySite> managedEntitySites = new ArrayList<>(sites.size());
		for (Entity entity : sites) {
			ManagedEntitySite es = new ManagedEntitySite((Site)entity, false);
			managedEntitySites.add(es);
		}
		return managedEntitySites;
	}
}