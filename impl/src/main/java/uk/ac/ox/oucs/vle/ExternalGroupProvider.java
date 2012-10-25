package uk.ac.ox.oucs.vle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.DisplayGroupProvider;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.StringUtil;

import uk.ac.ox.oucs.vle.ExternalGroup;
import uk.ac.ox.oucs.vle.ExternalGroupManager;

/**
 * Group provider based on external group definition.
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
		return externalGroupManager.getGroupRoles(userId);
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
			ExternalGroup group = externalGroupManager.findExternalGroup(groupId);
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
			ExternalGroup group = externalGroupManager.findExternalGroup(externalGroupId);
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
	
	public boolean groupExists(String groupId) {
		return externalGroupManager.findExternalGroupId(groupId) != null;
	}
}
