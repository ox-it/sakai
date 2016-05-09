/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.entitybroker.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.providers.model.ResourcesListItem;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 * 
 */
public class ContentActionsProvider extends AbstractEntityProvider 
implements EntityProvider, RESTful, AutoRegisterEntityProvider, RequestInterceptor {
	
	private static final Log logger = LogFactory.getLog(ContentActionsProvider.class);

	public ResourceLoader rb = new ResourceLoader("resources"); 

	public static final String ENC_UTF8 = "UTF-8";
	public final static String PREFIX = "contentActions";

	private static final long ONE_KILOBYTE = 1024L;
	private static final long ONE_MEGABYTE = ONE_KILOBYTE * ONE_KILOBYTE;
	private static final long ONE_GIGABYTE = ONE_KILOBYTE * ONE_MEGABYTE;
	private static final double THRESHOLD = 0.8;
	
	public static final String PERIOD = ".";
	
	protected static NumberFormat nf = NumberFormat.getInstance();
	
	protected DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	protected TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	protected ContentHostingService contentService;
	public void setContentService(ContentHostingService contentService) {
		this.contentService = contentService;
	}
	
	protected SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	protected EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}


	public final static Map<String, String> ACTION_MAP = new HashMap<String, String>();
	
	static {
		ACTION_MAP.put(ResourceToolAction.REVISE_METADATA, EntityView.VIEW_EDIT);
		ACTION_MAP.put(ResourceToolAction.REVISE_CONTENT, EntityView.VIEW_EDIT);
		ACTION_MAP.put(ResourceToolAction.REPLACE_CONTENT, EntityView.VIEW_EDIT);
		ACTION_MAP.put(ResourceToolAction.REORDER, EntityView.VIEW_EDIT);
		ACTION_MAP.put(ResourceToolAction.MOVE, EntityView.VIEW_EDIT);
		ACTION_MAP.put(ResourceToolAction.PASTE_MOVED, EntityView.VIEW_NEW);
		ACTION_MAP.put(ResourceToolAction.PASTE_COPIED, EntityView.VIEW_NEW);
		ACTION_MAP.put(ResourceToolAction.PERMISSIONS, EntityView.VIEW_EDIT);
		ACTION_MAP.put(ResourceToolAction.CREATE, EntityView.VIEW_NEW);
		ACTION_MAP.put(ResourceToolAction.DELETE, EntityView.VIEW_DELETE);
		ACTION_MAP.put("list", EntityView.VIEW_LIST);
		ACTION_MAP.put("get", EntityView.VIEW_SHOW);
	}
	
	/**
	 * Take actions after the request is handled for an entity view, this will
	 * be called just before each response is sent back to the requester,
	 * normally this would be used to add something to the response as it is
	 * getting ready to be sent back to the requester
	 * 
	 * @param view
	 *            an entity view, should contain all the information related to
	 *            the incoming entity URL
	 * @param req
	 *            the servlet request (available in case you need to get
	 *            anything out of it)
	 * @param res
	 *            the servlet response, put the correct data response into the
	 *            outputstream
	 */
	public void after(EntityView view, HttpServletRequest req,
			HttpServletResponse res) {
		logger.debug("after ");
		
		// When updating a resource IE clients use an iframe which doesn't allow
		// the headers to be retrieved. Putting a message in the body works around
		// this.
		String viewKey = view.getViewKey();
		if(EntityView.VIEW_EDIT.equals(viewKey) || EntityView.VIEW_DELETE.equals(viewKey)) {
			if (!res.isCommitted()) {
				try {
					// By default it's a 204 for no content, but we're now sending something.
					res.setStatus(200);
					res.getWriter().append("OK");
				} catch (IOException e) {
					logger.warn("Failed to append workaround message.", e);
				}
			}
		}
	}

	/**
	 * Take actions before the request is handled for an entity view, this will
	 * be called just before each request is sent to the correct request
	 * handler, this might be used to add information to the response before it
	 * goes on to be handled or to take some action as a result of information
	 * in the request or reference,<br/>
	 * if you want to interrupt the handling of this request (stop it) then
	 * throw an {@link EntityException} and include the type of response you
	 * would like to return in the exception (this can be a success or failure
	 * response status)
	 * 
	 * @param view
	 *            an entity view, should contain all the information related to
	 *            the incoming entity URL
	 * @param req
	 *            the servlet request (available in case you need to get
	 *            anything out of it)
	 * @param res
	 *            the servlet response, put the correct data response into the
	 *            outputstream
	 */
	public void before(EntityView view, HttpServletRequest req,
			HttpServletResponse res) {
		String pathInfo = req.getPathInfo();
		String mode = req.getParameter("mode");
		if(mode == null) {
			mode = "list";
		}

		logger.debug("......before()  mode = " + mode + "   pathInfo == " + pathInfo);

		if(ACTION_MAP.get(mode) == null) {
			view.setViewKey(mode);
		} else {
			view.setViewKey(ACTION_MAP.get(mode));
		}

		String entityId = findId(pathInfo);
		
		
		if(entityId != null) {
			String encodedId = encodeEntityId(entityId);
			if(encodedId != null) {
				EntityReference ref = new EntityReference(PREFIX, encodedId);
				view.setEntityReference(ref);
			} else {
				EntityReference ref = new EntityReference(PREFIX, "");
				view.setEntityReference(ref);
			}
		}
		
		String extension = req.getParameter("rettype");
		view.setExtension(extension);
		
	}
	
	/**
	 * curl -u "admin:admin" -F "mode=create" -F "actionId=" 
	 * 		-F "type=org.sakaiproject.content.types.TextDocumentType" 
	 * 		-F "containingCollectionId=/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/" 
	 * 		-F "Filedata=@myLocalFolder/myFile.json" 
	 * 		http://localhost:8080/direct/contentActions?__auth=basic
	 * 
	 * curl -u "admin:admin" -F "mode=create" -F "actionId=" 
	 * 		-F "type=org.sakaiproject.content.types.folder" 
	 * 		-F "entity.name=myNewFolder" 
	 * 		-F "containingCollectionId=/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/"  
	 * 		http://localhost:8080/direct/contentActions?__auth=basic
	 */
	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		logger.debug("......createEntity(" + ref + "," + entity + "," + params + ")  ");
		
		if(logger.isDebugEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("\n params -----------------------");
			for(String key : params.keySet()) {
				Object val = params.get(key);
				buf.append("\n\tKey: ");
				buf.append(key);
				buf.append(" --> ");
				buf.append(val.getClass().getCanonicalName());
				buf.append(" --> ");
				buf.append(val);
			}
			buf.append("\n params -----------------------\n");
			logger.debug(buf.toString());
		}
		String newEntityId = null;
		
		String actionId = (String) params.get("actionId");
		String containingCollectionId = (String) params.get("containingCollectionId");
		
		if(ActionType.PASTE_MOVED.toString().equalsIgnoreCase(actionId)) {
			newEntityId = moveResources(containingCollectionId, params);
		} else if(ActionType.PASTE_COPIED.toString().equalsIgnoreCase(actionId)) {
			newEntityId = copyResources(containingCollectionId, params);
		} else {

			String resourceType = (String) params.get("type");
			String content = (String) params.get("entity.content");
			String displayName = (String) params.get("entity.name");
			String notify = (String) params.get("notify");
			String contentType = (String) params.get("contentType");
			
			Object fileDataObj = params.get("Filedata");
			DiskFileItem fileData = null;
			InputStream contentStream = null;
			
			if(fileDataObj != null && fileDataObj instanceof DiskFileItem) {
				fileData = (DiskFileItem) fileDataObj;
				displayName = (String) fileData.getName();
				contentType = (String) fileData.getContentType();
				try {
					contentStream = fileData.getInputStream();
				} catch (IOException e) {
					logger.warn("IOException getting input stream from FileData object");
				}
				
				String sessionId = (String) params.get("JSESSIONID");
				if(sessionId != null) {
					setSession(sessionId);
				} 
	
			}
	
			if(resourceType == null) {
				
			} else if(ResourceType.TYPE_FOLDER.equalsIgnoreCase(resourceType)) {
				ContentCollection newItem = createContentCollection(containingCollectionId, displayName);
				if(newItem != null) {
					newEntityId = newItem.getId();
				}
			} else if (ResourceType.TYPE_HTML.equalsIgnoreCase(resourceType)) {
				ContentResource newItem = null;
				if(contentStream == null) {
					newItem = createContentResource(containingCollectionId, displayName, content.getBytes(), ResourceType.MIME_TYPE_HTML, resourceType, notify);
				} else {
					newItem = createContentResource(containingCollectionId, displayName, contentStream, ResourceType.MIME_TYPE_HTML, resourceType, notify);
				}
				if(newItem != null) {
					newEntityId = newItem.getId();
				}
			} else if (ResourceType.TYPE_TEXT.equalsIgnoreCase(resourceType)) {
				ContentResource newItem = null;
				if(contentStream == null) {
					newItem = createContentResource(containingCollectionId, displayName, content.getBytes(), ResourceType.MIME_TYPE_TEXT, resourceType, notify);
				} else {
					newItem = createContentResource(containingCollectionId, displayName, contentStream, ResourceType.MIME_TYPE_TEXT, resourceType, notify);
				} 
				if(newItem != null) {
					newEntityId = newItem.getId();
				}
			} else if (ResourceType.TYPE_UPLOAD.equalsIgnoreCase(resourceType)) {
				
				ContentResource newItem = createContentResource(containingCollectionId, displayName, contentStream, contentType, resourceType, notify);
				if(newItem != null) {
					newEntityId = newItem.getId();
				}
			} else if (ResourceType.TYPE_URL.equalsIgnoreCase(resourceType)) {
				ContentResource newItem = createContentResource(containingCollectionId, displayName, content.getBytes(), ResourceType.MIME_TYPE_URL, resourceType, notify);
				if(newItem != null) {
					newEntityId = newItem.getId();
				}
			}
		}
		
		newEntityId = encodeEntityId(newEntityId);
		
		return newEntityId;
	}
	
	/**
	 *  curl -u "admin:admin" -X DELETE 
	 *  	http://localhost:8080/direct/contentActions/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/myFile.xml?__auth=basic
	 *
	 *	curl -u "admin:admin" -X DELETE 
	 *		http://localhost:8080/direct/contentActions/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/?__auth=basic
	 */
	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		logger.debug("......deleteEntity(" + ref + "," + params + ")  ");
		
		String entityId = decodeEntityId(ref.getId());
		try {	
			if (contentService.isCollection(entityId)) { 
				ContentCollectionEdit edit = this.contentService.editCollection(entityId);
				contentService.removeCollection(edit);
				
			} else {
				ContentResourceEdit edit = contentService.editResource(entityId);
				contentService.removeResource(edit);
				
			}
			
		} catch(PermissionException e) {
			//log.debug("PermissionException trying to delete entity: " + entityId, e);
			throw new SecurityException(e);
			
		} catch (IdUnusedException e) {
			throw new IllegalStateException(e);
			
		} catch (TypeException e) {
			throw new IllegalStateException(e);
			
		} catch (InUseException e) {
			throw new IllegalStateException(e);
			
		} catch (InconsistentException e) {
			throw new IllegalStateException(e);
			
		} catch (ServerOverloadException e) {
			throw new IllegalStateException(e);
		}
		
		logger.debug("successfully deleted entity: " + ref.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable
	 * #getEntities(org.sakaiproject.entitybroker.EntityReference,
	 * org.sakaiproject.entitybroker.entityprovider.search.Search)
	 */
	public List<Object> getEntities(EntityReference ref, Search search) {
		String queryString = search.getQueryString();
		logger.debug("-----> getEntities(" + ref + "," + search + ")");
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#
	 * getEntity(org.sakaiproject.entitybroker.EntityReference)
	 * 
	 * curl -u "admin:admin"  
	 * http://localhost:8080/direct/resourcesUpdate/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/myfile.txt?__auth=basic
	 */
	
	public Object getEntity(EntityReference ref) {
		/*
		String entityId = ref.getId();
		if(entityId == null) { 
			return new ResourcesListItem();
		}
		entityId = decodeEntityId(entityId);

		ContentEntity entity = getContentEntity(entityId);
		if (entity == null) {
			throw new IllegalStateException();
		}
		
		ResourcesListItem item = new ResourcesListItem(entity, externalLogic);
		
		//logger.debug("......getEntity(" + ref + ")  entityId == '" + entityId + "' returning:\n" + item);
		
		return item;
		*/
		return null;
	}

	public String getEntityPrefix() {
		return PREFIX;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable#getHandledInputFormats()
	 */
	public String[] getHandledInputFormats() {
		// TODO Auto-generated method stub
		logger.debug("......getHandledInputFormats()  ");
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable#
	 * getHandledOutputFormats()
	 */
	public String[] getHandledOutputFormats() {
		logger.debug("......getHandledOutputFormats()  ");
		
		return new String[]{ Formats.HTML, Formats.JSON, Formats.XML };
	}

	public Object getSampleEntity() {
		logger.debug("......getSampleEntity()");
		
		return new ResourcesListItem();
	}

	/*
	 * example rename a resource
	 * 
	 * curl -u "admin:admin" 
	 * 		-F "actionId=properties"
	 * 		-F "entity.name=myTest.txt"  
	 * 		http://localhost:8080/direct/contentActions/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/departments.json?__auth=basic
	 * 
	 * replace the contents of a resource
	 * 
	 * curl -u "admin:admin" 
	 * 		-F "actionId=replace" 
	 * 		-F "Filedata=@Downloads/myNewFile.json" 
	 * 		http://localhost:8080/direct/contentActions/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/MyFile.txt?__auth=basic
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable#
	 * updateEntity(org.sakaiproject.entitybroker.EntityReference,
	 * java.lang.Object, java.util.Map)
	 */
	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		logger.debug("......updateEntity(" + ref + "," + entity + ")  ");
			
		if (logger.isDebugEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("\n params -----------------------");
			for(String key : params.keySet()) {
				Object val = params.get(key);
				buf.append("\n\tKey: ");
				buf.append(key);
				buf.append(" --> ");
				buf.append(val.getClass().getCanonicalName());
				buf.append(" --> ");
				buf.append(val);
			}
			buf.append("\n params -----------------------\n");
			logger.debug(buf.toString());
		}
		
		String actionId = (String) params.get("actionId");
		
		//ResourcesListItem item = (ResourcesListItem) entity;
		
		String notifyCode = (String) params.get("notify");
		int notify = calculateNotification(notifyCode);
		
		String entityId = ref.getId(); //item.getEntityId();
		entityId = decodeEntityId(entityId);
		
		try {
			
			if (ResourceToolAction.REVISE_METADATA.equalsIgnoreCase(actionId)) {
				// "properties" update
				String name = (String) params.get("entity.name");
				String description = (String) params.get("entity.description");
			
				String hiddenStr = (String) params.get("entity.hidden");
				boolean hidden = hiddenStr != null && Boolean.TRUE.toString().equalsIgnoreCase(hiddenStr);
				Time releaseDate = null;
				Time retractDate = null;
			
				if (!hidden) {
					String useReleaseDateStr = (String) params.get("entity.useReleaseDate");
					boolean useReleaseDate = useReleaseDateStr != null && Boolean.TRUE.toString().equalsIgnoreCase(useReleaseDateStr);
					if (useReleaseDate) {
						releaseDate = extractTime(params, "entity.releaseDate");
					}
				
					String useRetractDateStr = (String) params.get("entity.useRetractDate");
					boolean useRetractDate = useRetractDateStr != null && Boolean.TRUE.toString().equalsIgnoreCase(useRetractDateStr);
					if (useRetractDate) {
						retractDate = extractTime(params, "entity.retractDate");
					}
				}
				
				if (contentService.isCollection(entityId)) {
					ContentCollectionEdit collection = editContentCollection(entityId);
					ResourcePropertiesEdit props = collection.getPropertiesEdit();
				
					if (name != null) {
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
						}
				
					if (description != null) {
						props.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
					}
				
					collection.setAvailability(hidden, releaseDate, retractDate);
					contentService.commitCollection(collection);
				
				} else {
					
					ContentResourceEdit resource = editContentResource(entityId);
					ResourcePropertiesEdit props = resource.getPropertiesEdit();
				
					if (name != null) {
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
					}
				
					if (name != null) {
						props.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
					}
				
					resource.setAvailability(hidden, releaseDate, retractDate);
				
					String copyright = (String) params.get("entity.copyright");
					String copyrightAlertStr = (String) params.get("entity.copyrightAlert");
					boolean copyrightAlert = copyrightAlertStr != null && Boolean.TRUE.toString().equalsIgnoreCase(copyrightAlertStr);
				
					if (copyright != null) {
						props.addProperty(ResourceProperties.PROP_COPYRIGHT, copyright);
					}
					if (copyrightAlert) {
						props.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.toString(copyrightAlert));
					} else {
						props.removeProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
					}		
				
					contentService.commitResource(resource, notify);
				}
				
				} else if (ResourceToolAction.REVISE_CONTENT.equalsIgnoreCase(actionId)) {
					if (contentService.isCollection(entityId)) {
						throw new IllegalArgumentException("Cannot revise content on collections");
					} else {
						ContentResourceEdit resource = editContentResource(entityId);
				
						String name = (String) params.get("entity.name");
						if (name != null) {
							ResourcePropertiesEdit props = resource.getPropertiesEdit();
							props.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, name);
						}

						String contentString = (String) params.get("entity.content");
						resource.setContent(contentString.getBytes());
						contentService.commitResource(resource, notify);
					}
			
				} else if (ResourceToolAction.REPLACE_CONTENT.equalsIgnoreCase(actionId)) {
					if (contentService.isCollection(entityId)) {
						throw new IllegalArgumentException("Cannot replace content on collections");
					} else {
						ContentResourceEdit resource = editContentResource(entityId);
						
						Object fileDataObj = params.get("Filedata");
						DiskFileItem fileData = null;
						InputStream contentStream = null;
						
						if(fileDataObj != null && fileDataObj instanceof DiskFileItem) {
							fileData = (DiskFileItem) fileDataObj;
							//displayName = (String) fileData.getName();
							//String contentType = (String) fileData.getContentType();
							try {
								contentStream = fileData.getInputStream();
							} catch (IOException e) {
								logger.warn("IOException getting input stream from FileData object");
							}
							resource.setContent(contentStream);
						}
						
						contentService.commitResource(resource, notify);
					}
			
				} else if (ResourceToolAction.REORDER.equalsIgnoreCase(actionId)) {
					if (contentService.isCollection(entityId)) {
						ContentCollectionEdit collection = editContentCollection(entityId);
						contentService.commitCollection(collection);
				
					} else {
						throw new IllegalArgumentException("Cannot reorder resources");
					}
			
				} else if (ResourceToolAction.PERMISSIONS.equalsIgnoreCase(actionId)) {
			
				}
		
		} catch (VirusFoundException e) {
			throw new RuntimeException(e);
		} catch (OverQuotaException e) {
			throw new RuntimeException(e);
		} catch (ServerOverloadException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * curl -u "admin:admin" -F "mode=create" -F "actionId=paste_moved" 
	 * 		-F "item2move=/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/myFile.xml" 
	 * 		-F "containingCollectionId=/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/myNewFolder/"  
	 * 		http://localhost:8080/direct/contentActions?__auth=basic
	 * 
	 * @param item
	 * @param params
	 * @return
	 * @throws IllegalArgumentException if the entity could not be updated because of missing or invalid data or could not find entity to update 
	 * @throws SecurityException if permissions prevented this entity from being updated 
	 * @throws IllegalStateException for all other failures
	 */
	protected String moveResources(String destinationId, Map<String, Object> params) {
		logger.debug("moveResources()");
		String newEntityId = null;
		Object item2move = params.get("item2move");
		if(item2move == null) {
			logger.info("moveResources() no selectedMembers submitted");
			throw new IllegalArgumentException("No item to move selected");
			
		} else if(item2move instanceof String[]) {
			
			for(String itemId : (String[]) item2move) {	
				try {
					newEntityId = moveResourceIntoFolder(itemId, destinationId);
					logger.info("moveResources() move " + itemId + " to " + destinationId + " ==> " + newEntityId);
				} catch(RuntimeException e) {
					String msg = "Unable to move " + item2move + " to " + destinationId + " ==> " + e;
					logger.debug(msg);
					throw e;
				}
			}
			
		} else if(item2move instanceof String) {
			try {
				newEntityId = moveResourceIntoFolder((String) item2move, destinationId);
				logger.info("moveResources() move " + item2move + " to " + destinationId + " ==> " + newEntityId);
			} catch(RuntimeException e) {
				String msg = "Unable to move " + item2move + " to " + destinationId + " ==> " + e;
				logger.debug(msg);
				throw e;
			}
			
		} else {
			throw new IllegalStateException("Cannot process item to move of type " + item2move.getClass().getCanonicalName());
		}
		return newEntityId;
	}

	/**
	 * curl -u "admin:admin" -F "mode=create" -F "actionId=paste_copied" 
	 * 		-F "item2copy=/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFile.sql" 
	 * 		-F "containingCollectionId=/group/9c57a615-c92a-4879-baaf-3ec8df262b16/myFolder/"  
	 * 		http://localhost:8080/direct/contentActions?__auth=basic
	 * 
	 * @param item
	 * @param params
	 * @return
	 * @throws IllegalArgumentException if the entity could not be updated because of missing or invalid data or could not find entity to update 
	 * @throws SecurityException if permissions prevented this entity from being updated 
	 * @throws IllegalStateException for all other failures
	 */
	protected String copyResources(String destinationId, Map<String, Object> params) {
		logger.debug("copyResources()");
		String newEntityId = null;
		Object item2copy = params.get("item2copy");
		
		if(item2copy == null) {
			logger.info("copyResources() no selectedMembers submitted");
			throw new IllegalArgumentException("No item to copy selected");
			
		} else if(item2copy instanceof String[]) {
			for(String itemId : (String[]) item2copy) {
				try {
					newEntityId = copyResourceIntoFolder(itemId, destinationId);
					logger.info("copyResources() copy " + itemId + " to " + destinationId + " ==> " + newEntityId);
				} catch(RuntimeException e) {
					String msg = "Unable to copy " + item2copy + " to " + destinationId + " ==> " + e;
					logger.debug(msg);
					throw e;
				}
			}
			
		} else if(item2copy instanceof String) {
			try {
				newEntityId = copyResourceIntoFolder((String) item2copy, destinationId);
				logger.info("copyResources() copy " + item2copy + " to " + destinationId + " ==> " + newEntityId);
			} catch(RuntimeException e) {
				String msg = "Unable to copy " + item2copy + " to " + destinationId + " ==> " + e;
				logger.debug(msg);
				throw e;
			}
			
		} else {
			throw new IllegalStateException("Cannot process item to copy of type " + item2copy.getClass().getCanonicalName());
		}
		return newEntityId;
	}

	/**
	 * @param params
	 * @param dateName
	 * @return
	 */
	protected Time extractTime(Map<String, Object> params, String dateName) {
		Time datetime = null;
		int year = extractInt((String) params.get(dateName + ".year"));
		int month = extractInt((String) params.get(dateName + ".month")); 
		int day = extractInt((String) params.get(dateName + ".day"));
		int hour = extractInt((String) params.get(dateName + ".hour"));
		int minute = extractInt((String) params.get(dateName + ".minute"));
		int second = extractInt((String) params.get(dateName + ".second"));
		String ampm = (String) params.get(dateName + ".ampm");
		
		if(ampm != null && ampm.equalsIgnoreCase("pm")) {
			hour += 12;
		}
		datetime = timeService.newTimeLocal(year, month, day, hour, minute, second, 0);
		return datetime;
	}

	/**
	 * @param numStr
	 * @return
	 */
	protected int extractInt(String numStr) {
		int year;
		int num = 0;
		if(numStr != null) {
			try {
				num = Integer.parseInt(numStr);
			} catch(Exception e) {
				logger.debug("Error parsing int from String: " + numStr);
			}
		}
		year = num;
		return year;
	}

	/**
	 * @param notifyCode
	 * @return
	 */
	protected int calculateNotification(String notifyCode) {
		int notify = NotificationService.NOTI_NONE;
		if("r".equalsIgnoreCase(notifyCode)) {
			notify = NotificationService.NOTI_REQUIRED;
		} else if ("o".equalsIgnoreCase(notifyCode)) {
			notify = NotificationService.NOTI_OPTIONAL;
		}
		return notify;
	}
	
	/**
	 * 
	 * @param collectionId
	 * @param displayName
	 * @return
	 */
	public ContentCollection createContentCollection(String collectionId, String displayName) {
		ContentCollectionEdit edit = null;
		try {
			edit = contentService.addCollection(collectionId, displayName);
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
			edit.setResourceType(ResourceType.TYPE_FOLDER);
			contentService.commitCollection(edit);
			
		} catch (PermissionException e) {
			throw new SecurityException(e);
			
		} catch (IdInvalidException e) {
			throw new IllegalArgumentException(e);
			
		} catch (IdLengthException e) {
			throw new IllegalArgumentException(e);
			
		} catch (TypeException e) {
			throw new IllegalArgumentException(e);
			
		} catch (IdUnusedException e) {
			throw new IllegalStateException(e);
			
		} catch (IdUsedException e) {
			throw new IllegalStateException(e);
		}
		
		return edit;
	}
	
	/**
	 * 
	 * @param collectionId
	 * @param displayName
	 * @param content
	 * @param contentType
	 * @param resourceType
	 * @param notify
	 * @return
	 */
	public ContentResource createContentResource(String collectionId,
			String displayName, byte[] content, String contentType, String resourceType, String notify) {
		
		// notification
		int noti = NotificationService.NOTI_NONE;
		// read the notification options
		if ("r".equals(notify))	{
			noti = NotificationService.NOTI_REQUIRED;
			
		} else if ("o".equals(notify))	{
			noti = NotificationService.NOTI_OPTIONAL;
		}
		
		String[] nameParts = splitDisplayName(displayName);

		ContentResourceEdit edit = null;
		
		try {
			edit = contentService.addResource(collectionId, nameParts[0], nameParts[1], this.contentService.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
			edit.setContent(content);
			edit.setContentLength(content.length);
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
			edit.setContentType(contentType);
			edit.setResourceType(resourceType);
			contentService.commitResource(edit, noti);
			
		} catch (PermissionException e) {
			throw new SecurityException(e);
			
		} catch (IdInvalidException e) {
			throw new IllegalArgumentException(e);
			
		} catch (IdLengthException e) {
			throw new IllegalArgumentException(e);
			
		} catch (IdUnusedException e) {
			throw new IllegalStateException(e);
			
		} catch (IdUniquenessException e) {
			throw new IllegalStateException(e);
			
		} catch (OverQuotaException e) {
			throw new IllegalStateException(e);
			
		} catch (ServerOverloadException e) {
			throw new IllegalStateException(e);
		}
		return edit;
	}

	/**
	 * 
	 * @param collectionId
	 * @param displayName
	 * @param stream
	 * @param contentType
	 * @param resourceType
	 * @param notify
	 * @return
	 */
	public ContentResource createContentResource(String collectionId,
			String displayName, InputStream stream, String contentType, String resourceType, String notify) {
		// notification
		int noti = NotificationService.NOTI_NONE;
		// read the notification options
		if ("r".equals(notify))	{
			noti = NotificationService.NOTI_REQUIRED;
			
		} else if ("o".equals(notify)) {
			noti = NotificationService.NOTI_OPTIONAL;
		}

		String[] nameParts = splitDisplayName(displayName);

		ContentResourceEdit edit = null;
		try {
			// TODO: permission exception because no session/user is available in CHS or security service 
			// possible approach: get session and create security adviser that checks permission in to create resource in collection
			
			edit = contentService.addResource(collectionId, nameParts[0], nameParts[1], this.contentService.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
			edit.setContent(stream);
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
			
			edit.setContentType(contentType);
			edit.setResourceType(resourceType);
			contentService.commitResource(edit, noti);
			
		} catch (PermissionException e) {
			throw new SecurityException(e);
			
		} catch (IdInvalidException e) {
			throw new IllegalArgumentException(e);
			
		} catch (IdLengthException e) {
			throw new IllegalArgumentException(e);
			
		} catch (IdUnusedException e) {
			throw new IllegalStateException(e);
			
		} catch (IdUniquenessException e) {
			throw new IllegalStateException(e);
			
		} catch (OverQuotaException e) {
			throw new IllegalStateException(e);
			
		} catch (ServerOverloadException e) {
			throw new IllegalStateException(e);
		}
		return edit;
	}
	
	/**
	 * 
	 * @param contentEntityId
	 * @param destinationCollectionId
	 * @return
	 */
	public String moveResourceIntoFolder(String contentEntityId, String destinationCollectionId) {
		logger.info("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ")");
		String newEntityId = null;
		try {
			newEntityId = contentService.moveIntoFolder(contentEntityId, destinationCollectionId);
		
		} catch (PermissionException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new SecurityException("Could not move " + contentEntityId, e);
			
		} catch (IdUnusedException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not move " + contentEntityId, e);
			
		} catch (TypeException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not move " + contentEntityId, e);
			
		} catch (InUseException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not move " + contentEntityId, e);
			
		} catch (OverQuotaException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalStateException("Could not move " + contentEntityId, e);
			
		} catch (IdUsedException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not move " + contentEntityId, e);
			
		} catch (InconsistentException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not move " + contentEntityId, e);
			
		} catch (ServerOverloadException e)  {
			logger.warn("moveResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalStateException("Could not move " + contentEntityId, e);
		} 
		
		return newEntityId;
	}
	
	/**
	 * 
	 * @param contentEntityId
	 * @param destinationCollectionId
	 * @return
	 */
	public String copyResourceIntoFolder(String contentEntityId, String destinationCollectionId) {
		logger.info("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ")");
		String newEntityId = null;
		try {
			newEntityId = contentService.copyIntoFolder(contentEntityId, destinationCollectionId);
			
		} catch (PermissionException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new SecurityException("Could not copy " + contentEntityId, e);
			
		} catch (IdUnusedException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not copy " + contentEntityId, e);
			
		} catch (IdUniquenessException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not copy " + contentEntityId, e);
			
		} catch (TypeException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not copy " + contentEntityId, e);
			
		} catch (InUseException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not copy " + contentEntityId, e);
			
		} catch (IdLengthException e) {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not copy " + contentEntityId, e);
			
		}catch (OverQuotaException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalStateException("Could not copy " + contentEntityId, e);
			
		} catch (IdUsedException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not copy " + contentEntityId, e);
			
		} catch (InconsistentException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalArgumentException("Could not copy " + contentEntityId, e);
			
		} catch (ServerOverloadException e)  {
			logger.warn("copyResourceIntoFolder(" + contentEntityId + ", " + destinationCollectionId + ") ", e);
			throw new IllegalStateException("Could not copy " + contentEntityId, e);
		} 
		
		return newEntityId;
	}
	
	/**
	 * 
	 * @param entityId
	 * @return
	 */
	public ContentEntity getContentEntity(String entityId) {
		entityId = decodeEntityId(entityId);
		String refStr = "/content" + entityId;
		
		Reference ref = entityManager.newReference(refStr);
		
		return (ContentEntity) contentService.getEntity(ref);
	}

	/**
	 * 
	 * @param displayName
	 * @return
	 */
	protected String[] splitDisplayName(String displayName) {
		String[] parts = new String[2];
		if(displayName.contains(PERIOD)) {
			int pos = displayName.lastIndexOf(PERIOD);
			parts[0] = displayName.substring(0, pos);
			parts[1] = displayName.substring(pos + 1);
		} else {
			parts[0] = displayName;
			parts[1] = "";
		}
		return parts;
	}
	/**
	 * 
	 * @param entityId
	 * @return
	 */
	public String decodeEntityId(String entityId) {
		if(entityId != null && entityId.startsWith("%")) {
			try {
				entityId = URLDecoder.decode(entityId, ENC_UTF8);
			} catch (Exception e) {
				logger.warn("Problem decoding entityId: " + entityId, e);
			}
		}
		return entityId;
	}
	
	/**
	 * 
	 * @param entityId
	 * @return
	 */
	public String encodeEntityId(String entityId) {
    	if(entityId != null && ! entityId.startsWith("%")) {
	    	try {
				entityId = URLEncoder.encode(entityId, "UTF-8");
			} catch (Exception e) {
				logger.warn("Problem encoding entityId " + entityId, e);
			}
    	}
    	return entityId;
    }
	
	/**
	 * 
	 * @param sessionId
	 */
	public void setSession(String sessionId) {
		String[] parts = sessionId.split("\\.");
		if(parts == null || parts.length < 1) {
			// do nothing
		} else {
			sessionId = parts[0];
		}
		Session session = sessionManager.getSession(sessionId);
		
		if(session == null) {
			logger.warn("Unable to find Session: " + sessionId, new Exception());
		} else {
			try {
				sessionManager.setCurrentSession(session);
			} catch(Exception e) {
				logger.warn("Unable to find Session: " + sessionId, e);
			}
		}
	}
	
    /**
     * @param reference
     * @return
     */
    protected static String findId(String reference) 
    {
        String id = null;
        int spos = getSeparatorPos(reference, 1);
        if (spos != -1) {
            //int spos2 = reference.lastIndexOf(PERIOD);
            //id = spos2 == -1 ? reference.substring(spos + 1) : reference.substring(spos + 1, spos2);
            id = reference.substring(spos);
        }
        return id;
    }
    
    /**
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional id
     * @param number this is the separator to get,
     * 0 would return the first one found, 1 would return the second
     * @return the location of the separator between the entity and the id or -1 if none found
     */
    protected static int getSeparatorPos(String reference, int number) {
    	//EntityReference.checkReference(reference);
        int position = 0;
        for (int i = 0; i < number; i++) {
            position = reference.indexOf(EntityReference.SEPARATOR, position+1);
            if (position < 0) {
                break;
            }
        }
        return position;
    }
    
    /**
	 * 
	 * @param entityId
	 * @return
	 */
	public ContentCollectionEdit editContentCollection(String entityId) {
		ContentCollectionEdit edit = null;
		try {
			edit = contentService.editCollection(entityId);
			
		} catch (PermissionException e) {
			logger.debug("Error getting collection edit: ", e);
			throw new SecurityException(e);
			
		} catch (IdUnusedException e) {
			logger.debug("Error getting collection edit: ", e);
			throw new IllegalStateException(e);
			
		} catch (TypeException e) {
			logger.debug("Error getting collection edit: ", e);
			throw new IllegalStateException(e);
			
		} catch (InUseException e) {
			logger.debug("Error getting collection edit: ", e);
			throw new IllegalStateException(e);
		}
		return edit;
	}
	
	/**
	 * 
	 * @param entityId
	 * @return
	 */
	public ContentResourceEdit editContentResource(String entityId) {
		ContentResourceEdit edit = null;
		try {
			edit = contentService.editResource(entityId);
			
		} catch (PermissionException e) {
			logger.debug("Error getting resource edit: ", e);
			throw new SecurityException(e);
			
		} catch (IdUnusedException e) {
			logger.debug("Error getting resource edit: ", e);
			throw new IllegalStateException(e);
			
		} catch (TypeException e) {
			logger.debug("Error getting resource edit: ", e);
			throw new IllegalStateException(e);
			
		} catch (InUseException e) {
			logger.debug("Error getting resource edit: ", e);
			throw new IllegalStateException(e);
		}
		return edit;
	}

}
