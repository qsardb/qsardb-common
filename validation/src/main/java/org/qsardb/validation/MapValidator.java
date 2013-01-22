/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

abstract
public class MapValidator<E extends MapCargo> extends CargoValidator<E> {

	@Override
	protected Iterator<? extends ParameterRegistry<?, ?>> selectContainerRegistries(Qdb qdb){
		List<ParameterRegistry<?, ?>> result = new ArrayList<ParameterRegistry<?, ?>>();
		result.add(qdb.getPropertyRegistry());
		result.add(qdb.getDescriptorRegistry());
		result.add(qdb.getPredictionRegistry());

		return result.iterator();
	}

	@Override
	public void validate() throws IOException {
		MapCargo<?> cargo = getEntity();

		Map<String, String> map = cargo.loadStringMap();

		validateMap(map);
	}

	protected void validateMap(Map<String, String> map){
		validateKeys(map.keySet());
		validateValues(map.values());
	}

	protected void validateKeys(Set<String> keys){
		Qdb qdb = getEntity().getQdb();

		for(String key : keys){
			Compound compound = qdb.getCompound(key);

			if(isMissing(compound)){
				error("Unknown Compound Id \'" + key + "\'");
			}
		}
	}

	protected void validateValues(Collection<String> values){
	}
}