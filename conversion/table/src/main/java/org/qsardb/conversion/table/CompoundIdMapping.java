/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

public class CompoundIdMapping extends CompoundAttributeMapping {

	public CompoundIdMapping(){
	}

	public CompoundIdMapping(Erratum erratum){
		super(erratum);
	}

	@Override
	public void setAttribute(Compound compound, String string){
		compound.setId(string);
	}

	@Override
	final
	public void mapValue(Compound compound, String string){
		throw new UnsupportedOperationException();
	}
}