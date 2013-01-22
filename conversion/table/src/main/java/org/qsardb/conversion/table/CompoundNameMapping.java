/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

public class CompoundNameMapping extends CompoundAttributeMapping {

	public CompoundNameMapping(){
	}

	public CompoundNameMapping(Erratum erratum){
		super(erratum);
	}

	@Override
	public void setAttribute(Compound compound, String string){
		compound.setName(string);
	}
}