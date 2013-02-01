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

public class OxcapPopulatorWrapper implements PopulatorWrapper {
	
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
	private Populator populator;
	public void setPopulator(Populator populator) {
		this.populator = populator;
	}
	
	/**
	 * 
	 */
	private SakaiProxy proxy;
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	private static final Log log = LogFactory.getLog(OxcapPopulatorWrapper.class);

	/**
	 * 
	 */
	public void update(PopulatorContext context) {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XcriLogWriter writer = null;
		
		try {
			writer = new XcriLogWriter(out, context.getName()+"ImportDeleted", "Deleted Groups and Components from SES Import", null);
			
			dao.flagSelectedCourseGroups(context.getName());
			dao.flagSelectedCourseComponents(context.getName());
			
            populator.update(context);
            
            Collection<CourseGroupDAO> groups = dao.deleteSelectedCourseGroups(context.getName());
            for (CourseGroupDAO group : groups) {
            	writer.write("Deleting course ["+group.getCourseId()+" "+group.getTitle()+"]"+"\n");
            }
            
            Collection<CourseComponentDAO> components = dao.deleteSelectedCourseComponents(context.getName());
            for (CourseComponentDAO component : components) {
            	writer.write("Deleting component ["+component.getComponentId()+" "+component.getTitle()+"]"+"\n");
            }
            
            writer.flush();
            writer.footer();
			proxy.writeLog(writer.getIdName(), writer.getDisplayName(), out.toByteArray());
			
		} catch (PopulatorException e) {
        	log.error("PopulatorException ["+context.getURI()+"]", e);
        	
        } catch (IllegalStateException e) {
        	log.error("IllegalStateException ["+context.getURI()+"]", e);
        	
        } catch (IOException e) {
        	log.error("IOException ["+context.getURI()+"]", e);
        	
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
			if (null != writer) {
				try {
					writer.close();
					
				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}
		}
       
	}
	
}
