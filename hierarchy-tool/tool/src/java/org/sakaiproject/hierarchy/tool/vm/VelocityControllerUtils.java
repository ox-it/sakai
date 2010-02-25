package org.sakaiproject.hierarchy.tool.vm;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.springframework.validation.Errors;

public class VelocityControllerUtils {

	public static Map<String, Object> referenceData(HttpServletRequest request, Object command, Errors errors)
	{
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("sakai_fragment","false");
		model.put("sakai_head", (String) request
				.getAttribute("sakai.html.head"));
		model.put("sakai_onload", (String) request
				.getAttribute("sakai.html.body.onload"));

		String editor = ServerConfigurationService.getString("wysiwyg.editor");
		model.put("sakai_editor", editor);
		model.put("sakai_library_path", "/library/");
		
		model.put("rootUrl", request.getContextPath()+request.getServletPath());
		return model;
	}
	
}
