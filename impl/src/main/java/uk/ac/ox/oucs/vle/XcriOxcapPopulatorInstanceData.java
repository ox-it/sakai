package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;

public class XcriOxcapPopulatorInstanceData {

	private static final Log log = LogFactory.getLog(XcriOxcapPopulatorInstanceData.class);
	
	private ContentHostingService contentHostingService;
	
	ByteArrayOutputStream eOut;
	ByteArrayOutputStream iOut;
	
	private static XcriErrorWriter eWriter;
	private static XcriInfoWriter iWriter;
	
	private int departmentSeen;
	private int departmentCreated;
	private int departmentUpdated;
	private int subunitSeen;
	private int subunitCreated;
	private int subunitUpdated;
	private int groupSeen;
	private int groupCreated;
	private int groupUpdated;
	private int componentSeen;
	private int componentCreated;
	private int componentUpdated;
	
	private String lastGroup = null;
	
	private String siteId;
	
	private String feed;
	
	public XcriOxcapPopulatorInstanceData(ContentHostingService contentHostingService, String siteId, String feed, String generated) {
		
		this.contentHostingService = contentHostingService;
		this.siteId = siteId;
		this.feed = feed;
		
		eOut = new ByteArrayOutputStream();
		iOut = new ByteArrayOutputStream();
		
		try {
			
			eWriter = new XcriErrorWriter(eOut, feed, generated);
			iWriter = new XcriInfoWriter(iOut, feed, generated);
		
		} catch (IOException e) {
			log.warn("Failed to write content to logfile.", e);
		}
		
		departmentSeen = 0;
		departmentCreated = 0;
		departmentUpdated = 0;
		subunitSeen = 0;
		subunitCreated = 0;
		subunitUpdated = 0;
		groupSeen = 0;
		groupCreated = 0;
		groupUpdated = 0;
		componentSeen = 0;
		componentCreated = 0;
		componentUpdated = 0;
		
		lastGroup = null;
	}
	
	protected void finalise() {
		
		ContentResourceEdit cre = null;
		ContentResourceEdit cri = null;
		
		try {
			logMs("CourseDepartments (seen: "+ departmentSeen+ " created: "+ departmentCreated+ ", updated: "+ departmentUpdated+")");
			logMs("CourseSubUnits (seen: "+ subunitSeen+ " created: "+ subunitCreated+ ", updated: "+ subunitUpdated+")");
			logMs("CourseGroups (seen: "+ groupSeen+ " created: "+ groupCreated+ ", updated: "+ groupUpdated+")");
			logMs("CourseComponents (seen: "+ componentSeen+ " created: "+ componentCreated+ ", updated: "+ componentUpdated+")");

			eWriter.flush();
			eWriter.close();
		
			iWriter.flush();
			iWriter.close();
		
			if (null != contentHostingService) {
				
				String jsonResourceEId = contentHostingService.getSiteCollection(siteId)+ eWriter.getIdName();

				try {
					// editResource() doesn't throw IdUnusedExcpetion but PermissionException
					// when the resource is missing so we first just tco to find it.
					contentHostingService.getResource(jsonResourceEId);
					cre = contentHostingService.editResource(jsonResourceEId);
			
				} catch (IdUnusedException e) {
					try {
						cre = contentHostingService.addResource(jsonResourceEId);
						ResourceProperties props = cre.getPropertiesEdit();
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, eWriter.getDisplayName());
						cre.setContentType("text/html");
					} catch (Exception e1) {
						log.warn("Failed to create the import log file.", e1);
					}
				}
		
				cre.setContent(eOut.toByteArray());
				// Don't notify anyone about this resource.
				contentHostingService.commitResource(cre, NotificationService.NOTI_NONE);
			}
		
			if (null != contentHostingService) {
			
				String jsonResourceSId = contentHostingService.getSiteCollection(siteId)+ iWriter.getIdName();

				try {
					// editResource() doesn't throw IdUnusedExcpetion but PermissionException
					// when the resource is missing so we first just try to find it.
					contentHostingService.getResource(jsonResourceSId);
					cri = contentHostingService.editResource(jsonResourceSId);
			
				} catch (IdUnusedException e) {
					try {
						cri = contentHostingService.addResource(jsonResourceSId);
						ResourceProperties props = cri.getPropertiesEdit();
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, iWriter.getDisplayName());
						cri.setContentType("text/html");
					} catch (Exception e1) {
						log.warn("Failed to create the import log file.", e1);
					}
				}
		
				cri.setContent(iOut.toByteArray());
				// Don't notify anyone about this resource.
				contentHostingService.commitResource(cri, NotificationService.NOTI_NONE);
			}
		
		} catch (Exception e) {
			log.warn("Failed to write content to logfile.", e);
		
		} finally {
			if (null != cre && cre.isActiveEdit()) {
				contentHostingService.cancelResource(cre);
			}
			if (null != cri && cri.isActiveEdit()) {
				contentHostingService.cancelResource(cri);
			}
		}
	}
	
	protected String getFeed() {
		return this.feed;
	}
	
	protected void incrDepartmentSeen() {
		departmentSeen++;
	}
	
	protected void incrDepartmentCreated() {
		departmentCreated++;
	}
	
	protected void incrDepartmentUpdated() {
		departmentUpdated++;
	}
	
	protected void incrSubunitSeen() {
		subunitSeen++;
	}
	
	protected void incrSubunitCreated() {
		subunitCreated++;
	}
	
	protected void incrSubunitUpdated() {
		subunitUpdated++;
	}
	
	protected void incrGroupSeen() {
		groupSeen++;
	}
	
	protected void incrGroupCreated() {
		groupCreated++;
	}
	
	protected void incrGroupUpdated() {
		groupUpdated++;
	}
	
	protected void incrComponentSeen() {
		componentSeen++;
	}
	
	protected void incrComponentCreated() {
		componentCreated++;
	}
	
	protected void incrComponentUpdated() {
		componentUpdated++;
	}
	
	protected String getLastGroup() {
		return this.lastGroup;
	}
	
	protected void setLastGroup(String lastGroup) {
		this.lastGroup = lastGroup;
	}
	
	/**
	 * Log errors and warnings
	 * @param message
	 * @throws IOException
	 */
	protected static void logMe(String message) throws IOException {
		log.warn(message);
		if (null != eWriter) {
			eWriter.write(message+"\n");
		}
	}
	
	/**
	 * Log successes
	 * @param message
	 * @throws IOException
	 */
	protected static void logMs(String message) throws IOException {
		log.warn(message);
		if (null != iWriter) {
			iWriter.write(message+"\n");
		}
	}
	
}
