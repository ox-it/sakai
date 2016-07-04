package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

public class SheetReaderCSV implements SheetReader {
	
	private CSVReader reader;
	private String[] row;
	private int currentColumn = -1;
	private int currentRow = -1;

	public SheetReaderCSV(InputStream source) {
		reader = new CSVReader(new InputStreamReader(source));
	}

	public boolean nextRow() {
		try {
			row = reader.readNext();
			if(row != null) {
				currentRow++;
				currentColumn = -1;
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean nextColumn() {
		if (currentColumn + 1 < row.length) {
			currentColumn++;
			return true;
		}
		return false;
	}

	public int getRow() {
		return currentRow+1;
	}

	public int getColumn() {
		return currentColumn+1;
	}

	public String getContents() {
		return (currentColumn<row.length)?row[currentColumn]:null;
	}

}
