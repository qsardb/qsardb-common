/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.spreadsheet;

import java.util.*;

import org.qsardb.conversion.table.*;

abstract
public class Worksheet extends Table {

	private String id = null;


	public Worksheet(String id){
		setId(id);
	}

	abstract
	public Dimension getSize();

	abstract
	public String getValueAt(int row, int column);

	@Override
	public Iterator<Column> columns(){
		final
		int columns = getColumnCount();

		return new Iterator<Column>(){

			private int column = 0;


			public boolean hasNext(){
				return this.column < columns;
			}

			public Column next(){
				return getColumn(this.column++);
			}

			public void remove(){
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Iterator<Row> rows(){
		final
		int rows = getRowCount();

		return new Iterator<Row>(){

			private int row = 0;


			public boolean hasNext(){
				return (this.row < rows);
			}

			public Row next(){
				return getRow(this.row++);
			}

			public void remove(){
				throw new UnsupportedOperationException();
			}
		};
	}

	public String getId(){
		return this.id;
	}

	private void setId(String id){
		this.id = id;
	}

	public int getColumnCount(){
		return getSize().getColumns();
	}

	public Column getColumn(int column){
		return new Column(formatColumnId(column));
	}

	public int getRowCount(){
		return getSize().getRows();
	}

	public Row getRow(int row){
		Map<Column, Cell> values = new LinkedHashMap<Column, Cell>();

		int columns = getColumnCount();
		for(int column = 0; column < columns; column++){
			values.put(getColumn(column), getCell(row, column));
		}

		return new Row(formatRowId(row), values);
	}

	public Cell getCell(int row, int column){
		return new Cell(getValueAt(row, column));
	}

	static
	public String formatColumnId(int column){

		if(column < 0){
			throw new IllegalArgumentException();
		}

		int length = 1;
		int divisor = 1;

		int columnOffset = 0;

		for(int limit = ALPHABET.length(); true; limit *= ALPHABET.length()){

			if(column >= columnOffset && column < (columnOffset + limit)){
				break;
			}

			length++;
			divisor *= ALPHABET.length();

			columnOffset += limit;
		}

		column -= columnOffset;

		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < length; i++){
			int index = (column / divisor);
			sb.append(ALPHABET.charAt(index));

			column -= (index * divisor);

			divisor /= ALPHABET.length();
		}

		return sb.toString();
	}

	static
	public int parseColumnId(String id){
		int column = 0;

		int length = id.length();
		int multiplier = 1;

		for(int i = length - 1; i > -1; i--){
			int index = ALPHABET.indexOf(id.charAt(i));
			if(index < 0){
				throw new IllegalArgumentException();
			}

			if(multiplier == 1){
				column = index;
			} else

			if(multiplier > 1){
				column += (index + 1) * multiplier;
			}

			multiplier *= ALPHABET.length();
		}

		return column;
	}

	static
	public String formatRowId(int row){

		if(row < 0){
			throw new IllegalArgumentException();
		}

		return String.valueOf(row + 1);
	}

	static
	public int parseRowId(String id){
		return Integer.parseInt(id) - 1;
	}

	protected static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
}