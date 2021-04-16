/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CsvUtilTest {

	@Test
	public void checkFormatCsv() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("Year,Make,Model,Length").append('\n');
		sb.append("1997,Ford,E350,2.34").append('\n');
		sb.append("2000,Mercury,Cougar,2.38");

		CSVFormat format = getFormat(sb.toString());

		assertEquals(',', format.getDelimiter());
		assertEquals('\"', (char)format.getQuoteCharacter());
	}

	@Test
	public void checkFormatDsv() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("Year;Make;Model;Length").append('\n');
		sb.append("1997;Ford;E350;2,34").append('\n');
		sb.append("2000;Mercury;Cougar;2,38");

		CSVFormat format = getFormat(sb.toString());

		assertEquals(';', format.getDelimiter());
		assertEquals('\"', (char)format.getQuoteCharacter());
	}

	@Test
	public void checkFormatTsv() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("H1\tH2\tH3\n");
		sb.append("a,b\tc;\t1.0\n");
		sb.append("a'b\t,c\t2.0\n");
		sb.append("\n");

		CSVFormat format = getFormat(sb.toString());

		assertEquals('\t', format.getDelimiter());
		assertEquals('\"', (char)format.getQuoteCharacter());
	}

	private static CSVFormat getFormat(String string) throws IOException {
		File file = File.createTempFile("test", ".csv");
		file.deleteOnExit();

		FileWriter writer = new FileWriter(file);
		try {
			writer.write(string);
		} finally {
			writer.close();
		}

		return CsvUtil.getFormat(file);
	}
}