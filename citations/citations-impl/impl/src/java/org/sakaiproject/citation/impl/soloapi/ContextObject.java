package org.sakaiproject.citation.impl.soloapi;


/**
 * This is the main class for a Solo API reference.
 * @author nickwilson
 *
 */

import com.fasterxml.jackson.databind.JsonNode;

public class ContextObject {

	private JsonNode node;

	public void setNode(JsonNode node) {
		this.node = node;
	}
	public JsonNode getNode() {
		return node;
	}
}
