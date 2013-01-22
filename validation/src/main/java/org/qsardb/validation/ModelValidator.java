/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.model.*;

public class ModelValidator extends ContainerValidator<Model> {

	public ModelValidator(Scope scope){
		super(scope);
	}

	@Override
	protected Iterator<ModelRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<ModelRegistry>(qdb.getModelRegistry());
	}

	@Override
	public void validate(){
		Scope scope = getScope();

		if((Scope.LOCAL).equals(scope)){
			validateProperty();

			validateModelCargo();
		}
	}

	private void validateProperty(){
		Model model = getEntity();

		Property property = model.getProperty();
		if(isMissing(property)){
			error("Missing Property Id");
		}
	}

	private void validateModelCargo(){
		Model model = getEntity();

		if(!model.hasCargo(PMMLCargo.class) && !model.hasCargo(RDSCargo.class)){
			error("Missing PMML or RDS Cargo");
		}
	}
}