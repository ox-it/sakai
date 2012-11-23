package uk.ac.ox.oucs.vle;

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
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
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
	
	private static final Log log = LogFactory.getLog(OxcapPopulatorWrapper.class);

	/**
	 * 
	 */
	public void update(PopulatorContext context) {
		
		try {
			dao.flagSelectedCourseGroups(context.getName());
			dao.flagSelectedCourseComponents(context.getName());
			
            populator.update(context);
            
            dao.deleteSelectedCourseGroups(context.getName());
            dao.deleteSelectedCourseComponents(context.getName());
			
        } catch (IllegalStateException e) {
        	log.warn("IllegalStateException ["+context.getURI()+"]", e);
        }
       
	}
	
}
