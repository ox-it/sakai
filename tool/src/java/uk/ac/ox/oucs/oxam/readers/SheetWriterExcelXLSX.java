package uk.ac.ox.oucs.oxam.readers;

import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SheetWriterExcelXLSX extends SheetWriterExcel {

	public SheetWriterExcelXLSX(OutputStream out) {
		super(out);
	}

	@Override
	public Workbook getWorkbook() {
		return new XSSFWorkbook();
	}

}
