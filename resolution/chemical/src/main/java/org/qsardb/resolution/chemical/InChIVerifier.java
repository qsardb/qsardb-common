/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

public class InChIVerifier extends InChIResolver {

	public InChIVerifier(Identifier identifier){
		super(identifier);
	}

	@Override
	public String resolve(Compound compound) throws Exception {
		String inChI = super.resolve(compound);

		if(compound.getInChI() != null && !equals(inChI, compound.getInChI())){
			throw new VerificationException(compound.getInChI(), inChI);
		}

		return inChI;
	}

	static
	private boolean equals(String left, String right){
		left = stripPrefix(left);
		right = stripPrefix(right);

		int length = Math.min(left.length(), right.length());

		return (left.subSequence(0, length)).equals(right.subSequence(0, length));
	}

	static
	private String stripPrefix(String inChI){
		int slash = inChI.indexOf('/');

		return inChI.substring(slash + 1);
	}
}