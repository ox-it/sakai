/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;

import org.sakaiproject.util.ResourceLoader;
import uk.ac.ox.oucs.vle.*;

/**
 * This is the actual Sakai proxy which talks to the Sakai services.
 * @author buckett
 *
 */
public class SakaiProxyImpl implements SakaiProxy {

	private final static Log log = LogFactory.getLog(SakaiProxyImpl.class);

	private final static ResourceLoader rb = new ResourceLoader("messages");

	private String fromAddress;

	/**
	 * Allow all access to content.
	 */
	private SecurityAdvisor allowContentRead = new SecurityAdvisor() {
		@Override
		public SecurityAdvice isAllowed(String userId, String function, String reference) {
			if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)) {
				return SecurityAdvice.ALLOWED;
			}
			return SecurityAdvice.PASS;
		}
	};

	/**
	 * The type to create new users as. Defaults to "guest" which is the same as Site Info tool.
	 */
	private String userType = "guest";
	public void setUserType(String userType) {
		this.userType = userType;
	}

	/**
	 * 
	 */
	private UserDirectoryService userService;
	public void setUserService(UserDirectoryService userService) {
		this.userService = userService;
	}

	/**
	 * 
	 */
	private EmailService emailService;
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	/**
	 * 
	 */
	private EventTrackingService eventService;
	public void setEventService(EventTrackingService eventService) {
		this.eventService = eventService;
	}

	/**
	 * 
	 */
	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	/**
	 * 
	 */
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * 
	 */
	private PortalService portalService;
	public void setPortalService(PortalService portalService) {
		this.portalService = portalService;
	}
	
	/**
	 * 
	 */
	private AdditionalUserDetails additionalUserDetails;
	public void setAdditionalUserDetails(AdditionalUserDetails additionalUserDetails) {
		this.additionalUserDetails = additionalUserDetails;
	}
	
	/**
	 * 
	 */
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	/**
	 * 
	 */
	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	/**
	 * 
	 */
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void init() {
		if (fromAddress == null) {
			fromAddress = serverConfigurationService.getString("course-signup.from", null);
		}
	}
	
	public UserProxy getCurrentUser() {
		User sakaiUser = userService.getCurrentUser();
		UserProxy user = wrapUserProxy(sakaiUser);
		return user;
	}

	@Override
	public boolean isAnonymousUser() {
		return userService.getAnonymousUser().equals(userService.getCurrentUser());
	}

	public UserProxy findUserById(String id) {
		try {
			return wrapUserProxy(userService.getUser(id));
		} catch (UserNotDefinedException unde) {
			return null;
		}
	}
	
	public UserProxy findStudentById(String id) {
		try {
			return wrapStudentProxy(userService.getUser(id));
		} catch (UserNotDefinedException unde) {
			return null;
		}
	}

	public UserProxy findUserByEmail(String email) {
		Collection<User> users = userService.findUsersByEmail(email);
		if (users.size() == 0) {
			return null;
		} else {
			if (users.size() > 1) {
				log.warn("More than one user found with email: "+ email);
			}
			return wrapUserProxy(users.iterator().next());
		}
		
	}

	public UserProxy findUserByEid(String eid) {
		try {
			return wrapUserProxy(userService.getUserByAid(eid));
		} catch (UserNotDefinedException unde) {
			return null;
		}
	}
	
	public UserProxy newUser(String name, String email) {
		User sakaiUser;
		String id = null;
		String eid = email;
		String firstName = null;
		String lastName = null;
		// Authentication never works in Sakai if the password is null.
		String pw = null;
		String type = userType;
		String trimmedName = name.trim();
		int i = trimmedName.lastIndexOf(" ");
		if (i > 0) {
			firstName = trimmedName.substring(0, i);
			lastName = trimmedName.substring(i+1);
		} else {
			firstName = trimmedName;
		}
		try {
			sakaiUser = userService.addUser(id, eid, firstName, lastName, email, pw, type, null);
		} catch (UserIdInvalidException e) {
			throw new IllegalArgumentException("Failed to add user because of bad ID", e);
		} catch (UserPermissionException e) {
			throw new PermissionDeniedException(e.getUser(), "Current user doesn't have permission to add users.", e);
		} catch (UserAlreadyDefinedException e) {
			return findUserByEmail(email);
		}
		UserProxy user = wrapUserProxy(sakaiUser);
		return user;
	}

	public void sendEmail(String to, String subject, String body) {
		String from = fromAddress;
		if (from == null) {
			from = getCurrentUser().getEmail();
		}
		emailService.send(
				from, // from address
				to, // to address
				subject, // subject
				body, // message body
				null, // header to string
				null, // Reply to string
				null // Additional headers
		);
	}
	
	public void logEvent(String resourceId, String eventType, String placementId) {
		Placement placement = getPlacement(placementId);
		String context = placement.getContext();
		String resource = "/coursesignup/group/"+ resourceId;
		Event event = eventService.newEvent(eventType, resource, context, false, NotificationService.NOTI_OPTIONAL);
		eventService.post(event);
	}

	public String getCurrentPlacementId() {
		return getPlacement(null).getId();
	}

	/**
	 * Just get the current placement.
	 * @return The current placement.
	 * @throws RuntimeException If there isn't a current placement, this happens
	 * when a request comes through that isn't processed by the portal.
	 */
	private Placement getPlacement(String placementId) {
		Placement placement = null;
		if (null == placementId) {
			placement = toolManager.getCurrentPlacement();
		} else {
			placement = siteService.findTool(placementId);
		}
		if (placement == null) {
			try {
				String defaultSiteId = getSiteId();
				if (null == defaultSiteId) {
					throw new RuntimeException("No default tool placement set.");
				}
				Site site = siteService.getSite(defaultSiteId);
				placement = site.getToolForCommonId("course.signup");
				
			} catch(Exception e) {
				throw new RuntimeException("No current tool placement set.");
			}
		}
		
		if (placement == null) {
			throw new RuntimeException("No current tool placement set.");
		}
		return placement;
	}
	
	protected String getSiteId() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("ses.default.siteId", "d0c31496-d5b9-41fd-9ea9-349a7ac3a01a");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private UserProxy wrapUserProxy(User sakaiUser) {
		if(sakaiUser == null) {
			return null;
		}
		
		List<String> units = sakaiUser.getProperties().getPropertyList("units");
		return new UserProxy(sakaiUser.getId(), sakaiUser.getEid(), 
				sakaiUser.getFirstName(), sakaiUser.getLastName(), sakaiUser.getDisplayName(), 
				sakaiUser.getEmail(),
				sakaiUser.getDisplayId(),
				sakaiUser.getProperties().getProperty("oakOSSID"), 
				sakaiUser.getProperties().getProperty("yearOfStudy"), 
				sakaiUser.getProperties().getProperty("oakStatus"),
				sakaiUser.getProperties().getProperty("primaryOrgUnit"),
				null,
				(units == null)?Collections.EMPTY_LIST:units);
	}
	
	private UserProxy wrapStudentProxy(User sakaiUser) {
		if(sakaiUser == null) {
			return null;
		}
		
		List<String> units = sakaiUser.getProperties().getPropertyList("units");
		return new UserProxy(sakaiUser.getId(), sakaiUser.getEid(), 
				sakaiUser.getFirstName(), sakaiUser.getLastName(), sakaiUser.getDisplayName(), 
				sakaiUser.getEmail(),
				sakaiUser.getDisplayId(),
				sakaiUser.getProperties().getProperty("oakOSSID"), 
				sakaiUser.getProperties().getProperty("yearOfStudy"), 
				sakaiUser.getProperties().getProperty("oakStatus"),
				sakaiUser.getProperties().getProperty("primaryOrgUnit"),
				additionalUserDetails.getDegreeProgram(sakaiUser.getEid()),
				(units == null)?Collections.EMPTY_LIST:units);
	}

	public String getAdminUrl() {
		return getUrl("/static/admin.jsp");
	}
	
	public String getConfirmUrl(String signupId) {
		return getConfirmUrl(signupId, null);
	}
	
	public String getConfirmUrl(String signupId, String placementId) {
		if (null == signupId) {
			return getUrl("/static/pending.jsp", placementId);
		}
		return getUrl("/static/pending.jsp#"+ signupId, placementId);
	}
	
	public String getDirectUrl(String courseId) {
		return getUrl("/static/index.jsp?openCourse="+ courseId);
	}
	
	public String getApproveUrl(String signupId) {
		return getApproveUrl(signupId, null);
	}
	
	public String getApproveUrl(String signupId, String placementId) {
		if (null == signupId) {
			return getUrl("/static/approve.jsp", placementId);
		}
		return getUrl("/static/approve.jsp#"+ signupId, placementId);
	}
	
	public String getAdvanceUrl(String signupId, String status, String placementId) {
		String urlSafe = encode(signupId+"$"+status+"$"+getPlacement(placementId).getId());
		return serverConfigurationService.getServerUrl() +
			"/course-signup/rest/signup/advance/"+urlSafe;
			
	}
	
	public String encode(String uncoded) {
		byte[] encrypted = aes(uncoded.getBytes(), Cipher.ENCRYPT_MODE);
		if (encrypted != null) {
			String base64String = new String(Base64.encodeBase64(encrypted));
			return base64String.replace('+','-').replace('/','_');
		} else {
			// Return obvious note that encryption failed.
			return "encryption.failed";
		}
	}
	
	public String uncode(String encoded) {
		String base64String = encoded.replace('-','+').replace('_','/');
		byte[] encrypted = Base64.decodeBase64(base64String.getBytes());
		String decrypted = new String(aes(encrypted, Cipher.DECRYPT_MODE));
		// On failed decryption we have to return null.
		return decrypted;
	}

	public String getMyUrl() {
		return getMyUrl(null);
	}
	
	public String getMyUrl(String placementId) {
		return getUrl("/static/my.jsp", placementId);
	}

	@Override
	public String getMessage(String key) {
		return rb.getString(key);
	}

	private String getUrl(String toolState) {
		return getUrl(toolState, null);
	}

	private String getUrl(String toolState, String placementId) {
		Placement currentPlacement = getPlacement(placementId);
		//String siteId = currentPlacement.getContext();
		ToolConfiguration toolConfiguration = siteService.findTool(currentPlacement.getId());
		String pageUrl = toolConfiguration.getContainingPage().getUrl();
		Map<String, String[]> encodedToolState = portalService.encodeToolState(currentPlacement.getId(), toolState);
		StringBuilder params = new StringBuilder();
		for (Entry<String, String[]> entry : encodedToolState.entrySet()) {
			for(String value: entry.getValue()) {
				params.append("&");
				params.append(entry.getKey());
				params.append("=");
				params.append(URLEncoder.encode(value));
			}
		}
		if (params.length() > 0) {
			pageUrl += "?"+ params.substring(1); // Trim the leading &
		}
		return pageUrl;
	}

	protected String getSecretKey() {
		// Return null if not set.
		// TODO This should do length checking as AES has fixed key lenths.
		String key = serverConfigurationService.getString("aes.secret.key", null);
		if (key == null) {
			log.error("No secret key specified. Please set 'aes.secret.key' in configuration");
		}
		return key;
	}
	
	protected byte[] aes(byte[] source, int mode) {
		String secretKey = getSecretKey();
		if (secretKey == null) {
			return null;
		}
		SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
		try {
			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(mode, skeySpec);
			byte[] encrypted = cipher.doFinal(source);
			return encrypted;
		
		} catch (Exception e) {
			String type = (mode == Cipher.DECRYPT_MODE)? "decryption" :
				(mode == Cipher.ENCRYPT_MODE)? "encryption" :
				"unknown";
			log.warn("AES "+ type+ " failed.", e);
		}
		return null;
	}

	public Integer getConfigParam(String param, int dflt) {
		return Integer.parseInt(serverConfigurationService.getString(param, new Integer(dflt).toString()));
	}

	public String getConfigParam(String param, String dflt) {
		return serverConfigurationService.getString(param, dflt);
	}
	
	/**
	 * 
	 * @param contentId
	 * @param contentDisplayName
	 * @param bytes
	 * @throws VirusFoundException
	 * @throws OverQuotaException
	 * @throws ServerOverloadException
	 * @throws PermissionException
	 * @throws TypeException
	 * @throws InUseException
	 */
	public void writeLog(String contentId, String contentDisplayName, byte[] bytes) {
		
		switchUser();
		ContentResourceEdit cre;
		try {
			cre = getContentResourceEdit(contentId, contentDisplayName);
			cre.setContent(bytes);
			// Don't notify anyone about this resource.
			contentHostingService.commitResource(cre, NotificationService.NOTI_NONE);
			
		} catch (PermissionException e) {
			log.error("PermissionException ["+e.getMessage()+"]", e);
			
		} catch (TypeException e) {
			log.error("TypeException ["+e.getMessage()+"]", e);
			
		} catch (InUseException e) {
			log.error("InUseException ["+e.getMessage()+"]", e);
			
		} catch (VirusFoundException e) {
			log.error("VirusFoundException ["+e.getMessage()+"]", e);
			
		} catch (OverQuotaException e) {
			log.error("OverQuotaException ["+e.getMessage()+"]", e);
			
		} catch (ServerOverloadException e) {
			log.error("ServerOverloadException ["+e.getMessage()+"]", e);
			
		}
		
	}

	public void prependLog(String contentId, String contentDisplayName, byte[] logBytes) {
		
		switchUser();
		
		File tempFile = null;
		OutputStream out = null;
		ContentResourceEdit cre = null;
		
		
		try {
			cre = getContentResourceEdit(contentId, contentDisplayName);
			
			tempFile = File.createTempFile("ses", ".tmp");
			tempFile.deleteOnExit();
			
			out = new FileOutputStream(tempFile);
			out.write(logBytes);
			IOUtils.copy(cre.streamContent(), out);
			out.flush();
			cre.setContent(new FileInputStream(tempFile));
			
			// Don't notify anyone about this resource.
			contentHostingService.commitResource(cre, NotificationService.NOTI_NONE);
			
		} catch (IOException e) {
			log.error("IOException ["+e.getMessage()+"]", e);
			
		} catch (ServerOverloadException e) {
			log.error("ServerOverloadException ["+e.getMessage()+"]", e);
			
		} catch (PermissionException e) {
			log.error("PermissionException ["+e.getMessage()+"]", e);
			
		} catch (TypeException e) {
			log.error("TypeException ["+e.getMessage()+"]", e);
			
		} catch (InUseException e) {
			log.error("InUseException ["+e.getMessage()+"]", e);
			
		} catch (VirusFoundException e) {
			log.error("VirusFoundException ["+e.getMessage()+"]", e);
			
		} catch (OverQuotaException e) {
			log.error("OverQuotaException ["+e.getMessage()+"]", e);
			
		} finally {
			
			try {
				
				if (null != out) {
					out.close();
				}
				
				if (null != tempFile) {
					tempFile.delete();
				}
				
			} catch (IOException e) {
				log.error("IOException ["+e.getMessage()+"]", e);
			}
		}
		
	}

	@Override
	public Properties getCategoryMapping() {
		String siteId = getConfigParam("course-signup.site-id", "course-signup");
		String filename = getConfigParam("course-signup.category-mapping", "category-mapping.properties");
		String filePath = contentHostingService.getSiteCollection(siteId)+filename;
		InputStream input = null;
		try {
			securityService.pushAdvisor(allowContentRead);
			ContentResource resource = contentHostingService.getResource(filePath);
			input = resource.streamContent();
			Properties props = new Properties();
			props.load(input);
			return props;
		} catch (IdUnusedException iue) {
			// This may well be missing.
			log.debug("Couldn't find category mapping file: "+ filePath);
		} catch (Exception e) {
			log.warn("Failed to load properties from: "+ filePath, e);
		} finally {
			securityService.popAdvisor(allowContentRead);
			if (input != null) {
				try {
					input.close();
				} catch (IOException ioe) {
					// Ignore.
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param contentId
	 * @param contentDisplayName
	 * @return
	 * @throws PermissionException
	 * @throws TypeException
	 * @throws InUseException
	 */
	private ContentResourceEdit getContentResourceEdit(String contentId, String contentDisplayName) 
			throws PermissionException, TypeException, InUseException {
		
		ContentResourceEdit cre = null;
		
		String siteId = getConfigParam("course-signup.site-id", "course-signup");
		String jsonResourceEId = contentHostingService.getSiteCollection(siteId)+"logs/"+ contentId;

		try {
			// editResource() doesn't throw IdUnusedExcpetion but PermissionException
			// when the resource is missing so we first just tco to find it.
			contentHostingService.getResource(jsonResourceEId);
			cre = contentHostingService.editResource(jsonResourceEId);
	
		} catch (IdUnusedException e) {
			try {
				cre = contentHostingService.addResource(jsonResourceEId);
				ResourceProperties props = cre.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, contentDisplayName);
				cre.setContentType("text/html");
			} catch (Exception e1) {
				log.warn("Failed to create the import log file.", e1);
			}
		}
		return cre;
	}
	
	/**
	 * This sets up the user for the current request.
	 */
	
	private void switchUser() {
		if (null != sessionManager) {
			org.sakaiproject.tool.api.Session session = sessionManager.getCurrentSession();
			session.setUserEid("admin");
			session.setUserId("admin");
		}
	}

}
