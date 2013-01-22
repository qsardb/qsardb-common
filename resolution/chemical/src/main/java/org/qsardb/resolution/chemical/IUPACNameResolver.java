/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

public class IUPACNameResolver extends Resolver {

	public IUPACNameResolver(Identifier identifier){
		super(identifier);
	}

	@Override
	public String resolve(Compound compound) throws Exception {
		Identifier identifier = getIdentifier();

		return Service.iupac_name(identifier.format(compound));
	}
}