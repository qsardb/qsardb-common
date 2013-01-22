/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

public class SMILESResolver extends Resolver {

	public SMILESResolver(Identifier identifier){
		super(identifier);
	}

	@Override
	public String resolve(Compound compound) throws Exception {
		Identifier identifier = getIdentifier();

		return Service.smiles(identifier.format(compound));
	}
}