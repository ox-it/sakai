package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.gradebookng.business.importExport.HeadingValidationReport;
import org.sakaiproject.gradebookng.business.importExport.UserIdentifier;
import org.sakaiproject.gradebookng.business.importExport.UserIdentifierFactory;

/**
 * Wraps an imported file
 */
public class ImportedSpreadsheetWrapper implements Serializable {

	@Getter
	private List<ImportedRow> rows;

	@Getter
	@Setter
	private List<ImportedColumn> columns;

	@Getter
	private UserIdentifier userIdentifier;

	@Getter
	private final HeadingValidationReport headingReport;

	@Getter
	@Setter
	private boolean isDPC;

	public ImportedSpreadsheetWrapper() {
		rows = new ArrayList<>();
		columns = new ArrayList<>();
		userIdentifier = null;
		headingReport = new HeadingValidationReport();
		isDPC = false;
	}

	public void setRows(List<ImportedRow> rows, Map<String, GbUser> rosterMap) {
		this.rows = rows;
		userIdentifier = UserIdentifierFactory.buildIdentifierForSheet(rows, rosterMap);
	}
}
