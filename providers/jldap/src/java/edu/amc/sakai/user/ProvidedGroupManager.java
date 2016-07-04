package edu.amc.sakai.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.id.api.IdManager;

public class ProvidedGroupManager {

	private IdManager idManager;
	public IdManager getIdManager() {
		return idManager;
	}

	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}

	private Map<String,ProvidedGroup> groupsById = new HashMap<String, ProvidedGroup>();
	private Map<String,Set<ProvidedGroup>> groupsByDn =  new HashMap<String, Set<ProvidedGroup>>();
	
	public void init() {
		
	}
	
	public ProvidedGroup newGroup(String dn, String role) {
		String id = idManager.createUuid();
		ProvidedGroup newGroup = new ProvidedGroup(id, dn, role);
		groupsById.put(id, newGroup);
		Set groups = groupsByDn.get(dn);
		if (groups == null) {
			groups = new HashSet<ProvidedGroup>();
			groupsByDn.put(dn, groups);
		}
		groups.add(newGroup);
		return newGroup;
	}
	
	public ProvidedGroup getGroup(String id) {
		return groupsById.get(id);
	}
	
	public Set<ProvidedGroup> getGroupByDNs(String dn) {
		return groupsByDn.get(dn);
	}
	
}
