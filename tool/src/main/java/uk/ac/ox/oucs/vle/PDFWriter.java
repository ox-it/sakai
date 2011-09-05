package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.MultiColumnText;
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
    private MultiColumnText responseArea;

    private Font paragraphFont;
    private Font paragraphFontBold;
    private Font tableHeadFont;
    private Font tableHeadFontBold;
    private Font tableFont;
    private Font tableFontBold;
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

            // attempting to handle i18n chars better
            // BaseFont evalBF = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.IDENTITY_H,
            // BaseFont.EMBEDDED);
            // paragraphFont = new Font(evalBF, 9, Font.NORMAL);
            // paragraphFont = new Font(Font.TIMES_ROMAN, 9, Font.NORMAL);

            paragraphFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.NORMAL);
            paragraphFontBold = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.BOLD);
            tableHeadFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
            tableHeadFontBold = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.BOLD);
            tableFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.NORMAL);
            tableFontBold = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.BOLD);
            titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 18, Font.NORMAL);
            authorFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
            infoFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
            
        } catch (Exception e) {
            throw new IOException("Unable to start PDF Report");
        }
    }
    
    public void close() throws IOException {
        //document.add(responseArea);
		document.close();
    }

    /**
     * Write a single data value to the CSV file.
     * @param data The data to write to the CSV file
     */
    public void writeHead(CourseGroup group, Person presenter) throws IOException
    {
    	try {
    		//PdfContentByte cb = pdfWriter.getDirectContent();
    		//float docMiddle = (document.right() - document.left()) / 2 + document.leftMargin();

    		//Paragraph emptyPara = new Paragraph(" ");
    		//emptyPara.setSpacingAfter(100.0f);
    		Paragraph paragraph;
    		Phrase phrase;

    		// Title
    		paragraph = new Paragraph();
    		phrase = new Phrase("\n" + group.getTitle(), titleFont);
    		paragraph.add(phrase);
    		paragraph.setAlignment(Element.ALIGN_CENTER);
    		document.add(paragraph);
    		
    		// Presenter
    		paragraph = new Paragraph();
    		phrase = new Phrase("\nPresenter: " + presenter.getName(), authorFont);
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
    
    public void writeTable(List<Person> people) throws IOException
    {
    	try {
			
    		PdfPTable table = new PdfPTable(new float[]{1f, 2f});
    		table.setWidthPercentage(90);
    		table.setSpacingBefore(10f);
    		Paragraph paragraph = new Paragraph();

    		// t.setBorderColor(BaseColor.GRAY);
    		// t.setPadding(4);
    		// t.setSpacing(4);
    		// t.setBorderWidth(1);

    		table.addCell(headCell("Name", tableHeadFont));
    		table.addCell(headCell("I confirm that I attended this session", tableHeadFont));

    		table.setHeaderRows(1);
    		
    		for (Person person : people) {
    			table.addCell(nameCell(person.getName(), tableFont));
    			table.addCell(nameCell("", tableFont));
    		}
    
    		for (int i = 0; i < 5; i++) {
    			table.addCell(nameCell("", tableFont));
    			table.addCell(nameCell("", tableFont));
    		}
    	
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
