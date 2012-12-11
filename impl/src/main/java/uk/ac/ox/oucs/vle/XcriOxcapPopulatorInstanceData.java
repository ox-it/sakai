package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XcriOxcapPopulatorInstanceData {

	private static final Log log = LogFactory.getLog(XcriOxcapPopulatorInstanceData.class);
	
	private SakaiProxy proxy;
	
	ByteArrayOutputStream eOut;
	ByteArrayOutputStream iOut;
	
	private XcriLogWriter eWriter;
	private XcriLogWriter iWriter;
	
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
	
	private String feed;
	
	public XcriOxcapPopulatorInstanceData(SakaiProxy proxy, String feed, String generated) {
		
		this.proxy = proxy;
		this.feed = feed;
		
		eOut = new ByteArrayOutputStream();
		iOut = new ByteArrayOutputStream();
		
		try {
			eWriter = new XcriLogWriter(eOut, feed+"ImportError", "Errors and Warnings from SES Import",  generated);
			iWriter = new XcriLogWriter(iOut, feed+"ImportInfo", "Info and Warnings from SES Import", generated);
		
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
	
	protected void endTasks() {
		
		try {
			logMs("CourseDepartments (seen: "+ departmentSeen+ " created: "+ departmentCreated+ ", updated: "+ departmentUpdated+")");
			logMs("CourseSubUnits (seen: "+ subunitSeen+ " created: "+ subunitCreated+ ", updated: "+ subunitUpdated+")");
			logMs("CourseGroups (seen: "+ groupSeen+ " created: "+ groupCreated+ ", updated: "+ groupUpdated+")");
			logMs("CourseComponents (seen: "+ componentSeen+ " created: "+ componentCreated+ ", updated: "+ componentUpdated+")");

			eWriter.footer();
			iWriter.footer();
			
			proxy.writeLog(eWriter.getIdName(), eWriter.getDisplayName(), eOut.toByteArray());
			proxy.writeLog(iWriter.getIdName(), iWriter.getDisplayName(), iOut.toByteArray());
		
		} catch (Exception e) {
			log.warn("Failed to write content to logfile.", e);
			
		} finally {
			
			if (null != eWriter) {
				try {
					eWriter.flush();
					eWriter.close();
					
				} catch (IOException e) {
					log.error("IOException ["+e.getLocalizedMessage()+"]", e);
				}
			}
		
			if (null != iWriter) {
				try {
					iWriter.flush();
					iWriter.close();
					
				} catch (IOException e) {
					log.error("IOException ["+e.getLocalizedMessage()+"]", e);
				}
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
	protected void logMe(String message) throws IOException {
		if (null != eWriter) {
			eWriter.write(message+"\n");
		}
	}
	
	/**
	 * Log successes
	 * @param message
	 * @throws IOException
	 */
	protected void logMs(String message) throws IOException {
		if (null != iWriter) {
			iWriter.write(message+"\n");
		}
	}
	
}
