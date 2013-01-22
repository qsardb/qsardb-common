/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.opendocument;

import java.io.*;

import org.qsardb.conversion.spreadsheet.*;

import org.jopendocument.dom.*;
import org.jopendocument.dom.spreadsheet.*;

public class OpenDocumentWorkbook extends Workbook {

	private SpreadSheet spreadsheet = null;


	public OpenDocumentWorkbook(InputStream is) throws IOException {
		this.spreadsheet = SpreadSheet.create(new ODPackage(is));
	}

	@Override
	public int getWorksheetCount(){
		return this.spreadsheet.getSheetCount();
	}

	@Override
	public OpenDocumentWorksheet getWorksheet(int sheet){
		return new OpenDocumentWorksheet(this.spreadsheet.getSheet(sheet));
	}
}