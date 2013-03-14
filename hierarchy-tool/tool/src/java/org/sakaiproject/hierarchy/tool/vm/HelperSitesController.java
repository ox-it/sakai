package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.annotation.TargettedController;

@Controller("siteBrowserHelper")
@TargettedController("sakai.hierarchy-manager")
@RequestMapping("/sites*")
public class HelperSitesController extends HelperController {

	@Override
	public String getHelperId() {
		return "sakai.sitebrowser";
	}

}
