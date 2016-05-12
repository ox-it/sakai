package org.sakaiproject.citation.impl.soloapi;

import org.sakaiproject.citation.api.Citation;

import java.util.ArrayList;

/**
 * Converts from a Context to a Citation.
 * @author nickwilson
 *
 */
public class BookConverter extends AbstractConverter {

	public String getType() {
		return "BOOK";
	}

	@Override
	public Citation convert(ContextObject context) {
		Citation citation = super.convert(context);

		setCitationProperty(citation, "creator", primoNMBibNode, "record", "addata", "au");
		if (citation.getCitationProperty("creator")!=null && !citation.getCitationProperty("creator").equals("")){
			setCitationProperty(citation, "creator", primoNMBibNode, "record", "addata", "addau" );
		}
		else {
			setCitationProperty(citation, "editor", primoNMBibNode, "record", "addata", "addau" );
		}
		setCitationProperty(citation, "title", primoNMBibNode, "record", "addata", "btitle" );
		setCitationProperty(citation, "sourceTitle", primoNMBibNode, "addata", "seriestitle" );
		setCitationProperty(citation, "isnIdentifier", primoNMBibNode, "record", "addata", "isbn" );

		return citation;
	}
}
