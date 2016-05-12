package org.sakaiproject.citation.impl.soloapi;

import org.sakaiproject.citation.api.Citation;

import java.util.ArrayList;


/**
 * Converts from a Context to a Citation.
 * @author nickwilson
 *
 */
public class GenericConverter extends AbstractConverter  {

	public String getType() {
		return "GEN";
	}

	@Override
	public Citation convert(ContextObject context) {
		Citation citation = super.convert(context);

		setCitationProperty(citation, "creator", primoNMBibNode, "record", "addata", "au" );
		setCitationProperty(citation, "creator", primoNMBibNode, "record", "addata", "addau" );

		setCitationProperty(citation, "title",  primoNMBibNode, "record", "addata", "btitle" );
		if (citation.getCitationProperty("title", false)==null || citation.getCitationProperty("title", false).equals("")){
			setCitationProperty(citation, "title", primoNMBibNode, "record", "addata", "atitle" );
		}
		setCitationProperty(citation, "sourceTitle", primoNMBibNode, "record", "addata", "jtitle" );

		setCitationProperty(citation, "date", primoNMBibNode, "record", "addata", "date" );
		setCitationProperty(citation, "volume", primoNMBibNode, "record", "addata", "volume" );
		setCitationProperty(citation, "issue", primoNMBibNode, "record", "addata", "issue" );
		setCitationProperty(citation, "pages", primoNMBibNode, "record", "addata", "pages" );
		setCitationProperty(citation, "startPage", primoNMBibNode, "record", "addata", "spage" );
		setCitationProperty(citation, "endPage", primoNMBibNode, "record", "addata", "epage" );

		setCitationProperty(citation, "isnIdentifier", primoNMBibNode, "record", "addata", "issn" );
		if (citation.getCitationProperty("isnIdentifier")==null || citation.getCitationProperty("isnIdentifier").equals("")){
			setCitationProperty(citation, "isnIdentifier", primoNMBibNode, "record", "addata", "isbn" );
		}
		if (citation.getCitationProperty("isnIdentifier")==null || citation.getCitationProperty("isnIdentifier").equals("")){
			setCitationProperty(citation, "isnIdentifier", primoNMBibNode, "record", "addata", "eissn" );
		}

		return citation;
	}
}
