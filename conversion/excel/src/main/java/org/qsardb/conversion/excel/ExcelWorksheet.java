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

	private Dimension calculateSize(){
		int rows = 0;
		int columns = 0;

		if(this.sheet.getPhysicalNumberOfRows() > 0){
			rows = this.sheet.getLastRowNum() + 1;

			for(Row row : this.sheet){

				if(row.getPhysicalNumberOfCells() > 0){
					columns = Math.max(columns, row.getLastCellNum());
				}
			}
		}

		return new Dimension(rows, columns);
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
				return String.valueOf(cell.getNumericCellValue());
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			default:
				return null;
		}
	}
}