package org.sakaiproject.oxford.shortenedurl.entityprovider;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * An entity provider to handle the shortening of URLs from Weblearn to m.ox.
 * 
 * <p>Supply the weblearn path in the path parameter. It must be URL encoded, e.g. /direct/oxford/shorten?path=/poll/123.json
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface OxfordShortenedUrlEntityProvider extends EntityProvider {

	public final static String ENTITY_PREFIX = "oxford";
	
}
