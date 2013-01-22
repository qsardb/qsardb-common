/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import javax.measure.unit.*;

import org.qsardb.cargo.ucum.*;
import org.qsardb.model.*;

public class UCUMValidator extends CargoValidator<UCUMCargo> {

	@Override
	protected Iterator<ParameterRegistry<?, ?>> selectContainerRegistries(Qdb qdb){
		List<ParameterRegistry<?, ?>> result = new ArrayList<ParameterRegistry<?, ?>>();
		result.add(qdb.getPropertyRegistry());
		result.add(qdb.getDescriptorRegistry());
		result.add(qdb.getPredictionRegistry());

		return result.iterator();
	}

	@Override
	public Iterator<UCUMCargo> selectEntities(Qdb qdb){
		return selectCargos(qdb, UCUMCargo.class);
	}

	@Override
	public void validate() throws IOException {
		UCUMCargo cargo = getEntity();

		Unit<?> unit;

		try {
			unit = cargo.loadUnit();
		} catch(QdbException qe){
			error("Failed to parse UCUM", qe);

			return;
		}
	}
}