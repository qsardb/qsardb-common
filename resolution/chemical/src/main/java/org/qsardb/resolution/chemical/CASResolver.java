/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import java.util.*;

import org.qsardb.model.*;

public class CASResolver extends Resolver {

	public CASResolver(Identifier identifier){
		super(identifier);
	}

	@Override
	public List<String> resolve(Compound compound) throws Exception {
		Identifier identifier = getIdentifier();

		return Service.cas(identifier.format(compound));
	}
}