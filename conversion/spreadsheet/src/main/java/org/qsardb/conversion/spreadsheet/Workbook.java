/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.spreadsheet;

abstract
public class Workbook {

	abstract
	public int getWorksheetCount();

	abstract
	public Worksheet getWorksheet(int sheet);
}