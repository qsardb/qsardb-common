/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

public class VerificationException extends Exception {

	private Object expected = null;

	private Object actual = null;


	public VerificationException(Object expected, Object actual){
		super("Expected value " + expected + ", actual value " + actual);

		setExpected(expected);
		setActual(actual);
	}

	public Object getExpected(){
		return this.expected;
	}

	private void setExpected(Object expected){
		this.expected = expected;
	}

	public Object getActual(){
		return this.actual;
	}

	private void setActual(Object actual){
		this.actual = actual;
	}
}