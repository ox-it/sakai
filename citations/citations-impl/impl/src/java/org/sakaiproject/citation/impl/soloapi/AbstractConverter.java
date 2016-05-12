package org.sakaiproject.citation.impl.soloapi;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public abstract class AbstractConverter implements Converter {

	private CitationService citationService;

	protected JsonNode primoNMBibNode;
	protected JsonNode docNode;
	protected JsonNode getItNode;

	public void setCitationService(CitationService citationService) {
		this.citationService = citationService;
	}

	protected void setCitationProperty(Citation citation, String citationKey, JsonNode primoNMBibNode, String... jsonPath) {

		// find the json node
		for (String jsonNode : jsonPath) {
			primoNMBibNode = primoNMBibNode.get(jsonNode);
			if (primoNMBibNode==null) {
				break;
			}
		}

		if (primoNMBibNode!=null){
			String value = null;

			// if jsonnode contains array
			if (primoNMBibNode instanceof ArrayNode){
				for (JsonNode jsonNode : primoNMBibNode) {
					value = jsonNode.getTextValue();
					if (value != null) {
						citation.setCitationProperty(citationKey, value);
					}
				}
			}
			else {
				value = primoNMBibNode.getTextValue();
				if (value != null) {
					value = formatSpecialCases(citationKey, value);
					citation.setCitationProperty(citationKey, value);
				}
			}

		}
	}

	private String formatSpecialCases(String citationKey, String value) {

		// if it's a year we get the first four digits of the date
		if (citationKey.equals("year") && value.length()>3) {
			value = value.substring(0,4);
		}
		// to get the link to solo we use hte parameter from the the open url,
		else if (citationKey.equals("otherIds") && value.startsWith("http://oxfordsfx")) {
			for (String string : value.split("&")) {
				if (string.startsWith("rft_id=http") && string.contains("solo.bodleian")){
					value = string.split("=")[1];
					try {
						value = URLDecoder.decode(value, "UTF-8");
					} catch (UnsupportedEncodingException ex) {
						throw new RuntimeException("Could not decode rft_id value which is:" + value);
					}
				}
			}
		}
		return value;
	}

	public Citation convert(ContextObject context) {
		docNode = context.getNode().get("SEGMENTS").get("JAGROOT").get("RESULT").get("DOCSET").get("DOC");
		primoNMBibNode = docNode.get("PrimoNMBib");
		getItNode = docNode.get("GETIT");

		Citation citation = citationService.addCitation(getType().toLowerCase());

		setCitationProperty(citation, "year", primoNMBibNode, "record", "addata", "date" );
		setCitationProperty(citation, "publisher", primoNMBibNode, "record", "addata", "pub" );
		setCitationProperty(citation, "publicationLocation", primoNMBibNode, "record", "addata", "cop" );
		setCitationProperty(citation, "edition", primoNMBibNode, "record", "display", "edition" );
		setCitationProperty(citation, "doi", primoNMBibNode, "record", "addata", "doi" );
		setCitationProperty(citation, "otherIds", getItNode, "@GetIt2" );

		return citation;

	}
}
