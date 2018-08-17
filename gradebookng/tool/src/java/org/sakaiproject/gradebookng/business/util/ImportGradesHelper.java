package org.sakaiproject.gradebookng.business.util;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.importExport.AnonIdentifier;
import org.sakaiproject.gradebookng.business.importExport.DataConverter;
import org.sakaiproject.gradebookng.business.importExport.GradeValidationReport;
import org.sakaiproject.gradebookng.business.importExport.GradeValidator;
import org.sakaiproject.gradebookng.business.importExport.HeadingValidationReport;
import org.sakaiproject.gradebookng.business.importExport.UserIdentificationReport;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedCell;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.tool.model.AssignmentStudentGradeInfo;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Helper to handling parsing and processing of an imported gradebook file
 */
@Slf4j
public class ImportGradesHelper {

	// column positions we care about. 0 is first column.
	public final static int USER_ID_POS = 0;
	public final static int USER_NAME_POS = 1;

	// patterns for detecting column headers and their types
	public final static Pattern ASSIGNMENT_PATTERN = Pattern.compile("([^\\[]+)(\\[(\\d+([\\.,]\\d+)?)\\])?");
	final static Pattern COMMENT_PATTERN = Pattern.compile("\\* (.+)");
	final static Pattern IGNORE_PATTERN = Pattern.compile("(\\#.+)");

	// list of mimetypes for each category. Must be compatible with the parser
	private static final String[] XLS_MIME_TYPES = { "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" };
	private static final String[] XLS_FILE_EXTS = { ".xls", ".xlsx" };
	private static final String[] CSV_MIME_TYPES = { "text/csv", "text/plain", "text/comma-separated-values", "application/csv" };
	private static final String[] CSV_FILE_EXTS = { ".csv", ".txt" };
	private static final String[] DPC_FILE_EXTS = { ".dpc" };

	private static final char CSV_SEMICOLON_SEPARATOR = ';';

	// DPC default values
	private static final String DPC_STUDENT_ID_COLUMN_HEADER = "Student ID";
	public  static final String DPC_DEFAULT_GRADE_ITEM_TITLE = "Gradebook Item Import";
	private static final String DPC_DEFAULT_MAX_GRADE = "100";

	/**
	 * Helper to parse the imported file into an {@link ImportedSpreadsheetWrapper} depending on its type
	 * @param is
	 * @param mimetype
	 * @param filename
	 * @param businessService
	 * @return
	 * @throws GbImportExportInvalidFileTypeException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static ImportedSpreadsheetWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final String filename, final GradebookNgBusinessService businessService)
			throws GbImportExportInvalidFileTypeException, IOException, InvalidFormatException {
		return parseImportedGradeFile(is, mimetype, filename, businessService, "");
	}

	/**
	 * Helper to parse the imported file into an {@link ImportedSpreadsheetWrapper} depending on its type
	 *
	 * @param is
	 * @param mimetype
	 * @param filename
	 * @param businessService
	 * @param userDecimalSeparator
	 * @return
	 * @throws GbImportExportInvalidFileTypeException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static ImportedSpreadsheetWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final String filename,
				final GradebookNgBusinessService businessService, String userDecimalSeparator)
			throws GbImportExportInvalidFileTypeException, IOException, InvalidFormatException {

		ImportedSpreadsheetWrapper rval = null;

		// It would be great if we could depend on the browser mimetype, but Windows + Excel will always send an Excel mimetype
		if (StringUtils.endsWithAny(filename, CSV_FILE_EXTS) || ArrayUtils.contains(CSV_MIME_TYPES, mimetype)) {
			rval = parseCsv(is, businessService, userDecimalSeparator);
		} else if (StringUtils.endsWithAny(filename, XLS_FILE_EXTS) || ArrayUtils.contains(XLS_MIME_TYPES, mimetype)) {
			rval = parseXls(is, businessService, userDecimalSeparator);
		} else if (StringUtils.endsWithAny(filename, DPC_FILE_EXTS)) {
			rval = parseDPC(is, businessService.getUserStudentNumMap());
		} else {
			throw new GbImportExportInvalidFileTypeException("Invalid file type for grade import: " + mimetype);
		}
		return rval;
	}

	/**
	 * Parse a CSV into a list of {@link ImportedRow} objects.
	 *
	 * @param is InputStream of the data to parse
	 * @return
	 * @throws IOException
	 */
	private static ImportedSpreadsheetWrapper parseCsv(final InputStream is, final GradebookNgBusinessService businessService, String userDecimalSeparator) throws IOException {

		// Maps a String user identifier to its associated GbUser object. The key can be the eid (in a normal export), or the grading ID (in an anonymous export)
		Map<String, GbUser> idUserMap = Collections.emptyMap();

		// manually parse method so we can support arbitrary columns
		CSVReader reader;
		if (StringUtils.isEmpty(userDecimalSeparator)) {
			reader = new CSVReader(new InputStreamReader(is));
		} else {
			reader = new CSVReader(new InputStreamReader(is), ".".equals(userDecimalSeparator) ? CSVParser.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR);
		}
		String[] nextLine;
		int lineCount = 0;
		final List<ImportedRow> list = new ArrayList<>();
		Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();
		final ImportedSpreadsheetWrapper importedGradeWrapper = new ImportedSpreadsheetWrapper();

		try {
			while ((nextLine = reader.readNext()) != null) {

				if (lineCount == 0) {
					// header row, capture it
					mapping = mapHeaderRow(nextLine, importedGradeWrapper.getHeadingReport());
					boolean isContextAnonymous = !mapping.isEmpty() && ImportedColumn.Type.ANONYMOUS_ID.equals(mapping.get(0).getType());
					idUserMap = isContextAnonymous ? businessService.getAnonIDUserMap() : businessService.getUserEidMap();
				} else if (idUserMap != null) {
					// map the fields into the object
					final ImportedRow importedRow = mapLine(nextLine, mapping, idUserMap, userDecimalSeparator);
					if(importedRow != null) {
						list.add(importedRow);
					}
				}
				lineCount++;
			}
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				log.debug(e.getMessage());
			}
		}

		importedGradeWrapper.setColumns(new ArrayList<>(mapping.values()));
		importedGradeWrapper.setRows(list, idUserMap);
		return importedGradeWrapper;
	}

	/**
	 * Parse a DPC into a list of {@link ImportedRow} objects.
	 *
	 * @param is InputStream of the data to parse
	 * @return
	 * @throws IOException
	 */
	private static ImportedSpreadsheetWrapper parseDPC(final InputStream is, final Map<String, GbUser> studentNumMap) throws IOException {

		// Create the header row (which isn't included in the actual DPC file) with default values for the title and max points.
		// The user has the opportunity to change the title and max points in the confirmation step later on.
		Map<Integer, ImportedColumn> columnMapping = new LinkedHashMap<>();
		ImportedColumn studentNumColumn = new ImportedColumn();
		ImportedColumn gradebookItemColumn = new ImportedColumn();
		studentNumColumn.setType(ImportedColumn.Type.STUDENT_NUMBER);
		studentNumColumn.setColumnTitle(DPC_STUDENT_ID_COLUMN_HEADER);
		gradebookItemColumn.setType(ImportedColumn.Type.GB_ITEM_WITH_POINTS);
		gradebookItemColumn.setColumnTitle(DPC_DEFAULT_GRADE_ITEM_TITLE);
		gradebookItemColumn.setPoints(DPC_DEFAULT_MAX_GRADE);
		columnMapping.put(0, studentNumColumn);
		columnMapping.put(1, gradebookItemColumn);

		// Parse the file into a list of ImportedRow objects
		final List<ImportedRow> rows = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = reader.readLine()) != null) {

				// Skip empty lines
				if (DataConverter.isEmptyLine(line)) {
					continue;
				}

				// Skip lines that don't have the correct number of entries
				String[] lineValues = line.split(DataConverter.TAB_CHAR);
				if (lineValues.length < 2) {
					continue;
				}

				final ImportedRow row = mapLine(lineValues, columnMapping, studentNumMap, null);
				if (row != null) {
					rows.add(row);
				}
			}
		} catch (final IOException ex) {
			log.debug(ex.getMessage());
		}

		// Create the ImportedSpreadsheetWrapper object
		final ImportedSpreadsheetWrapper importedGradeWrapper = new ImportedSpreadsheetWrapper();
		if (rows.isEmpty()) {
			importedGradeWrapper.setColumns(new ArrayList<>());
		} else {
			importedGradeWrapper.setColumns(new ArrayList<>(columnMapping.values()));
		}
		importedGradeWrapper.setRows(rows, studentNumMap);
		importedGradeWrapper.setDPC(true);
		return importedGradeWrapper;
	}

	/**
	 * Parse an XLS into a list of {@link ImportedRow} objects.
	 *
	 * Note that only the first sheet of the Excel file is supported.
	 *
	 * @param is InputStream of the data to parse
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	private static ImportedSpreadsheetWrapper parseXls(final InputStream is, final GradebookNgBusinessService businessService, String userDecimalSeparator)
			throws InvalidFormatException, IOException {
		// Maps a String user identifier to its associated GbUser object. The key can be the eid (in a normal export), or the grading ID (in an anonymous export)
		Map<String, GbUser> idUserMap = Collections.emptyMap();

		int lineCount = 0;
		final List<ImportedRow> list = new ArrayList<>();
		Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();
		final ImportedSpreadsheetWrapper importedGradeWrapper = new ImportedSpreadsheetWrapper();

		final Workbook wb = WorkbookFactory.create(is);
		final Sheet sheet = wb.getSheetAt(0);
		for (final Row row : sheet) {

			final String[] r = convertRow(row);

			if (lineCount == 0) {
				// header row, capture it
				mapping = mapHeaderRow(r, importedGradeWrapper.getHeadingReport());
				boolean isContextAnonymous = !mapping.isEmpty() && ImportedColumn.Type.ANONYMOUS_ID.equals(mapping.get(0).getType());
				idUserMap = isContextAnonymous ? businessService.getAnonIDUserMap() : businessService.getUserEidMap();

			} else {
				// map the fields into the object
				final ImportedRow importedRow = mapLine(r, mapping, idUserMap, userDecimalSeparator);
				if(importedRow != null) {
					list.add(importedRow);
				}
			}
			lineCount++;
		}

		importedGradeWrapper.setColumns(new ArrayList<>(mapping.values()));
		importedGradeWrapper.setRows(list, idUserMap);
		return importedGradeWrapper;
	}

	/**
	 * Takes a row of data and maps it into the appropriate {@link ImportedRow} pieces
	 *
	 * @param line
	 * @param mapping
	 * @return
	 */
	private static ImportedRow mapLine(final String[] line, final Map<Integer, ImportedColumn> mapping, final Map<String, GbUser> userMap, String userDecimalSeparator) {

		final ImportedRow row = new ImportedRow();

		for (final Map.Entry<Integer, ImportedColumn> entry : mapping.entrySet()) {

			final int i = entry.getKey();
			final ImportedColumn column = entry.getValue();

			// In case there aren't enough data fields in the line to match up with the number of columns needed
			String lineVal = null;
			if (i < line.length) {
				lineVal = StringUtils.trimToNull(line[i]);
			}

			final String columnTitle = column.getColumnTitle();

			ImportedCell cell = row.getCellMap().get(columnTitle);
			if (cell == null) {
				cell = new ImportedCell();
			}

			if (column.getType() == ImportedColumn.Type.USER_ID
				|| column.getType() == ImportedColumn.Type.STUDENT_NUMBER
				|| column.getType() == ImportedColumn.Type.ANONYMOUS_ID) {

				//skip blank lines
				if (StringUtils.isBlank(lineVal)) {
					log.debug("Skipping empty row");
					return null;
				}

				if (null != column.getType()) switch(column.getType()) {
					case STUDENT_NUMBER:
						row.setStudentNumber(lineVal);
						break;
					case ANONYMOUS_ID:
						row.setAnonID(lineVal);
						break;
					default:
						// set eid (handled a couple lines below)
						break;
				}

				// check user is in the map (ie in the site)
				GbUser user = userMap.get(lineVal);
				if (user != null) {
					row.setStudentUuid(user.getUserUuid());
					row.setStudentEid(user.getEid());
				} else {
					row.setStudentEid(lineVal);
				}

			} else if (column.getType() == ImportedColumn.Type.USER_NAME) {
				row.setStudentName(StringUtils.trimToEmpty(lineVal));

			} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS) {
				convertSeparator(lineVal, userDecimalSeparator, cell);
				row.getCellMap().put(columnTitle, cell);

			} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS) {
				convertSeparator(lineVal, userDecimalSeparator, cell);
				row.getCellMap().put(columnTitle, cell);

			} else if (column.getType() == ImportedColumn.Type.COMMENTS) {
				cell.setComment(lineVal);
				row.getCellMap().put(columnTitle, cell);
			}
		}

		return row;
	}

	// fix the separator for the comparison with the current values
	private static void convertSeparator(final String lineVal, final String userDecimalSeparator, final ImportedCell cell)
	{
		if (StringUtils.isNotBlank(lineVal))
		{
			cell.setRawScore(lineVal);
			cell.setScore(",".equals(userDecimalSeparator) ? lineVal.replace(",", ".") : lineVal);
		}
	}

	/**
	 * Validates and processes the spreadsheet provided by importWizardModel.getSpreadsheetWrapper(); prepares an ImportWizardModel appropriate for the selection step
	 * @param sourcePage the ImportExportPage
	 * @param sourcePanel panel on which to invoke error(), etc. with localized messages should we encounter any issues
	 * @param importWizardModel
	 * @param businessService
	 * @param target
	 * @return true if the model was successfully set up without errors; in case of errors, the feedback panels will be updated and false will be returned
	 */
	public static boolean setupImportWizardModelForSelectionStep(ImportExportPage sourcePage, Panel sourcePanel, ImportWizardModel importWizardModel,
																		GradebookNgBusinessService businessService, AjaxRequestTarget target)
	{
		ImportedSpreadsheetWrapper spreadsheetWrapper = importWizardModel.getSpreadsheetWrapper();
		if (spreadsheetWrapper == null)
		{
			sourcePanel.error(MessageHelper.getString("importExport.error.unknown"));
			sourcePage.updateFeedback(target);
			return false;
		}

		// Track selections if coming back from the item creation steps
		List<ProcessedGradeItem> oldSelections = importWizardModel.getSelectedGradeItems();
		final Set<String> selectedItemTitles;
		if (oldSelections != null)
		{
			selectedItemTitles = oldSelections.stream().map(ProcessedGradeItem::getItemTitle).collect(Collectors.toSet());
		}
		else
		{
			selectedItemTitles = Collections.emptySet();
		}

		// If there are duplicate headings, tell the user now
		boolean hasValidationErrors = false;
		HeadingValidationReport headingReport = spreadsheetWrapper.getHeadingReport();
		SortedSet<String> duplicateHeadings = headingReport.getDuplicateHeadings();
		if (!duplicateHeadings.isEmpty()) {
			String duplicates = StringUtils.join(duplicateHeadings, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.duplicateColumns", duplicates));
			hasValidationErrors = true;
		}

		// If there are invalid headings, tell the user now
		SortedSet<String> invalidHeadings = headingReport.getInvalidHeadings();
		if (!invalidHeadings.isEmpty()) {
			String invalids = StringUtils.join(invalidHeadings, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.invalidColumns", invalids));
			hasValidationErrors = true;
		}

		// If there are blank headings, tell the user now
		int blankHeadings = headingReport.getBlankHeaderTitleCount();
		if (blankHeadings > 0) {
			sourcePanel.error(MessageHelper.getString("importExport.error.blankHeadings", blankHeadings));
			hasValidationErrors = true;
		}

		boolean isContextAnonymous = spreadsheetWrapper.getUserIdentifier() instanceof AnonIdentifier;

		// If there are duplicate student entries, tell the user now (we can't make the decision about which entry takes precedence)
		UserIdentificationReport userReport = spreadsheetWrapper.getUserIdentifier().getReport();
		SortedSet<GbUser> duplicateStudents = userReport.getDuplicateUsers();
		if (!duplicateStudents.isEmpty()) {
			String duplicates;
			if (isContextAnonymous)
			{
				duplicates = StringUtils.join(duplicateStudents.stream().map(GbUser::getAnonId).collect(Collectors.toList()), ", ");
			}
			else
			{
				duplicates = StringUtils.join(duplicateStudents, ", ");
			}
			sourcePanel.error(MessageHelper.getString("importExport.error.duplicateStudents", duplicates));
			hasValidationErrors = true;
		}

		// Perform grade validation; present error message with invalid grades on current page
		boolean isSourceDPC = spreadsheetWrapper.isDPC();
		List<ImportedColumn> columns = spreadsheetWrapper.getColumns();
		List<ImportedRow> rows = spreadsheetWrapper.getRows();
		GradeValidationReport gradeReport = new GradeValidator(businessService).validate(rows, columns, isSourceDPC, isContextAnonymous);
		// maps columnTitle -> (userEid -> grade)
		SortedMap<String, SortedMap<String, String>> invalidGradesMap = gradeReport.getInvalidNumericGrades();
		if (!invalidGradesMap.isEmpty()) {
			Collection<SortedMap<String, String>> invalidGrades = invalidGradesMap.values();
			List<String> badGradeEntries = new ArrayList<>(invalidGrades.size());
			for (SortedMap<String, String> invalidGradeEntries : invalidGrades) {
				badGradeEntries.add(StringUtils.join(invalidGradeEntries.entrySet(), ", "));
			}

			String badGrades = StringUtils.join(badGradeEntries, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.invalidGradeData",
					MessageHelper.getString("grade.notifications.invalid"), badGrades));
			hasValidationErrors = true;
		}

		// get existing data
		final List<Assignment> assignments = businessService.getGradebookAssignments();
		final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrixForImportExport(assignments, isContextAnonymous, null);

		// process file
		List<ProcessedGradeItem> processedGradeItems = processImportedGrades(spreadsheetWrapper, assignments, grades);

		// If the file has orphaned comment columns, tell the user now
		SortedSet<String> orphanedCommentColumns = headingReport.getOrphanedCommentHeadings();
		if (!orphanedCommentColumns.isEmpty()) {
			String invalids = StringUtils.join(orphanedCommentColumns, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.orphanedComments", invalids));
			hasValidationErrors = true;
		}
		
		// if the file has no valid users, tell the user now
		if (userReport.getIdentifiedUsers().isEmpty())
		{
			hasValidationErrors = true;
			sourcePanel.error(MessageHelper.getString("importExport.error.noValidStudents"));
		}

		// if empty there are no grade columns, tell the user now
		if (processedGradeItems.isEmpty() && !userReport.getIdentifiedUsers().isEmpty())
		{
			hasValidationErrors = true;
			sourcePanel.error(MessageHelper.getString("importExport.error.noValidGrades"));
		}
		
		boolean hasChanges = false;
		for (ProcessedGradeItem item : processedGradeItems)
		{
			if (item.getStatus().getStatusCode() == ProcessedGradeItemStatus.STATUS_MODIFIED ||
					item.getStatus().getStatusCode() == ProcessedGradeItemStatus.STATUS_NEW ||
					item.getStatus().getStatusCode() == ProcessedGradeItemStatus.STATUS_UPDATE ||
					item.getCommentStatus().getStatusCode() == ProcessedGradeItemStatus.STATUS_MODIFIED ||
					item.getCommentStatus().getStatusCode() == ProcessedGradeItemStatus.STATUS_NEW ||
					item.getCommentStatus().getStatusCode() == ProcessedGradeItemStatus.STATUS_UPDATE)
			{
				hasChanges = true;
				break;
			}
		}
		if ((!hasChanges && !processedGradeItems.isEmpty()) && !userReport.getIdentifiedUsers().isEmpty())
		{
			hasValidationErrors = true;
			sourcePanel.error(MessageHelper.getString("importExport.error.noChanges"));
		}

		// Return errors before processing further
		if (hasValidationErrors) {
			sourcePage.updateFeedback(target);
			return false;
		}

		// No validation errors were encountered; clear out previous errors and continue to the next step in the wizard
		sourcePage.clearFeedback();
		sourcePage.updateFeedback(target);

		// If returning from the creation pages, the ProcessedGradeItems in importWizardModel are now stale. Update them matching on getItemTitle()
		if (!CollectionUtils.isEmpty(selectedItemTitles))
		{
			Map<String, Assignment> titlesToAssignments = new HashMap<>();
			Map<ProcessedGradeItem, Assignment> staleAssignmentsToCreate = importWizardModel.getAssignmentsToCreate();
			for (Map.Entry<ProcessedGradeItem, Assignment> entry : staleAssignmentsToCreate.entrySet())
			{
				titlesToAssignments.put(entry.getKey().getItemTitle(), entry.getValue());
			}

			List<ProcessedGradeItem> selectedGradeItems = new ArrayList<>();
			Map<ProcessedGradeItem, Assignment> assignmentsToCreate = new LinkedHashMap<>();
			for (ProcessedGradeItem item : processedGradeItems)
			{
				String title = item.getItemTitle();
				if (selectedItemTitles.contains(title))
				{
					selectedGradeItems.add(item);
				}

				Assignment toCreate = titlesToAssignments.get(title);
				if (toCreate != null)
				{
					assignmentsToCreate.put(item, toCreate);
				}
			}

			importWizardModel.setSelectedGradeItems(selectedGradeItems);
			importWizardModel.setAssignmentsToCreate(assignmentsToCreate);
		}

		// Setup and return the model
		importWizardModel.setProcessedGradeItems(processedGradeItems);
		importWizardModel.setUserReport(userReport);
		importWizardModel.setContextAnonymous(isContextAnonymous);

		return true;
	}

	/**
	 * Process the data.
	 *
	 * TODO enhance this to have better returns ie GbExceptions
	 *
	 * @param spreadsheetWrapper
	 * @param assignments
	 * @param currentGrades
	 *
	 * @return
	 */
	public static List<ProcessedGradeItem> processImportedGrades(final ImportedSpreadsheetWrapper spreadsheetWrapper,
			final List<Assignment> assignments, final List<GbStudentGradeInfo> currentGrades) {

		// setup
		// TODO this will ensure dupes can't be added. Provide a report to the user that dupes were added. There would need to be a step before this though
		// this retains order of the columns in the imported file
		final Map<String, ProcessedGradeItem> assignmentProcessedGradeItemMap = new LinkedHashMap<>();

		// process grades
		Map<Long, AssignmentStudentGradeInfo> gradeMap = transformCurrentGrades(currentGrades);

		// Map assignment name to assignment
		final Map<String, Assignment> assignmentNameMap = assignments.stream().collect(Collectors.toMap(Assignment::getName, a -> a));

		// maintain a list of comment columns so we can check they have a corresponding item
		final List<String> commentColumns = new ArrayList<>();

		//for every column, setup the data
		for (final ImportedColumn column : spreadsheetWrapper.getColumns()) {
			boolean needsToBeAdded = false;

			// skip the ignorable columns
			if(column.isIgnorable()) {
				continue;
			}

			final String columnTitle = StringUtils.trim(column.getColumnTitle()); // trim whitespace so we can match properly

			//setup a new one unless it already exists (ie there were duplicate columns)
			ProcessedGradeItem processedGradeItem = assignmentProcessedGradeItemMap.get(columnTitle);
			if (processedGradeItem == null) {
				processedGradeItem = new ProcessedGradeItem();
				needsToBeAdded = true;

				//default to gb_item
				//overridden if a comment type
				processedGradeItem.setType(ProcessedGradeItem.Type.GB_ITEM);
			}

			final Assignment assignment = assignmentNameMap.get(columnTitle);
			final ProcessedGradeItemStatus status = determineStatus(column, assignment, spreadsheetWrapper, gradeMap);

			if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS) {
				log.debug("GB Item: {}, status: {}", columnTitle, status.getStatusCode());
				processedGradeItem.setItemTitle(columnTitle);
				processedGradeItem.setItemPointValue(column.getPoints());
				processedGradeItem.setStatus(status);
			} else if (column.getType() == ImportedColumn.Type.COMMENTS) {
				log.debug("Comments: {}, status: {}", columnTitle, status.getStatusCode());
				processedGradeItem.setType(ProcessedGradeItem.Type.COMMENT);
				processedGradeItem.setCommentStatus(status);
				commentColumns.add(columnTitle);
			} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS) {
				log.debug("Regular: {}, status: {}", columnTitle, status.getStatusCode());
				processedGradeItem.setItemTitle(columnTitle);
				processedGradeItem.setStatus(status);
			} else {
				// skip
				//TODO could return this but as a skip status?
				log.warn("Bad column. Type: {}, header: {}.  Skipping.", column.getType(), columnTitle);
				continue;
			}

			if (assignment != null) {
				processedGradeItem.setItemId(assignment.getId());
			}

			final List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<>();
			for (final ImportedRow row : spreadsheetWrapper.getRows()) {
				final ImportedCell cell = row.getCellMap().get(columnTitle);
				if (cell != null) {
					// Only process the grade item if the user is valid (present in the site/gradebook)
					if (row.getUser().isValid()) {
						final ProcessedGradeItemDetail processedGradeItemDetail = new ProcessedGradeItemDetail();
						processedGradeItemDetail.setUser(row.getUser());
						processedGradeItemDetail.setGrade(cell.getScore());
						processedGradeItemDetail.setComment(cell.getComment());
						processedGradeItemDetails.add(processedGradeItemDetail);
					}
				}
			}
			processedGradeItem.setProcessedGradeItemDetails(processedGradeItemDetails);

			// add to list
			if (needsToBeAdded) {
				assignmentProcessedGradeItemMap.put(columnTitle, processedGradeItem);
			}
		}

		// get just a list
		final List<ProcessedGradeItem> processedGradeItems = new ArrayList<>(assignmentProcessedGradeItemMap.values());

		// comment columns must have an associated gb item column
		// this ensures we have a processed grade item for each one
		HeadingValidationReport report = spreadsheetWrapper.getHeadingReport();
		commentColumns.forEach(c -> {
			final boolean matchingItemExists = processedGradeItems.stream().filter(p -> StringUtils.equals(c, p.getItemTitle())).findFirst().isPresent();

			if(!matchingItemExists) {
				report.addOrphanedCommentHeading(c);
			}
		});

		return processedGradeItems;
	}

	/**
	 * Determine the status of a column
	 * @param column
	 * @param assignment
	 * @param importedGradeWrapper
	 * @param transformedGradeMap
	 * @return
	 */
	private static ProcessedGradeItemStatus determineStatus(final ImportedColumn column, final Assignment assignment,
			final ImportedSpreadsheetWrapper importedGradeWrapper, final Map<Long, AssignmentStudentGradeInfo> gradeMap) {

		//TODO - really? an arbitrary value? How about null... Remove this
		// OWLTODO: null is a bad idea. keep this, change the default value to NA
		ProcessedGradeItemStatus status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UNKNOWN);

		if (ProcessedGradeItemStatus.COURSE_GRADE_RESERVED_TITLE.equalsIgnoreCase(column.getColumnTitle()))
		{
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_COURSE_GRADE);
		}
		else if (assignment == null) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NEW);
		} else if (assignment.getExternalId() != null) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_EXTERNAL, assignment.getExternalAppName());
		} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS && assignment.getPoints().compareTo(NumberUtils.toDouble(column.getPoints())) != 0) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_MODIFIED);
		} else {
			for (final ImportedRow row : importedGradeWrapper.getRows()) {
				final AssignmentStudentGradeInfo assignmentStudentGradeInfo = gradeMap.get(assignment.getId());
				final ImportedCell importedGradeItem = row.getCellMap().get(column.getColumnTitle());

				String actualScore = null;
				String actualComment = null;

				if (assignmentStudentGradeInfo != null) {
					final GbGradeInfo actualGradeInfo = assignmentStudentGradeInfo.getStudentGrades().get(row.getStudentEid());

					if (actualGradeInfo != null) {
						actualScore = actualGradeInfo.getGrade();
						actualComment = actualGradeInfo.getGradeComment();
					}
				}
				String importedScore = null;
				String importedComment = null;

				if (importedGradeItem != null) {
					importedScore = importedGradeItem.getScore();
					importedComment = importedGradeItem.getComment();
				}

				if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS) {
					final String trimmedImportedScore = StringUtils.removeEnd(importedScore, ".0");
					final String trimmedActualScore = StringUtils.removeEnd(actualScore, ".0");
					if (!StringUtils.equals(trimmedActualScore, trimmedImportedScore)) {
						status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UPDATE);
						break;
					}
				} else if (column.getType() == ImportedColumn.Type.COMMENTS) {
					if (!StringUtils.equals(actualComment, importedComment)) {
						status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UPDATE);
						break;
					}
				} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS) {
					//must be NA if it isn't new
					status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NA);
					break;
				}
			}
			// If we get here, must not have been any changes
			if (status.getStatusCode() == ProcessedGradeItemStatus.STATUS_UNKNOWN) {
				status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NA);
			}

			// TODO - What about if a user was added to the import file?
			// That probably means that actualGradeInfo from up above is null...but what do I do?
			// SS - this is now caught.

		}
		return status;
	}

	private static Map<Long, AssignmentStudentGradeInfo> transformCurrentGrades(final List<GbStudentGradeInfo> currentGrades) {
		final Map<Long, AssignmentStudentGradeInfo> assignmentMap = new HashMap<>();

		for (final GbStudentGradeInfo studentGradeInfo : currentGrades) {
			for (final Map.Entry<Long, GbGradeInfo> entry : studentGradeInfo.getGrades().entrySet()) {
				final Long assignmentId = entry.getKey();
				AssignmentStudentGradeInfo assignmentStudentGradeInfo = assignmentMap.get(assignmentId);
				if (assignmentStudentGradeInfo == null) {
					assignmentStudentGradeInfo = new AssignmentStudentGradeInfo();
					assignmentStudentGradeInfo.setAssignmemtId(assignmentId);
					assignmentMap.put(assignmentId, assignmentStudentGradeInfo);
				}
				//assignmentStudentGradeInfo.addGrade(studentGradeInfo.getStudentEid(), entry.getValue());
				assignmentStudentGradeInfo.addGrade(studentGradeInfo.getStudent().getEid(), entry.getValue());
			}
		}

		return assignmentMap;
	}

	/**
	 * Takes a row of String[] data to determine the position of the columns so that we can correctly parse any arbitrary delimited file.
	 * This is required because when we iterate over the rest of the lines, we need to know what the column header is, so we can take the appropriate action.
	 *
	 * Note that some columns are determined positionally
	 *
	 * @param line the already split line
	 * @return LinkedHashMap to retain order
	 */
	private static Map<Integer, ImportedColumn> mapHeaderRow(final String[] line, HeadingValidationReport headingReport) {

		// retain order
		final Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();

		boolean isContextAnonymous = false;

		for (int i = 0; i < line.length; i++) {

			ImportedColumn column = new ImportedColumn();

			log.debug("i: {}", i);
			log.debug("line[i]: {}", line[i]);

			if(i == USER_ID_POS) {
				if (MessageHelper.getString("importExport.export.csv.headers.anonId").equals(line[i]))
				{
					column.setType(ImportedColumn.Type.ANONYMOUS_ID);
					isContextAnonymous = true;
				}
				else
				{
					column.setType(ImportedColumn.Type.USER_ID);
				}
			} else if(!isContextAnonymous && i == USER_NAME_POS) {
				column.setType(ImportedColumn.Type.USER_NAME);
			} else {
				column = parseHeaderToColumn(StringUtils.trimToNull(line[i]), headingReport);
			}

			// check for duplicates
			if(mapping.values().contains(column)) {
				String columnTitle = column.getColumnTitle();
				headingReport.addDuplicateHeading(columnTitle);
				continue;
			}

			if (column != null) {
				mapping.put(i, column);
			}
		}

		return mapping;
	}

	/**
	 * Helper to parse the header row into an {@link ImportedColumn}
	 * @param headerValue
	 * @return the mapped column.
	 */
	private static ImportedColumn parseHeaderToColumn(final String headerValue, HeadingValidationReport headingReport) {

		if(StringUtils.isBlank(headerValue)) {
			headingReport.incrementBlankHeaderTitleCount();
			return null;
		}

		log.debug("headerValue: {}", headerValue);
		final ImportedColumn column = new ImportedColumn();

		Matcher m = IGNORE_PATTERN.matcher(headerValue);
		if (m.matches()) {
			log.debug("Found header: {} but ignoring it as it is prefixed with a #.", headerValue);
			column.setType(ImportedColumn.Type.IGNORE);
			return column;
		}

		m = COMMENT_PATTERN.matcher(headerValue);
		if (m.matches()) {
			column.setColumnTitle(StringUtils.trimToNull(m.group(1)));
			column.setType(ImportedColumn.Type.COMMENTS);
			return column;
		}

		m = ASSIGNMENT_PATTERN.matcher(headerValue);
		if (m.matches()) {
			column.setColumnTitle(StringUtils.trimToNull(m.group(1)));
			String points = m.group(3);
			if (StringUtils.isNotBlank(points)) {
				column.setPoints(points);
				column.setType(ImportedColumn.Type.GB_ITEM_WITH_POINTS);
			} else {
				column.setType(ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS);
			}

			return column;
		}

		// None of the patterns match, it must be invalid/formatted improperly
		headingReport.addInvalidHeading(headerValue);
		return null;
	}

	/**
	 * Helper to map an Excel {@link Row} to a String[] so we can use the same methods to process it as the CSV
	 *
	 * @param row
	 * @return
	 */
	private static String[] convertRow(final Row row) {

		// Gets the index of the last cell in this row *plus one* (https://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/Row.html#getLastCellNum())
		final int numCells = row.getLastCellNum();
		final String[] s = new String[numCells];
		// All initialized to null; populate only existing cells
		int i = 0;
		for (final Cell cell : row) {
			// force cell to String
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s[cell.getColumnIndex()] = StringUtils.trimToNull(cell.getStringCellValue());
			i++;
		}

		return s;
	}
}
