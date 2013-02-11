package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.util.Collection;

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

	@Override
	void runPopulator(PopulatorContext context) throws IOException {
		
		/* WL-2678 Comment out course deletion
		dao.flagSelectedDaisyCourseGroups(context.getName());
		dao.flagSelectedDaisyCourseComponents(context.getName());
		*/

		populator.update(context);

		/*
		Collection<CourseGroupDAO> groups = dao.deleteSelectedCourseGroups(context.getName());
		for (CourseGroupDAO group : groups) {
			context.getDeletedLogWriter().write("Deleting course ["+group.getCourseId()+" "+group.getTitle()+"]"+"\n");
		}

		Collection<CourseComponentDAO> components = dao.deleteSelectedCourseComponents(context.getName());
		for (CourseComponentDAO component : components) {
			context.getDeletedLogWriter().write("Deleting component ["+component.getComponentId()+" "+component.getTitle()+"]"+"\n");
		}
		*/
	}

}
