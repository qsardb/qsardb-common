/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.sdfile;

import java.io.*;
import java.util.*;

import org.qsardb.conversion.table.*;

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
			String molfile = readMolfile();
			values.put(new Column(SDFile.COLUMN_MOLFILE), new Cell(molfile));

			Map<String, String> data = readData();
			for(Map.Entry<String, String> entry : data.entrySet()){
				values.put(new Column(entry.getKey()), new Cell(entry.getValue()));
			}
		} catch(EOFException eofe){
			return null;
		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}

		return new Row(String.valueOf(this.index++), values);
	}

	private String readMolfile() throws IOException {
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

		return sb.toString();
	}

	private Map<String, String> readData() throws IOException {
		LineNumberReader reader = ensureOpen();

		Map<String, String> values = new LinkedHashMap<String, String>();

		fields:
		for(int i = 0; true; i++){
			String line = reader.readLine();
			if(line == null){
				throw new EOFException();
			} // End if

			if(line.equals("") && i == 0){
				// Extra blank line between the end of the molfile and the beginning of the first data item
			} else

			if(line.startsWith(">")){
				int nameBegin = line.indexOf('<');
				int nameEnd = line.indexOf('>', nameBegin);

				String name = line.substring(nameBegin + 1, nameEnd);

				StringBuilder sb = new StringBuilder();

				String sep = "";

				while(true){
					line = reader.readLine();
					if(line == null){
						throw new EOFException();
					} // End if

					if(line.equals("")){
						break;
					} else

					if(line.equals("$$$$")){
						break fields;
					}

					sb.append(sep);
					sep = NEWLINE;

					sb.append(line);
				}

				values.put(name, sb.toString());
			} else

			if(line.equals("$$$$")){
				break fields;
			} else

			{
				throw new IOException("Error parsing at line " + String.valueOf(reader.getLineNumber() + 1));
			}
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