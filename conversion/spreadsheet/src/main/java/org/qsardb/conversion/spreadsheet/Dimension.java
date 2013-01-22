/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.spreadsheet;

public class Dimension {

	private int rows = 0;

	private int columns = 0;


	public Dimension(int rows, int columns){
		setRows(rows);
		setColumns(columns);
	}

	public int getRows(){
		return this.rows;
	}

	private void setRows(int rows){
		this.rows = rows;
	}

	public int getColumns(){
		return this.columns;
	}

	private void setColumns(int columns){
		this.columns = columns;
	}
}