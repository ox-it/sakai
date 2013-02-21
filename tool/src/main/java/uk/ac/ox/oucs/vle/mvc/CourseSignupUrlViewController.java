package uk.ac.ox.oucs.vle.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.AbstractUrlViewController;

public class CourseSignupUrlViewController extends AbstractUrlViewController {

	@Override
	protected String getViewNameForRequest(HttpServletRequest request) {
		return request.getPathInfo();
	}

}
