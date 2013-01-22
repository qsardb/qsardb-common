/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.io.*;
import java.util.*;

abstract
public class Table {

	abstract
	public Iterator<Column> columns() throws Exception;

	public Column getColumn(String id) throws Exception {
		Iterator<Column> columns = columns();

		try {
			while(columns.hasNext()){
				Column column = columns.next();

				if((column.getId()).equals(id)){
					return column;
				}
			}
		} finally {
			close(columns);
		}

		return null;
	}

	abstract
	public Iterator<Row> rows() throws Exception;

	public Row getRow(String id) throws Exception {
		Iterator<Row> rows = rows();

		try {
			while(rows.hasNext()){
				Row row = rows.next();

				if((row.getId()).equals(id)){
					return row;
				}
			}
		} finally {
			close(rows);
		}

		return null;
	}

	private void close(Object object) throws IOException {

		if(object instanceof Closeable){
			Closeable closeable = (Closeable)object;

			closeable.close();
		}
	}
}