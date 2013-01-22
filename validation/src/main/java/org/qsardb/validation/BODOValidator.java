/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.cargo.bodo.*;
import org.qsardb.model.*;

import net.sf.blueobelisk.*;

import org.openscience.cdk.qsar.*;

public class BODOValidator extends CargoValidator<BODOCargo> {

	@Override
	protected Iterator<DescriptorRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<DescriptorRegistry>(qdb.getDescriptorRegistry());
	}

	@Override
	public Iterator<BODOCargo> selectEntities(Qdb qdb){
		return selectCargos(qdb, BODOCargo.class);
	}

	@Override
	public void validate(){
		BODOCargo cargo = getEntity();

		BODODescriptor bodoDescriptor;

		try {
			bodoDescriptor = cargo.loadBodoDescriptor();
		} catch(Exception e){
			error("Failed to parse YAML", e);

			return;
		}

		IDescriptor cdkDescriptor;

		try {
			cdkDescriptor = BODOUtil.parse(bodoDescriptor);
		} catch(Exception e){
			error("Unknown implementation", e);
		}
	}
}