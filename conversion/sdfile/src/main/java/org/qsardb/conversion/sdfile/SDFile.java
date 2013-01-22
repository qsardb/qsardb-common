/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.sdfile;

import java.io.*;
import java.util.*;

import org.qsardb.conversion.table.*;

public class SDFile extends Table {

	private File file = null;


	public SDFile(File file){
		this.file = file;
	}

	@Override
	public Iterator<Column> columns() throws IOException {
		Set<Column> columns = new LinkedHashSet<Column>();

		CompoundIterator rows = new CompoundIterator(this.file);

		try {
			while(rows.hasNext()){
				Row row = rows.next();

				columns.addAll((row.getValues()).keySet());
			}
		} finally {
			rows.close();
		}

		return columns.iterator();
	}

	@Override
	public Iterator<Row> rows() throws IOException {
		CompoundIterator rows = new CompoundIterator(this.file);

		return rows;
	}

	public static final String COLUMN_MOLFILE = "molfile";
}