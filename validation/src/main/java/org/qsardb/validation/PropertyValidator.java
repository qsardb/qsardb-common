/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.ucum.*;
import org.qsardb.model.*;

public class PropertyValidator extends ContainerValidator<Property> {

	public PropertyValidator(Scope scope){
		super(scope);
	}

	@Override
	protected Iterator<PropertyRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<PropertyRegistry>(qdb.getPropertyRegistry());
	}

	@Override
	public void validate(){
		Scope scope = getScope();

		if((Scope.LOCAL).equals(scope)){
			validateValuesCargo();
		} else

		if((Scope.GLOBAL).equals(scope)){
			validateEndpoint();

			validateUCUMCargo();
		}
	}

	private void validateEndpoint(){
		Property property = getEntity();

		String endpoint = property.getEndpoint();
		if(isMissing(endpoint)){
			error("Missing Endpoint");
		}
	}

	private void validateValuesCargo(){
		Property property = getEntity();

		if(!property.hasCargo(ValuesCargo.class)){
			error("Missing Values Cargo");
		}
	}

	private void validateUCUMCargo(){
		Property property = getEntity();

		if(!property.hasCargo(UCUMCargo.class)){
			error("Missing UCUM Cargo");
		}
	}
}