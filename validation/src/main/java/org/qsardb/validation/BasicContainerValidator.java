/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import org.qsardb.model.*;

public class BasicContainerValidator extends ContainerValidator {

	public BasicContainerValidator(){
		super(Scope.LOCAL);
	}

	@Override
	public void validate(){
		Scope scope = getScope();

		if((Scope.LOCAL).equals(scope)){
			validateId();
			validateName();
		}
	}

	private void validateId(){
		Container<?, ?> container = (Container<?, ?>)getEntity();

		String id = container.getId();
		if(!QdbUtil.validateId(id)){
			error("Invalid Id \'" + id + "\'");
		}
	}

	private void validateName(){
		Container<?, ?> container = (Container<?, ?>)getEntity();

		String name = container.getName();
		if(isMissing(name)){
			error("Missing Name");
		}
	}
}