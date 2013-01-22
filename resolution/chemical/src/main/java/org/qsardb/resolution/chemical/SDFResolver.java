/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

public class SDFResolver extends Resolver {

	public SDFResolver(Identifier identifier){
		super(identifier);
	}

	@Override
	public String resolve(Compound compound) throws Exception {
		Identifier identifier = getIdentifier();

		return Service.sdf(identifier.format(compound));
	}
}