/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CsvUtil {

	private CsvUtil(){
	}

	public static CSVFormat getFormat(File file) throws IOException {
		char[] delimiters = {',', '\t', ';'};
		char[] encapsulators = {'\"', '\''};

		for(char delimiter : delimiters){
			for(char encapsulator : encapsulators){
				CSVFormat format = CSVFormat.newFormat(delimiter).withQuote(encapsulator);
				if(checkFormat(file, format)){
					return format;
				}
			}
		}

		throw new IOException("Unknown CSV format");
	}

	public static boolean checkFormat(File file, CSVFormat format) throws IOException {
		InputStream is = new FileInputStream(file);

		try {
			return checkFormat(is, format);
		} finally {
			is.close();
		}
	}

	public static boolean checkFormat(InputStream is, CSVFormat format) throws IOException {
		Reader reader = new InputStreamReader(is, "UTF-8");

		try {
			int guessColumns = 0;

			CSVParser parser = new CSVParser(reader, format);
			Iterator<CSVRecord> records = parser.iterator();
			for (int i=0; records.hasNext() && i < 100; i++) {
				CSVRecord record = records.next();
				int columns = record.size();

				if (columns == 1 && record.get(0).trim().isEmpty()) {
					// skip empty lines
				} else if (guessColumns == 0) {
					guessColumns = columns;
				} else if (columns != guessColumns) {
					return false;
				}
			}

			return (guessColumns > 1);
		} finally {
			reader.close();
		}
	}
}
