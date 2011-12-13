package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class SheetWriterCSV implements SheetWriter {

	CSVWriter writer;
	List<String> row;
	
	public SheetWriterCSV(OutputStream out) {
		this.writer = new CSVWriter(new OutputStreamWriter(out));
		row = new ArrayList<String>();
	}
	public void nextRow() {
		writer.writeNext(row.toArray(new String[]{}));
		row.clear();
	}

	public void writeColumn(String value) {
		row.add(value);
	}
	public void flush() {
		try {
			writer.flush();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
