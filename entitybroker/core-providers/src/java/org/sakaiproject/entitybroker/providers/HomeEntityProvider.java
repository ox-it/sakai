package org.sakaiproject.entitybroker.providers;

import lombok.Setter;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.providers.model.EntityOverview;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HomeEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Describeable, EntityProvider, ActionsExecutable, Outputable, Sampleable {

	public final static String ENTITY_PREFIX = "home";

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON, Formats.XML, Formats.HTML };
	}

	@Override
	public Object getSampleEntity() {
		return "<HTML></HTML>";
	}

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@Setter
	private transient SiteService siteService;

	@Setter
	private transient ContentHostingService contentService;

	@Setter
	protected SessionManager sessionManager;

	@Setter
	private transient DeveloperHelperService developerHelperService;

	private static Logger log = LoggerFactory.getLogger(SiteEntityProvider.class);

	public void init() {
	}

	@EntityCustomAction(action="migrate",viewKey=EntityView.VIEW_LIST)
	public Object getEntity(EntityView view, Map<String, Object> params) throws PermissionException {
		String siteId = view.getPathSegment(2);
		if (siteId == null || "".equals(siteId)) {
			throw new IllegalArgumentException("No siteId in provided reference");
		}

		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("Cannot find site by siteId: " + siteId, e);
		} catch (PermissionException pe) {
			throw new SecurityException(pe);
		}

		// if a file is used as the overview page use the content of that,
		// otherwise the site description if not null or the title if it is. 
		String fileLocation = site.getInfoUrl();
		String description = site.getDescription();
		EntityOverview eo = new EntityOverview();

		if (fileLocation == null) {
			if (description != null) {
				eo.setData(site.getDescription());
			} else {
				eo.setData(site.getTitle());
			}
		} else {
			// Return the content of the HTML file.

			ContentResource resource = null;
			String resourceLocation = fileLocation.replaceAll("/access/content", "");
			try {
				resource = contentService.getResource(resourceLocation);
				if ("text/html".equals(resource.getContentType())) {
					eo.setData(new String(resource.getContent()));
				}
			} catch (PermissionException pe) {
				log.warn("Permission exception: " + pe);
			} catch (IdUnusedException ie) {
				log.warn("ID unused exception: " + ie);
			} catch (TypeException te) {
				log.warn("Type exception: " + te);
			} catch (ServerOverloadException se) {
				log.warn("Server overload exception: " + se);
			}
		}

		return eo;
	}
}
