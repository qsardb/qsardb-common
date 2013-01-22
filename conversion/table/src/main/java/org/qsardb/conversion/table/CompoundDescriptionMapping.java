/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

public class CompoundDescriptionMapping extends CompoundAttributeMapping {

	public CompoundDescriptionMapping(){
	}

	public CompoundDescriptionMapping(Erratum erratum){
		super(erratum);
	}

	@Override
	public void setAttribute(Compound compound, String string){
		compound.setDescription(string);
	}
}