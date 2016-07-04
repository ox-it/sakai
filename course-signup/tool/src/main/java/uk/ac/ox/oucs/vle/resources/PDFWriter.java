/*
 * #%L
 * Course Signup Webapp
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle.resources;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import uk.ac.ox.oucs.vle.CourseComponent;
import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.Person;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public class PDFWriter {
	// Starts of ends with a blank, or contains a double quote, comma or newline

	private Document document;
	private PdfWriter pdfWriter;
	private PdfPTable table;

	private Font tableHeadFont;
	private Font tableNameFont;
	private Font tableOtherFont;

	private Font titleFont;
	private Font authorFont;
	private Font infoFont;

	/**
	 * Create a new attendance PDF writer
	 * @param out The outputstream to write the PDF to.
	 * @throws IOException If there is a problem writing to the outputstream.
	 */
	public PDFWriter(OutputStream out) throws IOException {

		document = new Document();
		try {
			pdfWriter = PdfWriter.getInstance(document, out);
			pdfWriter.setStrictImageSequence(true);
			document.open();

			tableHeadFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
			tableNameFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.NORMAL);
			tableOtherFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.NORMAL);
			titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 18, Font.NORMAL);
			authorFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
			infoFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);

		} catch (Exception e) {
			throw new IOException("Unable to start PDF Report");
		}
	}

	public void close() throws IOException {
		document.close();
	}

	public void writeHead(Collection<CourseGroup> courseGroups, CourseComponent courseComponent) throws IOException {
		try {
			Paragraph paragraph;
			Phrase phrase;

			// Title
			paragraph = new Paragraph();
			for (CourseGroup courseGroup : courseGroups) {
				phrase = new Phrase("\n" + courseGroup.getTitle(), titleFont);
				paragraph.add(phrase);
			}
			paragraph.setAlignment(Element.ALIGN_CENTER);
			document.add(paragraph);

			// Component
			paragraph = new Paragraph();
			phrase = new Phrase("\nComponent: " + courseComponent.getTitle(), authorFont);
			paragraph.add(phrase);
			paragraph.setIndentationLeft(25);
			paragraph.setIndentationRight(25);
			paragraph.setAlignment(Element.ALIGN_LEFT);
			document.add(paragraph);

			// Presenter
			paragraph = new Paragraph();
			Person presenter = courseComponent.getPresenter();
			phrase = new Phrase("\nPresenter: " + ((presenter == null)?"":presenter.getName()), authorFont);
			paragraph.add(phrase);
			paragraph.setIndentationLeft(25);
			paragraph.setIndentationRight(25);
			paragraph.setAlignment(Element.ALIGN_LEFT);
			document.add(paragraph);

			// Date
			paragraph = new Paragraph();
			phrase = new Phrase("Date/Time: ...........................................", infoFont);
			paragraph.add(phrase);
			paragraph.setIndentationLeft(25);
			paragraph.setIndentationRight(25);
			document.add(paragraph);

			// info
			paragraph = new Paragraph();
			phrase = new Phrase("Please sign to confirm that you have attended this session", infoFont);
			paragraph.add(phrase);
			paragraph.setIndentationLeft(25);
			paragraph.setIndentationRight(25);
			paragraph.setAlignment(Element.ALIGN_LEFT);
			document.add(paragraph);

		} catch (DocumentException e) {
			throw new IOException("Unable to write Document Header.");
		}
	}

	public void writeTableHead() throws IOException {

		table = new PdfPTable(new float[]{3f, 2f});
		table.setWidthPercentage(90);
		table.setSpacingBefore(10f);

		table.addCell(headCell("Name", tableHeadFont));
		table.addCell(headCell("I confirm that I attended", tableHeadFont));

		table.setHeaderRows(1);
	}

	public void writeTableBody(List<Person> people) throws IOException {
		for (Person person : people) {
			table.addCell(nameCell(person.getName(), person.getWebauthId(), person.getDepartmentName()));
			table.addCell("");
		}
	}

	public void writeTableFoot() throws IOException {
		try {
			for (int i = 0; i < 5; i++) {
				table.addCell(nameCell("", "", ""));
				table.addCell("");
			}

			Paragraph paragraph = new Paragraph();
			paragraph.add(table);
			paragraph.setAlignment(Element.ALIGN_CENTER);
			document.add(paragraph);
		} catch (DocumentException e) {
			throw new IOException("Unable to write Document table.");
		}
	}

	private PdfPCell headCell(String name, Font font) {

		PdfPCell pdfCell = new PdfPCell(new Phrase(name, font));
		pdfCell.setMinimumHeight(font.getSize() * 2f);
		pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		pdfCell.setVerticalAlignment(Element.ALIGN_CENTER);
		pdfCell.setPaddingBottom(font.getSize() * 0.5f);
		pdfCell.setPaddingTop(font.getSize() * 0.5f);
		pdfCell.setPaddingLeft(font.getSize());
		pdfCell.setPaddingRight(font.getSize());
		return pdfCell;
	}

	private PdfPCell nameCell(String name, String webauthId, String department) {

		Phrase phrase = new Phrase();
		phrase.add(new Chunk(name, tableNameFont));
		phrase.add(Chunk.NEWLINE);
		StringBuilder otherDetails = new StringBuilder();
		if (webauthId != null && webauthId.trim().length() > 0) {
			otherDetails.append(webauthId);
		}
		if (department != null && department.trim().length() > 0) {
			if (otherDetails.length() > 0) {
				otherDetails.append(" ");
			}
			otherDetails.append(department);
		}
		phrase.add(new Chunk(otherDetails.toString(), tableOtherFont));

		PdfPCell pdfCell = new PdfPCell(phrase);
		pdfCell.setMinimumHeight(tableNameFont.getSize() * 2f);
		pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		pdfCell.setVerticalAlignment(Element.ALIGN_CENTER);
		pdfCell.setPaddingBottom(tableNameFont.getSize() * 0.5f);
		pdfCell.setPaddingTop(tableNameFont.getSize() * 0.5f);
		pdfCell.setPaddingLeft(tableNameFont.getSize());
		pdfCell.setPaddingRight(tableNameFont.getSize());
		return pdfCell;
	}
}
