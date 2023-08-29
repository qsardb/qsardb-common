/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.excel;

import org.qsardb.conversion.spreadsheet.*;

import org.apache.poi.ss.usermodel.*;

public class ExcelWorksheet extends Worksheet {

	private Sheet sheet = null;

	private Dimension size = null;


	public ExcelWorksheet(Sheet sheet){
		super(sheet.getSheetName());

		this.sheet = sheet;
	}

	@Override
	public Dimension getSize(){

		if(this.size == null){
			this.size = calculateSize();
		}

		return this.size;
	}

	private Dimension calculateSize() {
		int lastRow = 0;
		int lastColumn = 0;

		for (int rowIdx = this.sheet.getLastRowNum(); rowIdx >= 0; rowIdx--) {
			Row row = this.sheet.getRow(rowIdx);
			if (row == null) {
				continue;
			}

			for (int colIdx = row.getLastCellNum()-1; colIdx >= 0; colIdx--) {
				if (getValueAt(row.getCell(colIdx)) != null) {
					lastColumn = Math.max(lastColumn, colIdx+1);
					if (lastRow == 0) {
						lastRow = rowIdx + 1;
					}
					break;
				}
			}
		}

		return new Dimension(lastRow, lastColumn);
	}

	@Override
	public String getValueAt(int row, int column){
		return getValueAt(this.sheet.getRow(row), column);
	}

	private String getValueAt(Row row, int column){

		if(row == null){
			return null;
		}

		return getValueAt(row.getCell(column));
	}

	private String getValueAt(Cell cell){

		if(cell == null){
			return null;
		}

		switch(cell.getCellType()){
			case Cell.CELL_TYPE_STRING:
				return String.valueOf(cell.getStringCellValue());
			case Cell.CELL_TYPE_NUMERIC:
				double doubleVal = cell.getNumericCellValue();
				int intVal = (int)doubleVal;

				if (intVal == doubleVal) {
					return String.valueOf(intVal);
				} else {
					return String.valueOf(doubleVal);
				}
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			default:
				return null;
		}
	}
}