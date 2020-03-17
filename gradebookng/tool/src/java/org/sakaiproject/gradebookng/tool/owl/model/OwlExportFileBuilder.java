package org.sakaiproject.gradebookng.tool.owl.model;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Session;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.owl.OwlBusinessService;
import org.sakaiproject.gradebookng.business.owl.OwlGbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.owl.anon.OwlAnonGradingService.AnonStatus;
import org.sakaiproject.gradebookng.business.owl.finalgrades.FinalGradeFormatter;
import org.sakaiproject.gradebookng.business.util.EventHelper;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.owl.panels.importExport.OwlExportPanel;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 *
 * @author plukasew
 */
public class OwlExportFileBuilder implements Serializable
{
	@Builder
	public static class CsvType
	{
		public boolean anon, cg; // is anon / course grade allowed (with respect to anon status)
	}

	public enum OwlExportFormat { CSV, ZIP };

	public static final String IGNORE_COLUMN_PREFIX = "#";
	public static final String COMMENTS_COLUMN_PREFIX = "*";
	public static final char CSV_SEMICOLON_SEPARATOR = ';';

	private final String decimalSeparator, decimalEnd;

	public OwlExportFileBuilder(String decimalSeparator)
	{
		this.decimalSeparator = decimalSeparator;
		decimalEnd = decimalSeparator + "0";
	}

	public File buildFile(OwlExportPanel panel, List<Assignment> allItems)
	{
		return buildFile(panel, OwlExportConfig.DEFAULTS, false, false, allItems);
	}

	public File buildCustomFile(OwlExportPanel panel, OwlExportConfig config, List<Assignment> allItems)
	{
		return buildFile(panel, config, true, false, allItems);
	}

	public File buildRevealedFile(OwlExportPanel panel, List<Assignment> allItems)
	{
		return buildFile(panel, OwlExportConfig.DEFAULTS, false, true, allItems);
	}

	public File buildCustomRevealedFile(OwlExportPanel panel, OwlExportConfig config, List<Assignment> allItems)
	{
		return buildFile(panel, config, true, true, allItems);
	}

	private File buildFile(OwlExportPanel panel, OwlExportConfig config, boolean isCustom, boolean isRevealed, List<Assignment> allItems)
	{
		OwlBusinessService owlbus = panel.getOwlbus();

		// figure out what kind of export we have (normal/anon/mixed)
		AnonStatus anonStatus = isRevealed ? AnonStatus.NORMAL : owlbus.anon.detectAnonStatus(allItems);

		// tell the panel to build the file name
		OwlExportFormat format = anonStatus == AnonStatus.MIXED ? OwlExportFormat.ZIP : OwlExportFormat.CSV;
		panel.buildFileName(format, isCustom);

		// Step 3: Determine where the course grade goes
		boolean isCourseGradePureAnon = false;
		if (anonStatus == AnonStatus.MIXED)
		{
			isCourseGradePureAnon = owlbus.anon.isCourseGradePureAnonForAllAssignments(allItems);
		}

		// Step 4: create the file
		File tempFile;
		try
		{
			String extension = "." + format.name().toLowerCase();
			tempFile = File.createTempFile("gradebookTemplate", extension);
			String isoCharsetName = StandardCharsets.ISO_8859_1.name();

			if (anonStatus == AnonStatus.MIXED) // zip file
			{
				// partition the items into anon (true) and normal (false)
				Map<Boolean, List<Assignment>> itemMap = allItems.stream().collect(Collectors.partitioningBy(Assignment::isAnon));

				try (FileOutputStream fos = new FileOutputStream(tempFile);
						ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.ISO_8859_1);
						OutputStreamWriter normalWriter = new OutputStreamWriter(zos, isoCharsetName);
						CSVWriter normalCsv = getCsvWriter(normalWriter);
						OutputStreamWriter anonWriter = new OutputStreamWriter(zos, isoCharsetName);
						CSVWriter anonCsv = getCsvWriter(anonWriter))
				{
						zos.putNextEntry(new ZipEntry("gradebookExport_normal.csv"));
						CsvType type = CsvType.builder().anon(false).cg(!isCourseGradePureAnon).build();
						writeLines(normalCsv, getCsvContents(owlbus, type, config, panel, itemMap.get(false)));
						normalCsv.flush();
						zos.closeEntry();

						zos.putNextEntry(new ZipEntry("gradebookExport_anonymous.csv"));
						type = CsvType.builder().anon(true).cg(isCourseGradePureAnon).build();
						writeLines(anonCsv, getCsvContents(owlbus, type, config, panel, itemMap.get(true)));
						anonCsv.flush();
						zos.closeEntry();
				}
			}
			else // normal or pure anon csv file
			{
				try (FileOutputStream fos = new FileOutputStream(tempFile);
						OutputStreamWriter osw = new OutputStreamWriter(fos, isoCharsetName);
						CSVWriter csvWriter = getCsvWriter(osw))
				{
					CsvType type = CsvType.builder().anon(anonStatus == AnonStatus.ANON).cg(true).build();
					writeLines(csvWriter, getCsvContents(owlbus, type, config, panel, allItems));
				}
			}
		}
		catch (IOException ioe)
		{
			Session.get().error("Unable to create export file");
			throw new RuntimeException(ioe);
		}

		EventHelper.postExportEvent(panel.getGradebook(), isCustom);

		return tempFile;
	}

	private void writeLines(final CSVWriter writer, List<List<String>> lines)
	{
		lines.stream().forEach(line -> { writer.writeNext(line.toArray(new String[] {})); });
	}

	private CSVWriter getCsvWriter(OutputStreamWriter outstream)
	{
		char csvDelimiter = ".".equals(decimalSeparator) ? CSVWriter.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR;
		return new CSVWriter(outstream, csvDelimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
	}

	private List<List<String>> getCsvContents(OwlBusinessService owlbus, CsvType type, OwlExportConfig config, OwlExportPanel panel, List<Assignment> items)
	{
		List<List<String>> csvContents = new ArrayList<>();

		buildHeader(type, csvContents, items, config, panel);

		List<OwlGbStudentGradeInfo> matrix = owlbus.buildGradeMatrixForImportExport(items, config.group, type.anon);
		buildRows(type, csvContents, items, matrix, config);

		return csvContents;
	}

	/**
	 *
	 * @param type
	 * @param contents
	 * @param items the items to include
	 * @param config
	 * @param panel
	 */
	private void buildHeader(CsvType type, List<List<String>> contents, List<Assignment> items, OwlExportConfig config, OwlExportPanel panel)
	{
		List<String> header = new ArrayList<>();

		if (type.anon)
		{
			addAnonUserDataToHeader(header, panel);
		}
		else
		{
			addNormalUserDataToHeader(header, config, panel);
		}

		// no gradebook items, give a template
		if (items.isEmpty())
		{
			// with points
			header.add(String.join(" ", panel.getString("importExport.export.csv.headers.example.points"), "[100]"));

			// no points
			header.add(panel.getString("importExport.export.csv.headers.example.nopoints"));

			// points and comments
			header.add(String.join(" ", COMMENTS_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.example.pointscomments"), "[50]"));

			// ignore
			header.add(String.join(" ", IGNORE_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.example.ignore")));
		}

		// build column header
		items.forEach(assignment -> {
			final String assignmentPoints = FormatHelper.formatGradeForDisplay(assignment.getPoints().toString());
			String externalPrefix = "";
			if (assignment.isExternallyMaintained()) {
				externalPrefix = IGNORE_COLUMN_PREFIX;
			}
			if (config.includeGradeItemScores)
			{
				header.add(externalPrefix + assignment.getName() + " [" + StringUtils.removeEnd(assignmentPoints, decimalEnd) + "]");
			}
			if (config.includeGradeItemComments)
			{
				header.add(String.join(" ", externalPrefix, COMMENTS_COLUMN_PREFIX, assignment.getName()));
			}
		});

		if (type.cg)
		{
			addCourseGradeDataToHeader(header, config, panel);
		}

		contents.add(header);
	}

	private void addNormalUserDataToHeader(List<String> header, OwlExportConfig config, OwlExportPanel panel)
	{
		if (config.includeStudentId)
		{
			header.add(panel.getString("importExport.export.csv.headers.studentId"));
		}
		if (config.includeStudentName)
		{
			header.add(panel.getString("importExport.export.csv.headers.studentName"));
		}
		if (config.includeStudentNumber)
		{
			header.add(String.join(" ", IGNORE_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.studentNumber")));
		}
	}

	private void addAnonUserDataToHeader(List<String> header, OwlExportPanel panel)
	{
		header.add(panel.getString("importExport.export.csv.headers.anonId"));
	}

	private void addCourseGradeDataToHeader(List<String> header, OwlExportConfig config, OwlExportPanel panel)
	{
		if (config.includePoints)
		{
			header.add(String.join(" ", IGNORE_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.points")));
		}
		if (config.includeCalculatedGrade)
		{
			header.add(String.join(" ", IGNORE_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.calculatedGrade")));
		}
		if (config.includeFinalGrade)
		{
			header.add(String.join(" ", IGNORE_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.finalGrade")));
		}
		if (config.includeGradeOverride)
		{
			header.add(String.join(" ", IGNORE_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.gradeOverride")));
		}
		if (config.includeLastLogDate)
		{
			header.add(String.join(" ", IGNORE_COLUMN_PREFIX, panel.getString("importExport.export.csv.headers.lastLogDate")));
		}
	}

	/**
	 *
	 * @param type
	 * @param contents
	 * @param items the gradebook items to include
	 * @param matrix the users to include
	 * @param config
	 * @param panel
	 */
	private void buildRows(CsvType type, List<List<String>> contents, List<Assignment> items, List<OwlGbStudentGradeInfo> matrix, OwlExportConfig config)
	{
		final int cols = contents.get(0).size(); // get the numbers of columns in the header
		for (OwlGbStudentGradeInfo student : matrix)
		{
			List<String> row = new ArrayList<>(cols);

			if (type.anon)
			{
				addAnonUserData(row, student);
			}
			else
			{
				addNormalUserData(row, student, config);
			}

			if (items.isEmpty()) // no gradebook items, give a template, add empty cell for each example column
			{
				row.add("");
				row.add("");
				row.add("");
				row.add("");
			}
			else if (config.includeGradeItemScores || config.includeGradeItemComments)
			{
				for (Assignment item : items)
				{
					final GbGradeInfo gradeInfo = student.info.getGrades().get(item.getId());
					if (config.includeGradeItemScores)
					{
						String grade = gradeInfo == null ? "" : FormatHelper.formatGradeForDisplay(gradeInfo.getGrade());
						row.add(StringUtils.removeEnd(grade, decimalEnd));
					}
					if (config.includeGradeItemComments)
					{
						row.add(gradeInfo == null ? "" : gradeInfo.getGradeComment());
					}
				}
			}

			if (type.cg)
			{
				addCourseGradeData(row, config, student);
			}

			contents.add(row);
		}
	}

	private void addNormalUserData(List<String> row, OwlGbStudentGradeInfo student, OwlExportConfig config)
	{
		if (config.includeStudentId)
		{
			row.add(student.info.getStudentEid());
		}
		if (config.includeStudentName)
		{
			row.add(FormatHelper.htmlUnescape(student.info.getStudentLastName()) + ", " + FormatHelper.htmlUnescape(student.info.getStudentFirstName()));
		}
		if (config.includeStudentNumber)
		{
			row.add(student.info.getStudentNumber());
		}
	}

	private void addAnonUserData(List<String> row, OwlGbStudentGradeInfo student)
	{
		row.add(String.valueOf(student.anonId));
	}

	private void addCourseGradeData(List<String> row, OwlExportConfig config, OwlGbStudentGradeInfo student)
	{
		if (config.includePoints)
		{
			row.add(FormatHelper.formatGradeForDisplay(FormatHelper.formatDoubleToDecimal(student.info.getCourseGrade().getCourseGrade().getPointsEarned())));
		}
		if (config.includeCalculatedGrade)
		{
			row.add(FormatHelper.formatGradeForDisplay(student.info.getCourseGrade().getCourseGrade().getCalculatedGrade()));
		}
		if (config.includeFinalGrade)
		{
			row.add(FinalGradeFormatter.format(student.info.getCourseGrade()));
		}
		if (config.includeGradeOverride)
		{
			row.add(FormatHelper.formatGradeForDisplay(student.getGradeOverride().orElse("")));
		}
		if (config.includeLastLogDate)
		{
			Date overrideDate = student.info.getCourseGrade().getCourseGrade().getDateRecorded();
			row.add(overrideDate == null ? "" : FormatHelper.formatDateTime(overrideDate));
		}
	}
}
