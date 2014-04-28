package uk.ac.ox.oucs.vle;

import java.util.*;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MockUserDirectoryServices implements UserDirectoryService {

	@Override
	public PasswordRating validatePassword(String s, User user) {
		return null;
	}

	public UserEdit addUser(String arg0, String arg1)
			throws UserIdInvalidException, UserAlreadyDefinedException,
			UserPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean allowAddUser() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowRemoveUser(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUser(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserEmail(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserName(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserPassword(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserType(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public User authenticate(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void cancelEdit(UserEdit arg0) {
		// TODO Auto-generated method stub

	}

	public void commitEdit(UserEdit arg0) throws UserAlreadyDefinedException {
		// TODO Auto-generated method stub

	}

	public int countSearchUsers(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int countUsers() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void destroyAuthentication() {
		// TODO Auto-generated method stub

	}

	public UserEdit editUser(String arg0) throws UserNotDefinedException,
			UserPermissionException, UserLockedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection findUsersByEmail(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getAnonymousUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUser(String arg0) throws UserNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByAid(String arg0) throws UserNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByEid(final String eid) throws UserNotDefinedException {
		return new User() {

			public boolean checkPassword(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			public User getCreatedBy() {
				// TODO Auto-generated method stub
				return null;
			}

			public Time getCreatedTime() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Date getCreatedDate() {
				return null;
			}

			public String getDisplayId() {
				// TODO Auto-generated method stub
				return "displayId"+eid;
			}

			public String getDisplayName() {
				// TODO Auto-generated method stub
				return "displayName"+eid;
			}

			public String getEid() {
				// TODO Auto-generated method stub
				return eid;
			}

			public String getEmail() {
				// TODO Auto-generated method stub
				return "email"+eid;
			}

			public String getFirstName() {
				// TODO Auto-generated method stub
				return "firstName"+eid;
			}

			public String getLastName() {
				// TODO Auto-generated method stub
				return "lastName"+eid;
			}

			public User getModifiedBy() {
				// TODO Auto-generated method stub
				return null;
			}

			public Time getModifiedTime() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Date getModifiedDate() {
				return null;
			}

			public String getSortName() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getType() {
				// TODO Auto-generated method stub
				return "type"+eid;
			}

			public String getId() {
				// TODO Auto-generated method stub
				return "id"+eid;
			}

			public ResourceProperties getProperties() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getReference() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getReference(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			public String getUrl() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getUrl(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			public Element toXml(Document arg0, Stack arg1) {
				// TODO Auto-generated method stub
				return null;
			}

			public int compareTo(Object o) {
				// TODO Auto-generated method stub
				return 0;
			}
			
		};
	}

	public String getUserEid(String arg0) throws UserNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserId(String arg0) throws UserNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public List getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getUsers(Collection arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getUsers(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> getUsersByEids(Collection<String> strings) {
		return null;
	}

	public UserEdit mergeUser(Element arg0) throws UserIdInvalidException,
			UserAlreadyDefinedException, UserPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeUser(UserEdit arg0) throws UserPermissionException {
		// TODO Auto-generated method stub

	}

	public List searchUsers(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> searchExternalUsers(String s, int i, int i2) {
		return null;
	}

	public String userReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public User addUser(String arg0, String arg1, String arg2, String arg3,
			String arg4, String arg5, String arg6, ResourceProperties arg7)
			throws UserIdInvalidException, UserAlreadyDefinedException,
			UserPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public String archive(String arg0, Document arg1, Stack arg2, String arg3,
			List arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String arg0, Element arg1, String arg2, String arg3,
			Map arg4, Map arg5, Set arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String arg0, Reference arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge() {
		// TODO Auto-generated method stub
		return false;
	}

}
