/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

public class InChIIdentifier extends Identifier {

	@Override
	public String format(Compound compound){
		return compound.getInChI();
	}
}