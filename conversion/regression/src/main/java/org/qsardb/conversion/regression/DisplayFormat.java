/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.regression;

public interface DisplayFormat {

	String formatLeftHandSide(String identifier);

	String formatRightHandSide(String identifier);
}