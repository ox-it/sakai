package org.sakaiproject.hierarchy.tool.vm;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("velocityControllerUtils")
public class VelocityControllerUtils {

	private ServerConfigurationService serverConfigurationService;

	@Autowired
	public VelocityControllerUtils(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public Map<String, Object> referenceData(HttpServletRequest request) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("sakai_fragment", "false");
		StringBuilder headHtml = new StringBuilder();
		String requestHeadHtml = (String) request.getAttribute("sakai.html.head");
		if (requestHeadHtml != null) {
			headHtml.append(requestHeadHtml);
			headHtml.append("\n");
		}
		headHtml.append(additionHeadContent());
		model.put("sakai_head", headHtml);
		model.put("sakai_onload", (String) request.getAttribute("sakai.html.body.onload"));

		String editor = serverConfigurationService.getString("wysiwyg.editor");
		model.put("sakai_editor", editor);
		model.put("sakai_library_path", "/library/");

		model.put("rootUrl", request.getContextPath() + request.getServletPath());
		return model;
	}

	private String additionHeadContent() {
		// The Sakai inline style defines borders which end up overlapping.
		// Loading a CSS file out of the context isn't simple without hardcoding the
		// context path and putting the file in /library creates a cross project dependency.
		return "<style type='text/css'>span.alertMessageInline { border: none; }</style>";
	}

}
