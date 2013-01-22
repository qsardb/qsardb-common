/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

abstract
public class ParameterReferencesMapping<R extends ParameterRegistry<R, C>, C extends Parameter<R, C>> extends Mapping {

	private C parameter = null;

	transient
	private Map<String, String> references = null;


	public ParameterReferencesMapping(C parameter){
		setParameter(parameter);
	}

	@Override
	public void beginMapping() throws IOException {
		C parameter = getParameter();

		this.references = new LinkedHashMap<String, String>();

		if(parameter.hasCargo(ReferencesCargo.class)){
			ReferencesCargo cargo = parameter.getCargo(ReferencesCargo.class);

			this.references.putAll(cargo.loadReferences());
		}
	}

	@Override
	public void mapValue(Compound compound, String string){
		string = filter(string);

		if(string == null){
			return;
		}

		this.references.put(compound.getId(), string);
	}

	@Override
	public void endMapping() throws IOException {
		C parameter = getParameter();

		ReferencesCargo cargo = parameter.getOrAddCargo(ReferencesCargo.class);

		cargo.storeReferences(this.references);

		this.references.clear();
		this.references = null;
	}

	public C getParameter(){
		return this.parameter;
	}

	private void setParameter(C parameter){
		this.parameter = parameter;
	}
}