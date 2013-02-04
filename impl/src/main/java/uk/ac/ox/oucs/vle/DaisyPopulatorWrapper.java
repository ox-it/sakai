package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

public class DaisyPopulatorWrapper implements PopulatorWrapper {
	
	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}

	/**
	 * 
	 */
	private SakaiProxy proxy;
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	/**
	 * 
	 */
	private Populator populator;
	public void setPopulator(Populator populator) {
		this.populator = populator;
	}
	
	private static final Log log = LogFactory.getLog(DaisyPopulatorWrapper.class);
	
	/**
	 * 
	 */
	public void update(PopulatorContext context) {
		
		PopulatorLogWriter dWriter = null;
		PopulatorLogWriter eWriter = null;
		PopulatorLogWriter iWriter = null;
		
		try {
			ByteArrayOutputStream dOut = new ByteArrayOutputStream();
			ByteArrayOutputStream eOut = new ByteArrayOutputStream();
			ByteArrayOutputStream iOut = new ByteArrayOutputStream();
			
			dWriter = new XcriLogWriter(dOut, context.getName()+"ImportDeleted");
			context.setDeletedLogWriter(dWriter);
			
			eWriter = new XcriLogWriter(eOut, context.getName()+"ImportError");
			context.setErrorLogWriter(eWriter);
			
			iWriter = new XcriLogWriter(iOut, context.getName()+"ImportInfo");
			context.setInfoLogWriter(iWriter);
		
			/*
			dao.flagSelectedDaisyCourseGroups(context.getName());
			dao.flagSelectedDaisyCourseComponents(context.getName());
			*/
            populator.update(context);
            
            /*
            Collection<CourseGroupDAO> groups = dao.deleteSelectedCourseGroups(context.getName());
            for (CourseGroupDAO group : groups) {
            	writer.write("Deleting course ["+group.getCourseId()+" "+group.getTitle()+"]"+"\n");
            }
            
            Collection<CourseComponentDAO> components = dao.deleteSelectedCourseComponents(context.getName());
            for (CourseComponentDAO component : components) {
            	writer.write("Deleting component ["+component.getComponentId()+" "+component.getTitle()+"]"+"\n");
            }
            */
            dWriter.footer();
            dWriter.flush();
			proxy.writeLog(dWriter.getIdName(), dWriter.getDisplayName(), dOut.toByteArray());
			
			eWriter.footer();
			eWriter.flush();
			proxy.writeLog(eWriter.getIdName(), eWriter.getDisplayName(), eOut.toByteArray());
			
			iWriter.footer();
			iWriter.flush();
			proxy.writeLog(iWriter.getIdName(), iWriter.getDisplayName(), iOut.toByteArray());

		} catch (PopulatorException e) {
        	log.error("PopulatorException ["+context.getURI()+"]", e);
        	
        } catch (IllegalStateException e) {
        	log.error("IllegalStateException ["+context.getURI()+"]", e);
        	
        } catch (IOException e) {
			log.error("Failed to write content to logfile.", e);
			
        } catch (VirusFoundException e) {
        	log.error("VirusFoundException ["+context.getURI()+"]", e);
        	
		} catch (OverQuotaException e) {
			log.error("OverQuotaException ["+context.getURI()+"]", e);
			
		} catch (ServerOverloadException e) {
			log.error("ServerOverloadException ["+context.getURI()+"]", e);
			
		} catch (PermissionException e) {
			log.error("PermissionException ["+context.getURI()+"]", e);
			
		} catch (TypeException e) {
			log.error("TypeException ["+context.getURI()+"]", e);
			
		} catch (InUseException e) {
			log.error("InUseException ["+context.getURI()+"]", e);
			
		} finally {
			if (null != dWriter) {
				try {
					dWriter.close();
					
				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}
			
			if (null != eWriter) {
				try {
					eWriter.close();
					
				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}
			
			if (null != iWriter) {
				try {
					iWriter.close();
					
				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}
		}   
	}
	
}
