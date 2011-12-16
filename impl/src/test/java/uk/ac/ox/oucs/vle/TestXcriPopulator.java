package uk.ac.ox.oucs.vle;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;

public class TestXcriPopulator extends TestOnSampleData {

	CourseDAOImpl courseDao;
	
	private final Date END_MIC_2010 = createDate(2010, 12, 4); 

	public void onSetUp() throws Exception {
		courseDao = (CourseDAOImpl) getApplicationContext().getBean("uk.ac.ox.oucs.vle.CourseDAO");
	}
	
	protected String[] getConfigPaths() {
		return new String[]{"/xcri-components.xml"};
	}
	
	private static Date createDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return new Date(cal.getTimeInMillis());
	}
	
	public void testUpdate() {
		XcriPopulatorImpl reader = new XcriPopulatorImpl();
		reader.update();
	}
	
}
