/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.util.*;

public class TableUtil {

	private TableUtil(){
	}

	static
	public void print(Table table){

		try {
			List<String> columns = formatColumnIds(table);
			List<String> rows = formatRowIds(table);

			System.out.println(columns);
			System.out.println(rows);

			List<List<String>> cells = TableUtil.formatCells(table);
			for(List<String> data : cells){
				System.out.println(format(data));
			}
		} catch(Exception e){
			// XXX
		}
	}

	static
	public List<String> formatColumnIds(Table table) throws Exception {
		List<String> result = new ArrayList<String>();

		for(Iterator<Column> it = table.columns(); it.hasNext(); ){
			Column column = it.next();

			result.add(format(column.getId()));
		}

		return result;
	}

	static
	public List<String> formatRowIds(Table table) throws Exception {
		List<String> result = new ArrayList<String>();

		for(Iterator<Row> it = table.rows(); it.hasNext(); ){
			Row row = it.next();

			result.add(format(row.getId()));
		}

		return result;
	}

	static
	public List<List<String>> formatCells(Table table) throws Exception {
		List<List<String>> result = new ArrayList<List<String>>();

		List<Column> columns = new ArrayList<Column>();

		for(Iterator<Column> it = table.columns(); it.hasNext(); ){
			Column column = it.next();

			columns.add(column);
		}

		for(Iterator<Row> it = table.rows(); it.hasNext(); ){
			Row row = it.next();

			Map<Column, Cell> values = row.getValues();

			List<String> data = new ArrayList<String>();

			for(Column column : columns){
				Cell cell = values.get(column);

				data.add(format(cell != null ? cell.getText() : null));
			}

			result.add(data);
		}

		return result;
	}

	static
	public String format(List<String> strings){
		return format(strings, "\t");
	}

	static
	public String format(List<String> strings, String separator){
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < strings.size(); i++){
			String string = strings.get(i);

			sb.append(i > 0 ? separator : "");

			sb.append(string);
		}

		return sb.toString();
	}

	static
	private String format(String string){

		if(string == null){
			return null;
		}

		StringBuilder sb = new StringBuilder(string.length() * 2);

		for(int i = 0; i < string.length(); i++){
			char c = string.charAt(i);

			if(c >= 32 && c <= 127){
				sb.append(c);
			} else

			{
				sb.append("\\u");

				String hex = Integer.toHexString(c);

				for(int j = 0; j < (4 - hex.length()); j++){
					sb.append('0');
				}

				sb.append(hex);
			}
		}

		return sb.toString();
	}
}