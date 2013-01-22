/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.model.*;

abstract
public class ContainerValidator<E extends Container> extends Validator<E> {

	private Scope scope = null;


	public ContainerValidator(Scope scope){
		setScope(scope);
	}

	@Override
	public Iterator<E> selectEntities(Qdb qdb){
		return (Iterator<E>)selectContainers(qdb);
	}

	public Scope getScope(){
		return this.scope;
	}

	private void setScope(Scope scope){
		this.scope = scope;
	}
}