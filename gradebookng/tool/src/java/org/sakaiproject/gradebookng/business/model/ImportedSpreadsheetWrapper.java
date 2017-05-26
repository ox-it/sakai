package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.gradebookng.business.importExport.AnonIdentifier;
import org.sakaiproject.gradebookng.business.importExport.UserIdentifier;
import org.sakaiproject.gradebookng.business.importExport.UserIdentifierFactory;
import org.sakaiproject.user.api.User;

/**
 * Wraps an imported file
 */
public class ImportedSpreadsheetWrapper implements Serializable {

	@Getter
	@Setter
	private List<ImportedRow> rows;

	@Getter
	@Setter
	private List<ImportedColumn> columns;

	@Getter
	@Setter
	private UserIdentifier userIdentifier;

	@Getter
	@Setter
	private boolean anon;

	public ImportedSpreadsheetWrapper(Map<String, User> rosterMap) {
		rows = new ArrayList<>();
		columns = new ArrayList<>();
		userIdentifier = UserIdentifierFactory.buildIdentifierForSheet(rows, rosterMap);
		anon = userIdentifier instanceof AnonIdentifier;
	}
}
