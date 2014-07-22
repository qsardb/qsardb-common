/*
 * Copyright (c) 2013 University of Tartu
 */
package org.qsardb.conversion.csv;

import java.io.*;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.qsardb.conversion.table.*;

public class CsvExporter extends TableExporter {
	private final OutputStream os;
	private CSVPrinter output;

	public CsvExporter(OutputStream os) {
		this.os = os;
	}

	@Override
	public void write() throws Exception {
		output = new CSVPrinter(new OutputStreamWriter(os), CSVFormat.EXCEL);

		ArrayList<String> header = new ArrayList<String>();
		for (Iterator<Column> it=columns(); it.hasNext();) {
			header.add(it.next().getId());
		}
		output.printRecord(header);

		for (Iterator<Row> it=rows(); it.hasNext();) {
			ArrayList<String> cellValues = new ArrayList<String>();
			for (Cell v: it.next().getValues().values()) {
				cellValues.add(v.getText());
			}
			output.printRecord(cellValues);
		}
	}

	@Override
	public void close() throws IOException {
		if (output != null) {
			output.close();
		}
	}

}