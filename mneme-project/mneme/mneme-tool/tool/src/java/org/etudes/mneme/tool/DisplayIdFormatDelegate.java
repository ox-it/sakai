package org.etudes.mneme.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.FormatDelegateImpl;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * The "Display ID" format delegate for the mneme tool.
 */
public class DisplayIdFormatDelegate extends FormatDelegateImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(DisplayIdFormatDelegate.class);
	
	private UserDirectoryService userDirectoryService;

	/**
	 * Shutdown.
	 */
	public void destroy() {
		M_log.info("destroy()");
	}

	public String format(Context context, Object value)	{
		return value.toString();
	}

	public Object formatObject(Context context, Object value) {
		
		if (!(value instanceof String)) {
			return null;
		}
		
		User user = null;
		try {
			user = userDirectoryService.getUser((String)value);
			
		} catch (UserNotDefinedException e) {
			M_log.info("formatObject(): User with the ID - " + value + " - not found.");
		}
		
		String displayId = null;
		if (null != user) {
			displayId = user.getDisplayId(); 
		}
		
		return displayId != null ? displayId : context.getMessages().getString("dash");
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init(){
		super.init();
		M_log.info("init()");
	}

	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
}
