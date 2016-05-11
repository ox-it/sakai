package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

abstract public class SheetWriterExcel implements SheetWriter {

	private OutputStream out;
	private Sheet createSheet;
	private int rowNum = 0;
	private int columnNum = 0;
	private Workbook wb;
	private Row row;

	public SheetWriterExcel(OutputStream out) {
		this.out = out;
		wb = getWorkbook();
		createSheet = wb.createSheet();
		nextRow(); // Needed to start the first row.
	}

	/**
	 * This is called by the constructor to create the workbook.
	 */
	public abstract Workbook getWorkbook();

	public void nextRow() {
		row = createSheet.createRow(rowNum++);
		columnNum = 0;
	}

	public void writeColumn(String value) {
		Cell cell = row.createCell(columnNum++, Cell.CELL_TYPE_STRING);
		cell.setCellValue(value);
	}

	public void flush() {
		try {
			wb.write(new DontCloseMyOutputStreamThankYou(out));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * The Apache POI library closes an OutputStream after writing to it, this
	 * breaks streaming of multiple files to a zipfile.
	 * @author buckett
	 *
	 */
	private class DontCloseMyOutputStreamThankYou extends OutputStream {

		private OutputStream wrapped;

		private DontCloseMyOutputStreamThankYou(OutputStream wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public void write(byte b[]) throws IOException {
			wrapped.write(b, 0, b.length);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			wrapped.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			wrapped.flush();
		}

		@Override
		public void close() throws IOException {
			wrapped.flush();
			// Not doing anything, I can manage my streams.
		}

		@Override
		public void write(int b) throws IOException {
			wrapped.write(b);
		}

	}
}
