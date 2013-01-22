/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

public class CompoundInChIMapping extends CompoundAttributeMapping {

	public CompoundInChIMapping(){
	}

	public CompoundInChIMapping(Erratum erratum){
		super(erratum);
	}

	@Override
	public String filter(String string){
		string = super.filter(string);

		if(string != null){

			if(string.startsWith("InChI=")){
				return string;
			} // End if

			if(string.length() > 0){
				throw new IllegalArgumentException(string);
			}
		}

		return null;
	}

	@Override
	public void setAttribute(Compound compound, String string){
		compound.setInChI(string);
	}
}