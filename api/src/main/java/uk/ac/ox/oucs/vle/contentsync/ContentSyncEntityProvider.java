package uk.ac.ox.oucs.vle.contentsync;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Entity api for MessageForum's Forum Message Entities
 * 
 * @author Joshua Ryan josh@asu.edu
 *
 */
public interface ContentSyncEntityProvider extends EntityProvider {
  public final static String ENTITY_PREFIX = "content_sync";
}
