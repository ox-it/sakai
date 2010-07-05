package uk.ac.ox.oucs.vle;

import java.util.Date;

import uk.ac.ox.oucs.vle.CourseSignupServiceImpl;

/**
 * Test version of the service.
 * @author buckett
 *
 */
public class CourseSignupServiceTest extends CourseSignupServiceImpl {

	private Date now = new Date();
	
	public void setNow(Date now) {
		this.now = now;
	}
	
	@Override
	public Date getNow() {
		return now;
	}
	
}
