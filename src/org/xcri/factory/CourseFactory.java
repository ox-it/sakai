package org.xcri.factory;

import org.xcri.common.OverrideManager;
import org.xcri.core.Course;

public class CourseFactory {
	
	public static Course getCourse(Class baseClass) throws InstantiationException, IllegalAccessException {
		if (OverrideManager.getOverrides().containsKey(baseClass)) {
			return (Course) OverrideManager.getOverrides().get(baseClass).getClass().newInstance();
		} else {
			return new Course();
		}
	}

}
