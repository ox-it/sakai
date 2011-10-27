package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

public class SheetReaderCSV implements SheetReader {
	
	private CSVReader reader;
	private String[] row;
	private int currentColumn = 0;
	private int currentRow = 0;

	public SheetReaderCSV(InputStream source) {
		reader = new CSVReader(new InputStreamReader(source));

	}

	public boolean nextRow() {
		try {
			row = reader.readNext();
			if(row != null) {
				currentRow++;
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean nextColumn() {
		if (++currentColumn < row.length) {
			return true;
		}
		return false;
	}

	public int getRow() {
		return currentRow;
	}

	public int getColumn() {
		return currentColumn;
	}

	public String getContents() {
		return (currentColumn<row.length)?row[currentColumn]:null;
	}

}
