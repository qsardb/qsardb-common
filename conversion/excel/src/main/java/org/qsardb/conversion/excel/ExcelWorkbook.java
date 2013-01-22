/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.excel;

import java.io.*;

import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.ss.usermodel.*;

public class ExcelWorkbook extends org.qsardb.conversion.spreadsheet.Workbook {

	private Workbook workbook = null;


	public ExcelWorkbook(InputStream is) throws IOException, InvalidFormatException {
		this.workbook = WorkbookFactory.create(is);
	}

	@Override
	public int getWorksheetCount(){
		return this.workbook.getNumberOfSheets();
	}

	@Override
	public ExcelWorksheet getWorksheet(int sheet){
		return new ExcelWorksheet(this.workbook.getSheetAt(sheet));
	}
}