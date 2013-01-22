/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

public class PredictionValidator extends ContainerValidator<Prediction> {

	public PredictionValidator(Scope scope){
		super(scope);
	}

	@Override
	protected Iterator<PredictionRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<PredictionRegistry>(qdb.getPredictionRegistry());
	}

	@Override
	public void validate() throws IOException {
		Scope scope = getScope();

		if((Scope.LOCAL).equals(scope)){
			validateModel();
			validateType();

			validateValuesCargo();
		} else

		if((Scope.GLOBAL).equals(scope)){
			validateApplication();
		}
	}

	private void validateModel(){
		Prediction prediction = getEntity();

		Model model = prediction.getModel();
		if(isMissing(model)){
			error("Missing Model Id");
		}
	}

	private void validateType() throws IOException {
		Prediction prediction = getEntity();
		Set<String> predictionKeys = loadKeys(prediction);

		Prediction.Type type = prediction.getType();
		if(isMissing(type)){
			error("Missing Type");

			return;
		}

		Model model = prediction.getModel();
		if(isMissing(model)){
			return;
		}

		Property property = model.getProperty();
		Set<String> propertyKeys = loadKeys(property);

		switch(type){
			case TRAINING:
			case VALIDATION:
				boolean joint = (propertyKeys).containsAll(predictionKeys);
				if(!joint){
					error("Predicted values are disjoint from experimental values");
				}
				break;
			case TESTING:
				boolean disjoint = Collections.disjoint(propertyKeys, predictionKeys);
				if(!disjoint){
					error("Predicted values are not disjoint from experimental values. Consider changing the Type to " + Prediction.Type.VALIDATION);
				}
				break;
			default:
				break;
		}
	}

	private void validateApplication(){
		Prediction prediction = getEntity();

		String application = prediction.getApplication();
		if(isMissing(application)){
			warning("Missing Application");
		}
	}

	private void validateValuesCargo(){
		Prediction prediction = getEntity();

		if(!prediction.hasCargo(ValuesCargo.class)){
			error("Missing Values Cargo");
		}
	}

	static
	private Set<String> loadKeys(Parameter<?, ?> parameter) throws IOException {
		ValuesCargo cargo = parameter.getCargo(ValuesCargo.class);

		Map<String, String> map = cargo.loadStringMap();

		return map.keySet();
	}
}