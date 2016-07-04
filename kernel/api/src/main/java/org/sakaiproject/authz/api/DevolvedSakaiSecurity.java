package org.sakaiproject.authz.api;

import java.util.List;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.PermissionException;

public interface DevolvedSakaiSecurity {
	
	/**
	 * If a user has this permission then they are admin for any realms that
	 * depend on this realm. In most cases anyone who has this permission should also have
	 * the ability to use it ({@link #ADMIN_REALM_PERMISSION_USE}).
	 */
	public static final String ADMIN_REALM_PERMISSION = "site.admin";
	
	/**
	 * If a user has this permission then they can use this realm for any other
	 * realms created.
	 */
	public static final String ADMIN_REALM_PERMISSION_USE = "site.admin.use";
	
	/**
	 * Get the admin realm for the supplied reference.
	 * @param entityRef The entity reference.
	 * @return The admin reference.
	 */
	String getAdminRealm(String entityRef);

	/**
	 * Set the admin realm.
	 * @param entityRef The reference to have an admin realm.
	 * @param adminRealm The admin realm.
	 * @throws PermissionException If the user doesn't have permission to manage the entity reference
	 * or permission to use the admin realm.
	 */
	void setAdminRealm(String entityRef, String adminRealm)
			throws PermissionException;

	/**
	 * Find all available admin realms that the supplied reference could have.
	 * The results are dependent on the current user. 
	 * @param entityRef The entity reference, can be <code>null</code> if the site hasn't yet been created.
	 * @return A list of all entities which can be admin realms.
	 */
	public List<Entity> getAvailableAdminRealms(String entityRef);
	
	/**
	 * Find all the places that use this admin realm.
	 * @param adminRealm The references of the admin realm.
	 * @return A list of entities that use this admin realm.
	 */
	public List<Entity> findUsesOfAdmin(String adminRealm);
	
	public void removeAdminRealm(String entityRef) throws PermissionException;

	/**
	 * Check if the current user can change the admin realm on the supplied reference.
	 * @param entityRef The entity reference.
	 * @return <code>true</code> if the current user can change the admin realm.
	 */
	public boolean canSetAdminRealm(String entityRef);
	
	public boolean canUseAdminRealm(String entityRef);
	
	public boolean canRemoveAdminRealm(String entityRef);
	
}