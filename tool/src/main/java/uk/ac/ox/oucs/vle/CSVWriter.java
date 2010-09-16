package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * A CSV file writer. This escapes all the passed content so it can be written
 * safely to a CSV file.
 * Implementation created as we can't reuse an existing GPL CSV project.
 * Guidance from:
 * http://www.creativyst.com/Doc/Articles/CSV/CSV01.htm
 * @author buckett
 */
public class CSVWriter
{
    // Starts of ends with a blank, or contains a double quote, comma or newline
    private static final Pattern shouldQuote = Pattern.compile("(^\\p{Blank})|\"|,|\\n|\\r|(\\p{Blank}$)");
    private static final Pattern doubleQuote = Pattern.compile("\""); 
    private static final String separator = ",";
    
    private Writer out;
    private boolean firstColumn = true;
    private String lineEnding = "\n";

    
    /**
     * Create the writer wrapping up an existing writer.
     * @param out The output to send the CSV file to.
     */
    public CSVWriter (Writer out)
    {
        this.out = out;
    }

    /**
     * Write a single data value to the CSV file.
     * @param data The data to write to the CSV file
     */
    public void write(String data) throws IOException
    {
        if (firstColumn)
            firstColumn = false;
        else
            out.write(separator);
        out.write(formatData(data));
    }
    
    /**
     * Terminates the current CSV row.
     */
    public void writeln() throws IOException
    {
        out.write(lineEnding);
        firstColumn = true;
    }
    
    /**
     * Outputs a complete CSV row.
     * @param columns The data to write out to row. 
     */
    public void writeln(String[] columns) throws IOException
    {
        for (int column = 0; column < columns.length; column++)
            write(columns[column]);
        writeln();
    }
    
    
    private String formatData(String data)
    {
        if (shouldQuote.matcher(data).find())
        {
            data = "\""+ doubleQuote.matcher(data).replaceAll("\"\"")+ "\"";
        }
        return data;
    }

}
