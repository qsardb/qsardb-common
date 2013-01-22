/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.cargo.bodo.*;
import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

public class DescriptorValidator extends ContainerValidator<Descriptor> {

	public DescriptorValidator(Scope scope){
		super(scope);
	}

	@Override
	protected Iterator<DescriptorRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<DescriptorRegistry>(qdb.getDescriptorRegistry());
	}

	@Override
	public void validate(){
		Scope scope = getScope();

		if((Scope.LOCAL).equals(scope)){
			validateValuesCargo();
		} else

		if((Scope.GLOBAL).equals(scope)){
			validateApplication();

			validateBODOCargo();
		}
	}

	private void validateApplication(){
		Descriptor descriptor = getEntity();

		String application = descriptor.getApplication();
		if(isMissing(application)){
			warning("Missing Application");
		}
	}

	private void validateValuesCargo(){
		Descriptor descriptor = getEntity();

		if(!descriptor.hasCargo(ValuesCargo.class)){
			error("Missing Values Cargo");
		}
	}

	private void validateBODOCargo(){
		Descriptor descriptor = getEntity();

		if(!descriptor.hasCargo(BODOCargo.class)){
			warning("Missing BODO Cargo");
		}
	}
}