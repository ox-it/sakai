package org.sakaiproject.provider.user;

import java.util.Collection;

import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;

/**
 * UserDirectoryProvider which checks that something is set in a thread local 
 * before allowing any requests through.
 * The reason for doing this is to make sure that only some requests (eg WebDAV) 
 * are allowed to authenticate directly.
 *
 * @author buckett
 *
 */
public class ThreadCheckUserDirectoryProvider implements UserDirectoryProvider {

	private ThreadLocalManager threadLocalManager;

	private UserDirectoryProvider wrappedProvider;
	
	private String threadLocalKey;
	
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}

	public void setWrappedProvider(UserDirectoryProvider wrappedProvider) {
		this.wrappedProvider = wrappedProvider;
	}

	public void setThreadLocalKey(String threadLocalKey) {
		this.threadLocalKey = threadLocalKey;
	}
	
	public void init() {
		if(threadLocalKey == null) {
			threadLocalKey = ThreadCheckUserDirectoryProvider.class.getName();
		}
	}

	/**
	 * Check that the current thread has the correct variable set.
	 * @return <code>true</code> if the wrapped provider can be called.
	 */
	protected boolean checkThread() {
		return Boolean.TRUE.equals(Boolean.valueOf((String)threadLocalManager.get(threadLocalKey)));
	}
	
	public boolean authenticateUser(String eid, UserEdit edit, String password) {
		if (checkThread()) {
			return wrappedProvider.authenticateUser(eid, edit, password);
		}
		return false;
	}

	public boolean authenticateWithProviderFirst(String eid) {
		if(checkThread()) {
			return wrappedProvider.authenticateWithProviderFirst(eid);
		}
		return false;
	}

	public boolean findUserByEmail(UserEdit edit, String email) {
		if(checkThread()) {
			return wrappedProvider.findUserByEmail(edit, email);
		}
		return false;
	}

	public boolean getUser(UserEdit edit) {
		if(checkThread()) {
			return wrappedProvider.getUser(edit);
		}
		return false;
	}

	public void getUsers(Collection users) {
		if(checkThread()) {
			wrappedProvider.getUsers(users);
		} else {
			users.clear();
		}
	}

}
