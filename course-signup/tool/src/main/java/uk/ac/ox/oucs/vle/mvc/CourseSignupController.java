/*
 * #%L
 * Course Signup Webapp
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle.mvc;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import uk.ac.ox.oucs.vle.CourseSignupService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CourseSignupController extends AbstractController {

	private UserDirectoryService userDirectoryService;
	public void setUserService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private CourseSignupService courseSignupService;
	public void setCourseSignupService(CourseSignupService courseSignupService) {
		this.courseSignupService = courseSignupService;
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			 HttpServletResponse response) throws Exception {

		ModelAndView modelAndView = new ModelAndView(request.getPathInfo());
		modelAndView.addObject("externalUser", 
				userDirectoryService.getAnonymousUser().equals(userDirectoryService.getCurrentUser()));

		modelAndView.addObject("isAdministrator", 
				!courseSignupService.getAdministering().isEmpty());

		modelAndView.addObject("isApprover",
				!courseSignupService.getApprovals().isEmpty());
		
		modelAndView.addObject("isPending",
				!courseSignupService.getPendings().isEmpty());

		modelAndView.addObject("isLecturer",
				!courseSignupService.getLecturing().isEmpty());
		
		modelAndView.addObject("skinRepo",
				serverConfigurationService.getString("skin.repo", "/library/skin"));
		
		modelAndView.addObject("skinDefault",
				serverConfigurationService.getString("skin.default", "default"));

		modelAndView.addObject("skinPrefix",
				serverConfigurationService.getString("portal.neoprefix", ""));
		
		return modelAndView;
	}
}
