/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.util.*;

public class ArrayTable extends Table {

	private List<Column> columns = null;

	private List<Row> rows = null;


	public ArrayTable(String[]... data){
		this(null, null, data);
	}

	public ArrayTable(String[] columnIds, String[] rowIds, String[]... data){

		if(columnIds == null){
			columnIds = prepareColumnIds(data);
		} // End if

		if(rowIds == null){
			rowIds = prepareRowIds(data);
		}

		List<Column> columns = new ArrayList<Column>();

		int columnCount = columnIds.length;
		for(int i = 0; i < columnCount; i++){
			columns.add(new Column(columnIds[i]));
		}

		List<Row> rows = new ArrayList<Row>();

		int rowCount = rowIds.length;
		if(rowCount < data.length){
			throw new IllegalArgumentException("Number of data rows exceeds the number of row identifiers");
		}

		for(int i = 0; i < rowCount; i++){
			int cellCount = data[i].length;
			if(columnCount < cellCount){
				throw new IllegalArgumentException("Number of data columns at row " + i + " exceeds the number of column identifiers");
			}

			Map<Column, Cell> cells = new LinkedHashMap<Column, Cell>();

			for(int j = 0; j < cellCount; j++){
				cells.put(columns.get(j), new Cell(data[i][j]));
			}

			rows.add(new Row(rowIds[i], cells));
		}

		setColumns(columns);
		setRows(rows);
	}

	@Override
	public Iterator<Column> columns(){
		return getColumns().iterator();
	}

	@Override
	public Iterator<Row> rows(){
		return getRows().iterator();
	}

	public List<Column> getColumns(){
		return this.columns;
	}

	private void setColumns(List<Column> columns){
		this.columns = columns;
	}

	public List<Row> getRows(){
		return this.rows;
	}

	private void setRows(List<Row> rows){
		this.rows = rows;
	}

	static
	private String[] prepareColumnIds(String[]... data){
		int columnCount = 0;
		int rowCount = data.length;

		for(int i = 0; i < rowCount; i++){
			columnCount = Math.max(columnCount, data[i].length);
		}

		List<String> ids = new ArrayList<String>();

		for(int i = 0; i < columnCount; i++){
			ids.add(prepareId(i));
		}

		return ids.toArray(new String[ids.size()]);
	}

	static
	private String[] prepareRowIds(String[]... data){
		int rowCount = data.length;

		List<String> ids = new ArrayList<String>();

		for(int i = 0; i < rowCount; i++){
			ids.add(prepareId(i));
		}

		return ids.toArray(new String[ids.size()]);
	}

	static
	private String prepareId(int index){
		return "_" + String.valueOf(index + 1);
	}
}