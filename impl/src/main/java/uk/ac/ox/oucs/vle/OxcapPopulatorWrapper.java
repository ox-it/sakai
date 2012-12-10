package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.FormattedText;
import org.xcri.Extension;
import org.xcri.common.Description;
import org.xcri.common.ExtensionManager;
import org.xcri.common.OverrideManager;
import org.xcri.core.Catalog;
import org.xcri.core.Course;
import org.xcri.core.Presentation;
import org.xcri.core.Provider;
import org.xcri.exceptions.InvalidElementException;

import uk.ac.ox.oucs.vle.xcri.daisy.Bookable;
import uk.ac.ox.oucs.vle.xcri.daisy.CourseSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentThirdLevelApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentalSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.DivisionWideEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeName;
import uk.ac.ox.oucs.vle.xcri.daisy.Identifier;
import uk.ac.ox.oucs.vle.xcri.daisy.ModuleApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.OtherDepartment;
import uk.ac.ox.oucs.vle.xcri.daisy.Sessions;
import uk.ac.ox.oucs.vle.xcri.daisy.SupervisorApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.TermCode;
import uk.ac.ox.oucs.vle.xcri.daisy.TermLabel;
import uk.ac.ox.oucs.vle.xcri.daisy.WebAuthCode;
import uk.ac.ox.oucs.vle.xcri.oxcap.MemberApplyTo;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapCourse;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapPresentation;
import uk.ac.ox.oucs.vle.xcri.oxcap.Session;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject;

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
					writer.flush();
					writer.close();
					
				} catch (IOException e) {
					log.error("IOException ["+context.getURI()+"]", e);
				}
			}
		}
       
	}
	
}
