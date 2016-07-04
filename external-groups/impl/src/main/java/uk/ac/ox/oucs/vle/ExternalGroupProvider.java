package uk.ac.ox.oucs.vle;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.DisplayGroupProvider;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.util.StringUtil;

/**
 * Group provider based on external group definition. As the external group store may go down 
 * RuntimeExceptions are thrown when the external store is down.
 * 
 */
public class ExternalGroupProvider implements GroupProvider, DisplayGroupProvider{

	private final static Log log = LogFactory.getLog(ExternalGroupProvider.class);
	
	private ExternalGroupManager externalGroupManager;
	private List<String> roleOrder;

	public void init() {
		log.info("init()");
		if (externalGroupManager == null) {
			throw new IllegalStateException("ExternalGroupManager cannot be empty.");
		}
		if (roleOrder == null) {
			log.info("No role order specified, using maintain, access.");
			roleOrder = Arrays.asList(new String[]{"maintain", "access"});
		}
	}
	
	public Map getGroupRolesForUser(String userId) {
		try {
			return externalGroupManager.getGroupRoles(userId);
		} catch (ExternalGroupException ege) {
			throw new RuntimeException(ege);
		}
	}

	public String getRole(String id, String user) {
		return externalGroupManager.findRole(id);
	}

	public Map<String, String> getUserRolesForGroup(String ids) {
		if (ids == null) {
			log.debug("Can't get groups for null.");
			return Collections.EMPTY_MAP;
		}
		Map<String, String> userRoles = new HashMap<String, String>();
		for (String id: unpackId(ids)) {
			String groupId = externalGroupManager.findExternalGroupId(id);
			String role = externalGroupManager.findRole(id);
			ExternalGroup group;
			try {
				group = externalGroupManager.findExternalGroup(groupId);
			} catch (ExternalGroupException e) {
				throw new RuntimeException(e);
			}
			if (role == null || groupId == null || group == null) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to find all the data for provider id: "+ id);
				}
				continue;
			}
			for (Iterator<String> memberEids = group.getMemberEids(); memberEids.hasNext(); ) {
				String eid = memberEids.next();
				String existingRole = userRoles.get(eid);
				if (existingRole == null) {
					userRoles.put(eid, role);
				} else {
					userRoles.put(eid, preferredRole(existingRole, role));
				}
			}
		}
		return userRoles;
	}

	public String preferredRole(String one, String other) {

		if (one.equals(other))
			return one;
		for (String role : roleOrder) {
			if (role.equals(one)) {
				return one;
			}
			if (role.equals(other)) {
				return other;
			}
		}
		// Unknown roles are never preferred.
		if (log.isDebugEnabled()) {
			log.debug("Can't decide between two unknown roles. ("+ one+ ", "+ other+ " )");
		}
		return one;
	}

	public String packId(String[] ids) {
		return StringUtil.unsplit(ids, ",");
	}

	public String[] unpackId(String id) {
		if (id == null) {
			return new String[]{};
		}
		return StringUtil.split(id, ",");
	}

	
	public List<String> getRoleOrder() {
		return roleOrder;
	}

	public void setRoleOrder(List<String> roleOrder) {
		this.roleOrder = roleOrder;
	}

	public ExternalGroupManager getExternalGroupManager() {
		return externalGroupManager;
	}

	public void setExternalGroupManager(ExternalGroupManager externalGroupManager) {
		this.externalGroupManager = externalGroupManager;
	}

	public String getGroupName(String groupId) {
		// TODO need caching for this.
		String externalGroupId = externalGroupManager.findExternalGroupId(groupId);
		String role = externalGroupManager.findRole(groupId);
		if (role != null && externalGroupId != null) {
			ExternalGroup group;
			try {
				group = externalGroupManager.findExternalGroup(externalGroupId);
			} catch (ExternalGroupException e) {
				throw new RuntimeException(e);
			}
			if (group != null) {
				return group.getName()+ " ("+ role+ ")";
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Mapped group can't be found any more: "+ externalGroupId);
				}
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Mapped group doesn't exist: "+ groupId);
			}
		}
		return null;
	}

	@Override
	public String getSuspendedGroupProviderId(String providerId, List providerIds) {

		Map<String, String> providerIdToExternalGroupId=  new HashMap<>();
		for (String provId: (List<String>)providerIds) {
			String externalGroupId = externalGroupManager.findExternalGroupId(provId);
			providerIdToExternalGroupId.put(provId, externalGroupId);
		}

		String externalGroupId = externalGroupManager.findExternalGroupId(providerId);
		if (externalGroupId.contains("ou=programme,ou=course") && externalGroupId.contains("current")){
			for (String key : providerIdToExternalGroupId.keySet()) {
				if (externalGroupId.replace("current", "suspended").equals(providerIdToExternalGroupId.get(key))){
					return key;
				}
			}
		}
		return "";
	}

	public boolean groupExists(String groupId) {
		return externalGroupManager.findExternalGroupId(groupId) != null;
	}
}
