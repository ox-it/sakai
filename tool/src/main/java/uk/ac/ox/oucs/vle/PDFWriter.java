package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * A CSV file writer. This escapes all the passed content so it can be written
 * safely to a CSV file.
 * Implementation created as we can't reuse an existing GPL CSV project.
 * Guidance from:
 * http://www.creativyst.com/Doc/Articles/CSV/CSV01.htm
 * @author buckett
 */
public class PDFWriter
{
    // Starts of ends with a blank, or contains a double quote, comma or newline
    //private static final Pattern shouldQuote = Pattern.compile("(^\\p{Blank})|\"|,|\\n|\\r|(\\p{Blank}$)");
    //private static final Pattern doubleQuote = Pattern.compile("\""); 
    //private static final String separator = ",";
    
    //private Writer out;
    //private boolean firstColumn = true;
    //private String lineEnding = "\n";

    private Document document;
    private PdfWriter pdfWriter;
    private PdfPTable table;

    private Font tableHeadFont;
    private Font tableFont;
   
    private Font titleFont;
    private Font authorFont;
    private Font infoFont;
  
    /**
     * Create the writer wrapping up an existing writer.
     * @param out The output to send the CSV file to.
     * @throws IOException 
     */
    public PDFWriter (OutputStream out) throws IOException {
    	
        document = new Document();
        try {
            pdfWriter = PdfWriter.getInstance(document, out);
            pdfWriter.setStrictImageSequence(true);
            document.open();

            tableHeadFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
            tableFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.NORMAL);
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

    /**
     * Write a single data value to the CSV file.
     * @param data The data to write to the CSV file
     */
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
    		phrase = new Phrase("\nPresenter: " + courseComponent.getPresenter().getName(), authorFont);
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
    	
    	table = new PdfPTable(new float[]{1f, 1f, 1f, 2f});
    	table.setWidthPercentage(90);
    	table.setSpacingBefore(10f);

    	table.addCell(headCell("Name", tableHeadFont));
    	table.addCell(headCell("Department", tableHeadFont));
    	table.addCell(headCell("OSS Number", tableHeadFont));
    	table.addCell(headCell("I confirm that I attended", tableHeadFont));

    	table.setHeaderRows(1);
    }
    
    public void writeTableBody(List<Person> people) throws IOException {
    	for (Person person : people) {
    		table.addCell(nameCell(person.getName(), tableFont));
    		table.addCell(nameCell(person.getDepartmentName(), tableFont));
    		table.addCell(nameCell(person.getWebauthId(), tableFont));
    		table.addCell(nameCell("", tableFont));
    	}
    }
    
    public void writeTableFoot() throws IOException {
    	try {
    		for (int i = 0; i < 5; i++) {
    			table.addCell(nameCell("", tableFont));
    			table.addCell(nameCell("", tableFont));
    			table.addCell(nameCell("", tableFont));
    			table.addCell(nameCell("", tableFont));
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
    	pdfCell.setFixedHeight(font.getSize()*2f);
    	pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    	pdfCell.setVerticalAlignment(Element.ALIGN_CENTER);
    	pdfCell.setPaddingBottom(font.getSize()*0.5f);
    	pdfCell.setPaddingTop(font.getSize()*0.5f);
    	pdfCell.setPaddingLeft(font.getSize());
    	pdfCell.setPaddingRight(font.getSize());
    	return pdfCell;
    }
    
    private PdfPCell nameCell(String name, Font font) {
    	PdfPCell pdfCell = new PdfPCell(new Phrase(name, font));
    	pdfCell.setFixedHeight(font.getSize()*2f);
    	pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    	pdfCell.setVerticalAlignment(Element.ALIGN_CENTER);
    	pdfCell.setPaddingBottom(font.getSize()*0.5f);
    	pdfCell.setPaddingTop(font.getSize()*0.5f);
    	pdfCell.setPaddingLeft(font.getSize());
    	pdfCell.setPaddingRight(font.getSize());
    	return pdfCell;
    }
}
