/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

abstract
public class CompoundAttributeMapping extends Mapping {

	public CompoundAttributeMapping(){
	}

	public CompoundAttributeMapping(Erratum erratum){
		super(erratum);
	}

	abstract
	public void setAttribute(Compound compound, String string);

	@Override
	public void mapValue(Compound compound, String string){
		string = filter(string);

		if(string == null){
			return;
		}

		setAttribute(compound, string);
	}
}