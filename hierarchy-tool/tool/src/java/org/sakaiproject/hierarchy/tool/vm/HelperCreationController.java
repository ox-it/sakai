package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.annotation.TargettedController;

@Controller("siteCreationHelper")
@TargettedController("sakai.hierarchy-manager-servlet.xml")
@RequestMapping("/create*")
public class HelperCreationController extends HelperController {

    @Override
    public String getHelperId() {
        return "sakai.sitesetup";
    }

}
