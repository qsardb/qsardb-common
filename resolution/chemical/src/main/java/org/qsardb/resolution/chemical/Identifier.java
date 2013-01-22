/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

abstract
public class Identifier {

	abstract
	public String format(Compound compound);
}