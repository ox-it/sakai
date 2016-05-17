package org.sakaiproject.oxford.shortenedurl.entityprovider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.oxford.shortenedurl.api.ChartGenerator;
import org.sakaiproject.oxford.shortenedurl.api.OxfordShortenedUrlService;

/**
 * An entity provider to handle the shortening of URLs from Weblearn to m.ox.
 * 
 * <p>Supply the weblearn path in the path parameter. It must be URL encoded, e.g. /direct/oxford/shorten?path=/poll/123.json
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class OxfordShortenedUrlEntityProviderImpl implements OxfordShortenedUrlEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	@EntityCustomAction(action="shorten",viewKey=EntityView.VIEW_LIST)
	public Object shorten(OutputStream out, EntityView view, Map<String, Object> params) {
		
		String path = (String)params.get("path");
		if(StringUtils.isBlank(path)){
			throw new EntityException("Invalid path.", path);
		}
		
		try {
			String shortenedUrl = urlShortener.shorten(URLDecoder.decode(path, "UTF-8"));
			if(StringUtils.isBlank(shortenedUrl)){
				throw new EntityException("Couldn't shorten URL.", path);
			}
			return shortenedUrl;
		} catch (UnsupportedEncodingException e) {
			throw new EntityException("Unable to decode path.", path);
		}
	
	}
	
	
	@EntityCustomAction(action="qr",viewKey=EntityView.VIEW_LIST)
	public Object generateQRCode(OutputStream out, EntityView view, Map<String, Object> params) {
	
		String s = (String)params.get("s");
		String height = (String)params.get("height");
		String width = (String)params.get("width");
		if(StringUtils.isBlank(s) || StringUtils.isBlank(height) || StringUtils.isBlank(height)){
			throw new EntityException("Invalid input.", params.toString());
		}
	
		//get binary
		byte[] bytes = chartGenerator.generateQRCode(s, Integer.parseInt(height), Integer.parseInt(width));

		if(bytes != null && bytes.length > 0) {
			try {
				out.write(bytes);
				ActionReturn actionReturn = new ActionReturn("UTF-8", "image/png", out);
				return actionReturn;
			} catch (IOException e) {
				throw new EntityException("Error retrieving QR code : " + e.getMessage(), params.toString());
			}
		}
		
		return null;
	}
	
	
	
	private OxfordShortenedUrlService urlShortener;
	public void setUrlShortener(OxfordShortenedUrlService urlShortener) {
		this.urlShortener = urlShortener;
	}
	
	private ChartGenerator chartGenerator;
	public void setChartGenerator(ChartGenerator chartGenerator) {
		this.chartGenerator = chartGenerator;
	}
	
	
	public boolean entityExists(String eid) {
		return true;
	}

	public Object getSampleEntity() {
		return null;
	}
	
	public Object getEntity(EntityReference ref) {
		return null;
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] {};
	}

	public String[] getHandledInputFormats() {
		return new String[] {};
	}
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity,Map<String, Object> params) {
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		return null;
	}
	
	
}