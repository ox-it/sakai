package uk.ac.ox.oucs.vle;

import java.util.Collections;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;


public class ExternalGroupManagerCover {
	
	private static ExternalGroupManager m_instance;
	
	public static ExternalGroupManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (ExternalGroupManager) ComponentManager
						.get(ExternalGroupManager.class);
			return m_instance;
		}
		else
		{
			return (ExternalGroupManager) ComponentManager.get(ExternalGroupManager.class);
		}
	}
	
	public static List<ExternalGroup> search(String query) throws ExternalGroupException {
		ExternalGroupManager service = getInstance();
		if (service == null) return Collections.emptyList();
		
		return service.search(query);
	}
	
	public static List<ExternalGroup> search(String terms[]) throws ExternalGroupException {
		ExternalGroupManager service = getInstance();
		if (service == null) return Collections.emptyList();
		
		return service.search(terms);
	}
	
	public ExternalGroup findExternalGroup(String externalGroupId) throws ExternalGroupException {
		ExternalGroupManager service = getInstance();
		if (service == null) return null;;
		
		return service.findExternalGroup(externalGroupId);
	}
	
	public String findExternalGroupId(String mappedGroupId) {
		ExternalGroupManager service = getInstance();
		if (service == null) return null;
		
		return service.findExternalGroupId(mappedGroupId);
	}
	
	public String addMappedGroup(String externalGroupId, String role) throws ExternalGroupException {
		ExternalGroupManager service = getInstance();
		if (service == null) return null;
		
		return service.addMappedGroup(externalGroupId, role);
	}
	
}
