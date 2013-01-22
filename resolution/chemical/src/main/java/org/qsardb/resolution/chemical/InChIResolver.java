/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

public class InChIResolver extends Resolver {

	public InChIResolver(Identifier identifier){
		super(identifier);
	}

	@Override
	public String resolve(Compound compound) throws Exception {
		Identifier identifier = getIdentifier();

		return Service.stdinchi(identifier.format(compound));
	}
}