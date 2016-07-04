package org.sakaiproject.authz.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.PermissionException;

public class DevolvedSakaiSecurity {
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.authz.api.DevolvedSakaiSecurity getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.authz.api.DevolvedSakaiSecurity) ComponentManager
				.get(org.sakaiproject.authz.api.DevolvedSakaiSecurity.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.authz.api.DevolvedSakaiSecurity) ComponentManager
			.get(org.sakaiproject.authz.api.DevolvedSakaiSecurity.class);
		}
	}

	private static org.sakaiproject.authz.api.DevolvedSakaiSecurity m_instance = null;

	public static String getAdminRealm(String entityRef)
	{
		org.sakaiproject.authz.api.DevolvedSakaiSecurity service = getInstance();
		if (service == null) return null;

		return service.getAdminRealm(entityRef);
	}

	public static void setAdminRealm(String entityRef, String adminRealm) throws PermissionException
	{
		org.sakaiproject.authz.api.DevolvedSakaiSecurity service = getInstance();
		if (service == null) return;

		service.setAdminRealm(entityRef, adminRealm);
	}

	public static List<Entity> getAvailableAdminRealms(String entityRef)
	{
		org.sakaiproject.authz.api.DevolvedSakaiSecurity service = getInstance();
		if (service == null) return null;

		return service.getAvailableAdminRealms(entityRef);
	}
	
	public static List<Entity> findUsesOfAdmin(String adminRealm)
	{
		org.sakaiproject.authz.api.DevolvedSakaiSecurity service = getInstance();
		if (service == null) return null;

		return service.findUsesOfAdmin(adminRealm);
	}

	public static void removeAdminRealm(String entityRef) throws PermissionException
	{
		org.sakaiproject.authz.api.DevolvedSakaiSecurity service = getInstance();
		if (service == null) return;

		service.removeAdminRealm(entityRef);
	}
	
	public static boolean canSetAdminRealm(String entityRef)
	{
		org.sakaiproject.authz.api.DevolvedSakaiSecurity service = getInstance();
		if (service == null) return false;

		return service.canSetAdminRealm(entityRef);
	}
	
	public static boolean canRemoveAdminRealm(String entityRef)
	{
		org.sakaiproject.authz.api.DevolvedSakaiSecurity service = getInstance();
		if (service == null) return false;

		return service.canRemoveAdminRealm(entityRef);
	}
}
