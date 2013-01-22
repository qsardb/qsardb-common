/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.csv;

import java.io.*;
import java.util.*;

import org.qsardb.conversion.spreadsheet.*;

import org.apache.commons.csv.*;

public class CsvWorkbook extends Workbook {

	private List<CSVRecord> records = null;


	public CsvWorkbook(InputStream is) throws IOException {
		this(is, CSVFormat.DEFAULT);
	}

	public CsvWorkbook(InputStream is, CSVFormat format) throws IOException {
		this.records = readRecords(is, format);
	}

	@Override
	public int getWorksheetCount(){
		return 1;
	}

	@Override
	public CsvWorksheet getWorksheet(int sheet){

		if(sheet != 0){
			throw new IllegalArgumentException();
		}

		return new CsvWorksheet(this.records);
	}

	private List<CSVRecord> readRecords(InputStream is, CSVFormat format) throws IOException {
		List<CSVRecord> result = new ArrayList<CSVRecord>();

		Reader reader = new InputStreamReader(is, "UTF-8");

		try {
			CSVParser parser = new CSVParser(reader, format);

			Iterator<CSVRecord> records = parser.iterator();

			while(records.hasNext()){
				CSVRecord record = records.next();

				result.add(record);
			}
		} finally {
			reader.close();
		}

		return result;
	}
}