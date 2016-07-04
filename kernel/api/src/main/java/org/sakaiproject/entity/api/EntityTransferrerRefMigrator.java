package org.sakaiproject.entity.api;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Services which implement EntityTransferrerRefMigrator declare themselves as willing and able to transfer/copy
 * their entities from one context to another.
 * </p>
 * <p>
 * This interface has two parts:
 * <dl>
 * <dt>Transfer Entities</dt>
 * <dd>Copy the entities between the two contexts.
 * After transferring the entities, it expects you to return a map of (Old Site Entity Refs -> New Site Entity Refs).
 * {@link #transferCopyEntitiesRefMigrator(String, String, java.util.List)}</dd>
 * <dt>Update Entity References</dt>
 * <dd>After the entities have been copied accept a map of (Old Site Entity Refs -> New Site Entity Refs)
 * You can implement the conversion however you want within your tool.
 * {@link #updateEntityReferences(String, java.util.Map)}</dd>
 * 
 * </p>
 * @author bryan holladay
 * @see org.sakaiproject.entity.api.EntityTransferrer Your implementation must also implement this interface.
 *
 */

public interface EntityTransferrerRefMigrator {

	/**
	 * Takes a map of ref's (fromContextRef -> toContextRef) and replaces any reference to them
	 * 
	 * @param toContext The site ID where the content is going to.
	 * @param transversalMap
	 */
	void updateEntityReferences(String toContext, Map<String, String> transversalMap);

	/**
	 * {@link EntityTransferrer#transferCopyEntities(String, String, List)}
	 * 
	 * @return
	 */
	Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids);
	
	/**
	 * {@link EntityTransferrer#transferCopyEntities(String, String, List, boolean)}
	 * 
	 * @return
	 */
	Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids, boolean cleanup);

}
