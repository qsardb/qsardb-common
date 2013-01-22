/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.io.*;

import org.qsardb.model.*;

public class CompoundCargoMapping extends Mapping {

	private String id = null;


	public CompoundCargoMapping(String id){
		setId(id);
	}

	public CompoundCargoMapping(String id, Erratum erratum){
		super(erratum);

		setId(id);
	}

	@Override
	public void mapValue(Compound compound, String string) throws IOException {
		string = filter(string);

		if(string == null){
			return;
		}

		String id = getId();

		Cargo<Compound> cargo = compound.getOrAddCargo(id);

		// Alternatively, could be kept in memory as a StringPayload
		cargo.storeString(string);
	}

	public String getId(){
		return this.id;
	}

	private void setId(String id){
		this.id = id;
	}
}