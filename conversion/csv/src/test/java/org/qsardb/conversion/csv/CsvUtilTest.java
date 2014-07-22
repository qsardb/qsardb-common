/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.csv;

import java.io.*;

import org.apache.commons.csv.*;

import org.junit.*;

import static org.junit.Assert.*;

public class CsvUtilTest {

	@Test
	public void checkFormatCsv() throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("Year,Make,Model,Length").append('\n');
		sb.append("1997,Ford,E350,2.34").append('\n');
		sb.append("2000,Mercury,Cougar,2.38");

		CSVFormat format;

		File file = createFile(sb.toString());

		try {
			format = CsvUtil.getFormat(file);
		} finally {
			file.delete();
		}

		assertEquals(',', format.getDelimiter());
		assertEquals('\"', (char)format.getQuoteCharacter());
	}

	@Test
	public void checkFormatDsv() throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("Year;Make;Model;Length").append('\n');
		sb.append("1997;Ford;E350;2,34").append('\n');
		sb.append("2000;Mercury;Cougar;2,38");

		CSVFormat format;

		File file = createFile(sb.toString());

		try {
			format = CsvUtil.getFormat(file);
		} finally {
			file.delete();
		}

		assertEquals(';', format.getDelimiter());
		assertEquals('\"', (char)format.getQuoteCharacter());
	}

	static
	private File createFile(String string) throws IOException {
		File file = File.createTempFile("test", ".csv");

		FileWriter writer = new FileWriter(file);

		try {
			writer.write(string);
		} finally {
			writer.close();
		}

		return file;
	}
}