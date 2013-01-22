/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.resolution.chemical;

import org.qsardb.model.*;

abstract
public class Resolver {

	private Identifier identifier = null;


	public Resolver(Identifier identifier){
		setIdentifier(identifier);
	}

	abstract
	public Object resolve(Compound compound) throws Exception;

	public Identifier getIdentifier(){
		return this.identifier;
	}

	private void setIdentifier(Identifier identifier){
		this.identifier = identifier;
	}
}