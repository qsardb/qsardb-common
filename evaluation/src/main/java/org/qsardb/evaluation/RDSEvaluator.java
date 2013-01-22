/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.evaluation;

import java.util.*;

import org.qsardb.cargo.rds.*;
import org.qsardb.model.*;

public class RDSEvaluator extends Evaluator {

	private RDSObject object = null;


	public RDSEvaluator(Qdb qdb, RDSObject object){
		super(qdb);

		setObject(object);
	}

	@Override
	protected String loadSummary(){

		try {
			return getObject().getSummary();
		} catch(Exception e){
			return null;
		}
	}

	@Override
	protected Property loadProperty() throws Exception {
		return getProperty(getObject().getPropertyId());
	}

	@Override
	protected List<Descriptor> loadDescriptors() throws Exception {
		List<Descriptor> descriptors = new ArrayList<Descriptor>();

		List<String> ids = getObject().getDescriptorIdList();
		for(String id : ids){
			descriptors.add(getDescriptor(id));
		}

		return descriptors;
	}

	@Override
	public Result evaluate(Map<Descriptor, ?> values) throws Exception {
		List<Object> parameters = new ArrayList<Object>();

		List<Descriptor> descriptors = getDescriptors();
		for(Descriptor descriptor : descriptors){
			Object value = values.get(descriptor);
			if(value != null){
				value = parse(value);
			}

			parameters.add(value);
		}

		Object value = getObject().evaluate(parameters);

		return new Result(value, values);
	}

	@Override
	public void init() throws Exception {

		try {
			super.init();
		} catch(Exception e){
			getObject().clear();

			throw e;
		}
	}

	@Override
	public void destroy() throws Exception {

		try {
			super.destroy();

			getObject().clear();
		} finally {
			setObject(null);
		}
	}

	public RDSObject getObject(){
		return this.object;
	}

	private void setObject(RDSObject object){
		this.object = object;
	}

	static
	private Number parse(Object value){

		if(value instanceof Number){
			return (Number)value;
		}

		String string = String.valueOf(value);

		return Double.valueOf(string);
	}
}