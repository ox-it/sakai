package uk.ac.ox.oucs.oxam.readers;

import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

public class SheetWriterExcelXLS extends SheetWriterExcel {

	public SheetWriterExcelXLS(OutputStream out) {
		super(out);
	}

	@Override
	public Workbook getWorkbook() {
		return new HSSFWorkbook();
	}

}
