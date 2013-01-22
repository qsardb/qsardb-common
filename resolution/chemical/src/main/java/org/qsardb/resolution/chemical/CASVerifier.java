/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import java.util.*;

import org.qsardb.model.*;

public class CASVerifier extends CASResolver {

	public CASVerifier(Identifier identifier){
		super(identifier);
	}

	@Override
	public List<String> resolve(Compound compound) throws Exception {
		List<String> casrns = super.resolve(compound);

		if(compound.getCas() != null && !casrns.contains(compound.getCas())){
			throw new VerificationException(compound.getCas(), casrns);
		}

		return casrns;
	}
}