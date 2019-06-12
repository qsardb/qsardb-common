/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.sdfile;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.qsardb.conversion.table.Cell;
import org.qsardb.conversion.table.Column;
import org.qsardb.conversion.table.Row;

class CompoundIterator implements Iterator<Row>, Closeable {

	private int index = 1;

	private LineNumberReader reader = null;

	private Row row = null;


	CompoundIterator(File file) throws IOException {
		this.reader = new LineNumberReader(new FileReader(file));

		this.row = readRow();
	}

	public boolean hasNext(){
		return this.row != null;
	}

	public Row next(){
		Row row = this.row;

		if(row == null){
			throw new NoSuchElementException();
		}

		this.row = readRow();

		return row;
	}

	public void remove(){
		throw new UnsupportedOperationException();
	}

	private Row readRow(){
		Map<Column, Cell> values = new LinkedHashMap<Column, Cell>();

		try {
			values.putAll(readMolfile());
			values.putAll(readData());
		} catch(EOFException eofe){
			return null;
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}

		return new Row(String.valueOf(this.index++), values);
	}

	private Map<Column, Cell> readMolfile() throws IOException {
		Map<Column, Cell> values = new LinkedHashMap<Column, Cell>();

		LineNumberReader reader = ensureOpen();

		StringBuilder sb = new StringBuilder();

		// Header block
		String first = reader.readLine();
		String second = reader.readLine();
		String third = reader.readLine();

		if(first == null || second == null || third == null){
			throw new EOFException();
		}

		sb.append(first).append(NEWLINE);
		sb.append(second).append(NEWLINE);
		sb.append(third).append(NEWLINE);

		String sep = "";

		// Ctab block
		while(true){
			String line = reader.readLine();
			if(line == null){
				throw new EOFException();
			}

			sb.append(sep);
			sep = NEWLINE;

			sb.append(line);

			if(line.startsWith("M  END")){
				break;
			}
		}

		String molfile = sb.toString();
		values.put(new Column(SDFile.COLUMN_MOLFILE_TITLE), new Cell(first));
		values.put(new Column(SDFile.COLUMN_MOLFILE), new Cell(molfile));

		return values;
	}

	private Map<Column, Cell> readData() throws IOException {
		LineNumberReader reader = ensureOpen();

		Map<Column, Cell> values = new LinkedHashMap<Column, Cell>();

		String name = null;
		StringBuilder data = new StringBuilder();

		for (String lin; (lin = reader.readLine()) != null; ) {
			if (lin.startsWith("> ") && name == null && data.length() == 0) {
				int begin = lin.indexOf("<") + 1;
				int end = lin.indexOf(">", begin);
				if (begin < 3 || end - begin < 1) {
					throw new IOException("Error parsing line: " + (reader.getLineNumber() + 1));
				}
				name = lin.substring(begin, end);
			} else if (!lin.isEmpty() && name != null) {
				if (data.length() > 0) {
					data.append(NEWLINE);
				}
				data.append(lin);
			} else if (lin.isEmpty() && name != null) {
				values.put(new Column(name), new Cell(data.toString()));

				name = null;
				data = new StringBuilder();
			} else if (lin.isEmpty() && name == null && data.length() == 0) {
				continue;
			} else if (lin.equals("$$$$") && name == null && data.length() == 0) {
				break;
			} else {
				throw new IOException("Error parsing line: " + (reader.getLineNumber() + 1));
			}
		}

		// Accept files not terminated with $$$$.
		if (name != null) {
			values.put(new Column(name), new Cell(data.toString()));
		}

		return values;
	}

	private LineNumberReader ensureOpen() throws IOException {

		if(this.reader == null){
			throw new IOException();
		}

		return this.reader;
	}

	public void close() throws IOException {

		try {
			if(this.reader != null){
				this.reader.close();
			}
		} finally {
			this.reader = null;
		}
	}

	private static final String NEWLINE = "\n";
}
