/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.csv;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.*;
import org.apache.commons.csv.CSVFormat;

public class CsvUtil {

	private CsvUtil(){
	}

	static
	public CSVFormat getFormat(File file) throws IOException {
		char[] delimiters = {',', ';', '\t'};
		char[] encapsulators = {'\"', '\''};

		for(char delimiter : delimiters){

			for(char encapsulator : encapsulators){
				CSVFormat format = CSVFormat.newFormat(delimiter).withQuoteChar(encapsulator);

				if(checkFormat(file, format)){
					return format;
				}
			}
		}

		throw new IOException("Unknown CSV format");
	}

	static
	public boolean checkFormat(File file, CSVFormat format) throws IOException {
		InputStream is = new FileInputStream(file);

		try {
			return checkFormat(is, format);
		} finally {
			is.close();
		}
	}

	static
	public boolean checkFormat(InputStream is, CSVFormat format) throws IOException {
		Reader reader = new InputStreamReader(is, "UTF-8");

		try {
			CSVParser parser = new CSVParser(reader, format);

			int count = 0;

			Iterator<CSVRecord> records = parser.iterator();

			for(int i = 0; records.hasNext() && i < 100; i++){
				CSVRecord record = records.next();

				if(count == 0 || count == record.size()){
					count = record.size();
				} else

				{
					count = -1;

					break;
				}
			}

			return (count > 1);
		} finally {
			reader.close();
		}
	}
}
