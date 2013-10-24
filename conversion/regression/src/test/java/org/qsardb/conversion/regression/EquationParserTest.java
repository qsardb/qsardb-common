/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.regression;

import org.junit.*;

public class EquationParserTest {

	private EquationParser parser = null;


	@Test
	public void parseEquation() throws ParseException {
		this.parser.parseEquation("x = 1y + 0");
		this.parser.parseEquation("x = 1*(y) + 0");

		this.parser.parseEquation("x = 1.0y + 0.0");
		this.parser.parseEquation("x = 1.0*(y) + 0.0");

		this.parser.parseEquation("x = 1.0(0.00)y + 0.0(0.00)");
		this.parser.parseEquation("x = 1.0(\u00b10.00)*(y) + 0.0(\u00b10.00)");

		this.parser.parseEquation("x_exp = 1y + 0");
	}

	@Before
	public void initializeParser(){
		this.parser = new EquationParser();
	}

	@After
	public void nullifyParser(){
		this.parser = null;
	}
}