package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

public class DaisyPopulatorWrapper extends BasePopulatorWrapper implements PopulatorWrapper {

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

	private static final Log log = LogFactory.getLog(DaisyPopulatorWrapper.class);

	@Override
	void runPopulator(PopulatorContext context) throws IOException {
		
		dao.flagSelectedDaisyCourseGroups(context.getName());
		dao.flagSelectedDaisyCourseComponents(context.getName());

		populator.update(context);

		Collection<CourseGroupDAO> groups = dao.deleteSelectedCourseGroups(context.getName());
		for (CourseGroupDAO group : groups) {
			context.getDeletedLogWriter().write("Deleting course ["+group.getCourseId()+" "+group.getTitle()+"]"+"\n");
		}

		Collection<CourseComponentDAO> components = dao.deleteSelectedCourseComponents(context.getName());
		for (CourseComponentDAO component : components) {
			context.getDeletedLogWriter().write("Deleting component ["+component.getComponentId()+" "+component.getTitle()+"]"+"\n");
		}
	}

}
