package org.sakaiproject.hierarchy.tool.vm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/create*")
public class HelperCreationController extends HelperController {

    @Override
    public String getHelperId() {
        return "sakai.sitesetup";
    }

}
