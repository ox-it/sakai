package org.sakaiproject.citation.impl.soloapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.component.api.ServerConfigurationService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by nickwilson on 10/26/15.
*/
public class SoloApiServiceImpl {

	private ServerConfigurationService serverConfigurationService = null;
	private Map<String, Converter> converterMap = new HashMap<String, Converter>();
	private List<Converter> converters;
	static boolean firstTime = true;

	private static Log logger = LogFactory.getLog(SoloApiServiceImpl.class);

	public void init() {
		for (Converter converter : converters) {
			converterMap.put(converter.getType(), converter);
		}
	}

	public void setConverters(List<Converter> converters) {
		this.converters = converters;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public ContextObject parse(HttpServletRequest request) {
		String rftId = request.getParameter("rft_id");
		if (rftId == null) {
			throw new IllegalArgumentException("null rft_id passed to parse(HttpServletRequest request)");
		}
		String soloUrl = serverConfigurationService.getString("citations.availability.solo.api");
		if (soloUrl== null || soloUrl.equals("")) {
			throw new RuntimeException("Solo API url could not be obtained from properties.");
		}
		soloUrl = soloUrl.replace("<<<RFTID>>>", rftId.replace("TN_", ""));
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			URL url = new URL(soloUrl);
			JsonNode node = objectMapper.readValue(url, JsonNode.class);

			ContextObject co = new ContextObject();
			co.setNode(node);
			return co;

		} catch (MalformedURLException e) {
			logger.warn("MalformedURLException reading solo url : '" + soloUrl + "'. RftId: " + rftId);
		} catch (JsonMappingException e) {
			logger.warn("JsonMappingException reading solo url : '" + soloUrl + "'. RftId: " + rftId);
		} catch (JsonParseException e) {
			logger.warn("JsonParseException reading solo url : '" + soloUrl + "'. RftId: " + rftId);
		} catch (IOException e) {
			logger.warn("IOException reading solo url : '" + soloUrl + "'. RftId: " + rftId);
		}
		return null;
	}

	public Citation convert(ContextObject context) {
		if (context==null){
			throw new IllegalArgumentException("null context passed to convert(Context context)");
		}
		JsonNode node = context.getNode();
		if (node==null){
			throw new IllegalArgumentException("null jsonNode passed to convert(Context context)");
		}
		String genre = node.get("SEGMENTS").get("JAGROOT").get("RESULT").get("DOCSET").get("DOC").get("PrimoNMBib").get("record").get("addata").get("ristype").textValue();
		AbstractConverter converter = (AbstractConverter) converterMap.get(genre);
		return converter.convert(context);
	}
}
