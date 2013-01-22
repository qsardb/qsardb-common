/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.csv;

import java.awt.*;
import java.util.List;

import org.qsardb.conversion.spreadsheet.*;
import org.qsardb.conversion.spreadsheet.Dimension;

import org.apache.commons.csv.*;

public class CsvWorksheet extends Worksheet {

	private List<CSVRecord> records = null;


	public CsvWorksheet(List<CSVRecord> records){
		super(null);

		this.records = records;
	}

	@Override
	public Dimension getSize(){
		int rows = this.records.size();
		int columns = (rows > 0 ? getRecord(0).size() : 0);

		return new Dimension(rows, columns);
	}

	@Override
	public String getValueAt(int row, int column){
		return getRecord(row).get(column);
	}

	private CSVRecord getRecord(int row){
		return this.records.get(row);
	}
}