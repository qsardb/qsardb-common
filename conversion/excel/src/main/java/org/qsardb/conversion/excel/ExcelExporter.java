/*
 * Copyright (c) 2013 University of Tartu
 */
package org.qsardb.conversion.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.qsardb.conversion.table.Column;
import org.qsardb.conversion.table.TableExporter;
import org.qsardb.model.Model;


public class ExcelExporter extends TableExporter {

	private final OutputStream outputStream;
	private final Workbook workBook;
	private String worksheetName = "Dataset";

	public ExcelExporter(OutputStream os) {
		this(os, false);
	}

	public ExcelExporter(OutputStream os, boolean oldFormat) {
		outputStream = os;
		if (oldFormat) {
			workBook = new HSSFWorkbook();
		} else {
			workBook = new XSSFWorkbook();
		}
	}

	@Override
	public void prepareModel(Model model) {
		worksheetName = model.getId();
		super.prepareModel(model);
	}

	@Override
	public void write() throws Exception {
		Sheet sheet = workBook.createSheet(worksheetName);

		Row headerRow = sheet.createRow(0);
		for (Iterator<Column> it=columns(); it.hasNext();) {
			String colName = it.next().getId();
			Cell cell = headerRow.createCell(headerRow.getPhysicalNumberOfCells());
			cell.setCellValue(colName);
		}

		for (Iterator<org.qsardb.conversion.table.Row> it=rows(); it.hasNext();) {
			Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
			for (org.qsardb.conversion.table.Cell cellValue:  it.next().getValues().values()) {
				Cell cell = row.createCell(row.getPhysicalNumberOfCells());

				String v = cellValue.getText();
				try {
					cell.setCellValue(Double.parseDouble(v));
				} catch (NumberFormatException e) {
					cell.setCellValue(v);
				}
			}
		}

		workBook.write(outputStream);
		outputStream.close();
	}

	@Override
	public void close() throws IOException {
		outputStream.close();
	}
	
}
