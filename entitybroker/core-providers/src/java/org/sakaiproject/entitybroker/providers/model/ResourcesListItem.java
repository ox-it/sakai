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

package org.sakaiproject.entitybroker.providers.model;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;

/**
 * 
 *
 */
public class ResourcesListItem 
{
	private static Log logger = LogFactory.getLog(ResourcesListItem.class);

	protected ContentEntity entity;
	
	protected String accessLabel;
	protected String accessMode;
	protected String accessUrl;
	protected String addActionListUrl;
	protected String allActionListUrl;
	protected boolean available;
	protected List breadcrumbs;
	protected String collectionUrl;
	protected String containingCollectionId;
	protected Object content;
	protected String contentEntityId;
	protected InputStream contentStream;
	protected String contentType;
	protected String context;
	protected String copyright;
	protected boolean copyrightAlert = false;
	protected Map<String, String> createdDate;
	protected Map<String, String> creator;
	protected String description;
	protected String directUrl;
	protected Map<String, String> displayAttributes = new HashMap<String, String>();
	protected String entityId;
	protected Map<String, Map<String, String>> groups;
	protected boolean hasQuota = false;
	protected boolean hidden;
	protected String imageUrl;
	protected String itemActionListUrl;
	protected String inheritedAccessMode;
	protected Map<String, Map<String, String>> inheritedGroups;
	protected boolean inheritingPubView;
	protected String longAccessLabel;
	protected Map<String, String> modifiedDate;
	protected Map<String, String> modifier;
	protected String multiItemActionListUrl;
	protected String name;
	protected Map<String, Map<String, String>> possibleGroups;
	protected boolean pubView;
	protected long quota = Long.MIN_VALUE;
	protected Map<String, String> releaseDate;
	protected String resourceType;
	protected Map<String, String> retractDate;
	protected Map<String, Object> size;
	protected boolean useReleaseDate = false;
	protected boolean useRetractDate = false;
	protected String webdavUrl;
	protected String webdavSiteUrl;
	protected int hashedId;

	/**
	 * 
	 */
	public ResourcesListItem() {
	}

	/**
	 * Add a temporary attribute to affect display of the entity one time only.
	 * Display attributes are not persisted. They may be added to the entity for
	 * use during its existence from construction to garbage collection.
	 * 
	 * @param key
	 * @param value
	 */
	public void addDisplayAttribute(String key, String value) {
		if (key == null) {
			// do nothing
		} else if (value == null) {
			this.displayAttributes.remove(key);
		} else {
			this.displayAttributes.put(key, value);
		}
	}

	/**
	 * @param url
	 */
	public void addImageUrl(String url) {
		this.imageUrl = url;
	}

	/**
	 * @return the accessLabel
	 */
	public String getAccessLabel() {
		return accessLabel;
	}

	/**
	 * @return the accessMode
	 */
	public String getAccessMode() {
		return accessMode;
	}

	/**
	 * @return the accessUrl
	 */
	public String getAccessUrl() {
		return accessUrl;
	}

	/**
	 * @return the actionListUrl
	 */
	public String getAddActionListUrl() {
		return addActionListUrl;
	}

	/**
	 * @return the allActionListUrl
	 */
	public String getAllActionListUrl() {
		return allActionListUrl;
	}

	/**
	 * @return the breadcrumbs
	 */
	public List getBreadcrumbs() {
		return breadcrumbs;
	}

	/**
	 * @return the collectionUrl
	 */
	public String getCollectionUrl() {
		return collectionUrl;
	}

	/**
	 * @return the containingCollectionId
	 */
	public String getContainingCollectionId() {
		return containingCollectionId;
	}

	/**
	 * @return the content
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * @return the contentEntityId
	 */
	public String getContentEntityId() {
		return contentEntityId;
	}

	/**
	 * @param contentEntityId the contentEntityId to set
	 */
	public void setContentEntityId(String contentEntityId) {
		this.contentEntityId = contentEntityId;
	}

	/**
	 * @return
	 */
	public String getContentType() {
		return this.contentType;
	}

	/** * @return the copyright */
	public String getCopyright() {
		return copyright;
	}

	/** * @return the createdDate */
	public Map<String, String> getCreatedDate() {
		return createdDate;
	}

	/** * @return the creator */
	public Map<String, String> getCreator() {
		return creator;
	}

	/** * @return the description */
	public String getDescription() {
		return description;
	}

	/** * @return the directUrl */
	public String getDirectUrl() {
		return directUrl;
	}

	/** * @return the displayAttributes */
	public Map<String, String> getDisplayAttributes() {
		return displayAttributes;
	}

	/** * @return the entityId */
	public String getEntityId() {
		return entityId;
	}

	/** * @return the groups */
	public Map<String, Map<String, String>> getGroups() {
		return groups;
	}

	/** * @return the imageUrl */
	public String getImageUrl() {
		return imageUrl;
	}

	/** * @return the inheritedAccessMode */
	public String getInheritedAccessMode() {
		return inheritedAccessMode;
	}

	/** * @return the inheritedGroups */
	public Map<String, Map<String, String>> getInheritedGroups() {
		return inheritedGroups;
	}

	/** * @return the itemActionListUrl */
	public String getItemActionListUrl() {
		return itemActionListUrl;
	}

	/** * @return the longAccessLabel */
	public String getLongAccessLabel() {
		return longAccessLabel;
	}

	/** * @return the modifiedDate */
	public Map<String, String> getModifiedDate() {
		return modifiedDate;
	}

	/** * @return the modifier */
	public Map<String, String> getModifier() {
		return modifier;
	}

	/** * @return the multiItemActionListUrl */
	public String getMultiItemActionListUrl() {
		return multiItemActionListUrl;
	}

	/** * @return the name */
	public String getName() {
		return name;
	}

	/** * @return the possibleGroups */
	public Map<String, Map<String, String>> getPossibleGroups() {
		return possibleGroups;
	}

	/** * @return the quota */
	public long getQuota() {
		return quota;
	}

	/** * @return the releaseDate */
	public Map<String, String> getReleaseDate() {
		return releaseDate;
	}

	/** * @return the resourceType */
	public String getResourceType() {
		return resourceType;
	}

	/** * @return the retractDate */
	public Map<String, String> getRetractDate() {
		return retractDate;
	}

	/** * @return the size */
	public Map<String, Object> getSize() {
		return size;
	}

	/** * @return the available */
	public boolean isAvailable() {
		return available;
	}

	public boolean isCollection() {
		return ResourceType.TYPE_FOLDER.equalsIgnoreCase(this.resourceType);
	}

	/** * @return the copyrightAlert */
	public boolean isCopyrightAlert() {
		return copyrightAlert;
	}

	/** * @return the hasQuota */
	public boolean isHasQuota() {
		return hasQuota;
	}

	/** * @return the hidden */
	public boolean isHidden() {
		return hidden;
	}

	/** * @return the inheritingPubView */
	public boolean isInheritingPubView() {
		return inheritingPubView;
	}

	/** * @return the pubView */
	public boolean isPubView() {
		return pubView;
	}

	/**
	 * @return the useReleaseDate
	 */
	public boolean isUseReleaseDate() {
		return useReleaseDate;
	}

	/**
	 * @return the useRetractDate
	 */
	public boolean isUseRetractDate() {
		return useRetractDate;
	}

	/**
	 * @param accessMode
	 *            the accessMode to set
	 */
	public void setAccessMode(String accessMode) {
		this.accessMode = accessMode;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @param copyright
	 *            the copyright to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * @param copyrightAlert
	 *            the copyrightAlert to set
	 */
	public void setCopyrightAlert(boolean copyrightAlert) {
		this.copyrightAlert = copyrightAlert;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param hasQuota
	 *            the hasQuota to set
	 */
	public void setHasQuota(boolean hasQuota) {
		this.hasQuota = hasQuota;
	}

	/**
	 * @param hidden
	 *            the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * *
	 * 
	 * @param pubView the pubView to set
	 */
	public void setPubView(boolean pubView) {
		this.pubView = pubView;
	}

	/**
	 * @param quota
	 *            the quota to set
	 */
	public void setQuota(long quota) {
		this.quota = quota;
	}

	/**
	 * @param releaseDate
	 *            the releaseDate to set
	 */
	public void setReleaseDate(Map<String, String> releaseDate) {
		this.releaseDate = releaseDate;
	}

	/**
	 * @param resourceType
	 *            the resourceType to set
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * @param retractDate
	 *            the retractDate to set
	 */
	public void setRetractDate(Map<String, String> retractDate) {
		this.retractDate = retractDate;
	}

	/**
	 * @param useReleaseDate
	 *            the useReleaseDate to set
	 */
	public void setUseReleaseDate(boolean useReleaseDate) {
		this.useReleaseDate = useReleaseDate;
	}

	/**
	 * @param useRetractDate
	 *            the useRetractDate to set
	 */
	public void setUseRetractDate(boolean useRetractDate) {
		this.useRetractDate = useRetractDate;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * @return the webdavUrl
	 */
	public String getWebdavUrl() {
		return webdavUrl;
	}

	/**
	 * @param webdavUrl the webdavUrl to set
	 */
	public void setWebdavUrl(String webdavUrl) {
		this.webdavUrl = webdavUrl;
	}

	/**
	 * @return the webdavSiteUrl
	 */
	public String getWebdavSiteUrl() {
		return webdavSiteUrl;
	}

	/**
	 * @param webdavSiteUrl the webdavSiteUrl to set
	 */
	public void setWebdavSiteUrl(String webdavSiteUrl) {
		this.webdavSiteUrl = webdavSiteUrl;
	}

	/**
	 * @return the hashedId
	 */
	public int getHashedId() {
		return hashedId;
	}

	/**
	 * @param hashedId the hashedId to set
	 */
	public void setHashedId(int hashedId) {
		this.hashedId = hashedId;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append('\n');
		buf.append(super.toString());
		buf.append("\n	\t entityId: ");
		buf.append(entityId);
		buf.append("\n\t containingCollectionId: ");
		buf.append(containingCollectionId);
		buf.append("\n\t entity: ");
		buf.append(entity);
		buf.append("\n\t content	: ");
		buf.append(content);
		buf.append("\n\t accessUrl: ");
		buf.append(accessUrl);
		buf.append("\n\t createdDate	: ");
		buf.append(createdDate);
		buf.append("\n\t creator: ");
		buf.append(creator);
		buf.append("\n\t description: 	");
		buf.append(description);
		buf.append("\n\t imageUrl: ");
		buf.append(imageUrl);
		buf.append("\n\t modifiedDate: ");
		buf.append(modifiedDate);
		buf.append("\n\t modifier: ");
		buf.append(modifier);
		buf.append(name);
		buf.append("\n\t resourceType: ");
		buf.append(resourceType);
		buf.append("\n\t size: ");
		buf.append(size);
		buf.append('\n');

		return buf.toString();
	}

	public void updateSize(Map<String, Object> size2) {
		this.size = size2;
	}

	/**
	 * @param groupObjects
	 */
	protected Map<String, Map<String, String>> extractGroups(
			Collection<Group> groupObjects) {
		Map<String, Map<String, String>> groups = new HashMap<String, Map<String, String>>();
		for (Group group : groupObjects) {
			Map<String, String> groupInfo = new HashMap<String, String>();
			groupInfo.put("id", group.getId());
			groupInfo.put("title", group.getTitle());
			groupInfo.put("description", group.getDescription());
			groups.put(group.getId(), groupInfo);
		}
		return groups;
	}

	protected Map<String, Object> extractSize(ContentCollection collection) {
		Map<String, Object> map = new HashMap<String, Object>();
		// folders come before other types when sorting by size
		map.put("sortGroup", 1);
		map.put("value", Integer.toString(collection.getMemberCount()));
		return map;
	}

	protected Map<String, Object> extractSize(ContentResource resource) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value", Long.toString(resource.getContentLength()));
		if (ResourceType.TYPE_URL.equalsIgnoreCase(resource.getResourceType())) {
			// URL's after folders and uploads etc when sorting by size
			map.put("sortGroup", "8");
		} else {
			// uploads, text files, html files, etc , come after folders
			// whensorting by size
			map.put("sortGroup", 4);
		}
		return map;
	}

	/**
	 * @param time
	 * @return
	 */
	protected Map<String, String> extractTime(Time time) {
		Map<String, String> map = new HashMap<String, String>();
		if (time != null) {
			TimeBreakdown tb = time.breakdownLocal();

			map.put("year", Integer.toString(tb.getYear()));
			map.put("month", Integer.toString(tb.getMonth()));
			map.put("day", Integer.toString(tb.getDay()));
			map.put("hour", Integer.toString(tb.getHour()));
			map.put("minute", Integer.toString(tb.getMin()));
			map.put("second", Integer.toString(tb.getSec()));
			map.put("millisecond", Integer.toString(tb.getMs()));
			boolean am = true;
			int hour12 = tb.getHour();
			if (hour12 >= 12) {
				am = false;
			}
			if (hour12 > 12) {
				hour12 -= 12;
			}
			map.put("hour12", Integer.toString(hour12));
			map.put("am", Boolean.toString(am));
			map.put("display", time.getDisplay());
			map.put("gmtFull", time.toStringGmtFull());
			map.put("localFull", time.toStringLocalFull());
			map.put("localTimeZ", time.toStringLocalTimeZ());
			map.put("localDate", time.toStringLocalDate());
			map.put("localFullZ", time.toStringLocalFullZ());
			map.put("systemTime", Long.toString(time.getTime()));
		}
		return map;
	}
}
