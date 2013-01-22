/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.spreadsheet;

import org.junit.*;

import static org.junit.Assert.*;

public class WorksheetTest {

	@Test
	public void formatColumnId(){
		assertEquals("A", Worksheet.formatColumnId(COLUMN_A));
		assertEquals("Z", Worksheet.formatColumnId(COLUMN_Z));

		assertEquals("AA", Worksheet.formatColumnId(COLUMN_AA));
		assertEquals("AZ", Worksheet.formatColumnId(COLUMN_AZ));
		assertEquals("ZA", Worksheet.formatColumnId(COLUMN_ZA));
		assertEquals("ZZ", Worksheet.formatColumnId(COLUMN_ZZ));

		assertEquals("AAA", Worksheet.formatColumnId(COLUMN_AAA));
		assertEquals("AAZ", Worksheet.formatColumnId(COLUMN_AAZ));
		assertEquals("AZA", Worksheet.formatColumnId(COLUMN_AZA));
		assertEquals("AZZ", Worksheet.formatColumnId(COLUMN_AZZ));
		assertEquals("ZAA", Worksheet.formatColumnId(COLUMN_ZAA));
		assertEquals("ZZA", Worksheet.formatColumnId(COLUMN_ZZA));
		assertEquals("ZZZ", Worksheet.formatColumnId(COLUMN_ZZZ));
	}

	@Test
	public void parseColumnId(){
		assertEquals(COLUMN_A, Worksheet.parseColumnId("A"));
		assertEquals(COLUMN_Z, Worksheet.parseColumnId("Z"));

		assertEquals(COLUMN_AA, Worksheet.parseColumnId("AA"));
		assertEquals(COLUMN_AZ, Worksheet.parseColumnId("AZ"));
		assertEquals(COLUMN_ZA, Worksheet.parseColumnId("ZA"));
		assertEquals(COLUMN_ZZ, Worksheet.parseColumnId("ZZ"));

		assertEquals(COLUMN_AAA, Worksheet.parseColumnId("AAA"));
		assertEquals(COLUMN_AAZ, Worksheet.parseColumnId("AAZ"));
		assertEquals(COLUMN_AZA, Worksheet.parseColumnId("AZA"));
		assertEquals(COLUMN_AZZ, Worksheet.parseColumnId("AZZ"));
		assertEquals(COLUMN_ZAA, Worksheet.parseColumnId("ZAA"));
		assertEquals(COLUMN_ZAZ, Worksheet.parseColumnId("ZAZ"));
		assertEquals(COLUMN_ZZA, Worksheet.parseColumnId("ZZA"));
		assertEquals(COLUMN_ZZZ, Worksheet.parseColumnId("ZZZ"));
	}

	@Test
	public void formatRowId(){
		assertEquals("1", Worksheet.formatRowId(0));
		assertEquals("11", Worksheet.formatRowId(10));
		assertEquals("101", Worksheet.formatRowId(100));
	}

	@Test
	public void parseRowId(){
		assertEquals(0, Worksheet.parseRowId("1"));
		assertEquals(10, Worksheet.parseRowId("11"));
		assertEquals(100, Worksheet.parseRowId("101"));
	}

	private static final int length = Worksheet.ALPHABET.length();

	private static final int COLUMN_A = 0;
	private static final int COLUMN_Z = (length - 1);
	private static final int COLUMN_A2 = (1 + COLUMN_A) * length;
	private static final int COLUMN_AA = COLUMN_A2 + COLUMN_A;
	private static final int COLUMN_AZ = COLUMN_A2 + COLUMN_Z;
	private static final int COLUMN_Z2 = (1 + COLUMN_Z) * length;
	private static final int COLUMN_ZA = COLUMN_Z2 + COLUMN_A;
	private static final int COLUMN_ZZ = COLUMN_Z2 + COLUMN_Z;
	private static final int COLUMN_A3 = (1 + COLUMN_A) * length * length;
	private static final int COLUMN_AAA = COLUMN_A3 + COLUMN_AA;
	private static final int COLUMN_AAZ = COLUMN_A3 + COLUMN_AZ;
	private static final int COLUMN_AZA = COLUMN_A3 + COLUMN_ZA;
	private static final int COLUMN_AZZ = COLUMN_A3 + COLUMN_ZZ;
	private static final int COLUMN_Z3 = (1 + COLUMN_Z) * length * length;
	private static final int COLUMN_ZAA = COLUMN_Z3 + COLUMN_AA;
	private static final int COLUMN_ZAZ = COLUMN_Z3 + COLUMN_AZ;
	private static final int COLUMN_ZZA = COLUMN_Z3 + COLUMN_ZA;
	private static final int COLUMN_ZZZ = COLUMN_Z3 + COLUMN_ZZ;
}