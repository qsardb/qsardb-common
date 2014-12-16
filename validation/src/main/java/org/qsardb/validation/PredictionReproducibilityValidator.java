/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.evaluation.*;
import org.qsardb.evaluation.Evaluator.Result;
import org.qsardb.model.*;

public class PredictionReproducibilityValidator extends ContainerValidator<Prediction> {

	public PredictionReproducibilityValidator(){
		super(Scope.LOCAL);
	}

	@Override
	protected Iterator<PredictionRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<PredictionRegistry>(qdb.getPredictionRegistry());
	}

	@Override
	public void validate() throws IOException {
		Prediction prediction = (Prediction)getEntity();

		Model model = prediction.getModel();
		if(isMissing(model)){
			return;
		}

		Map<String, String> values = loadValues(prediction);

		EvaluatorFactory evaluatorFactory = EvaluatorFactory.getInstance();

		try {
			Evaluator evaluator = evaluatorFactory.getEvaluator(model);

			evaluator.init();

			try {
				Map<Descriptor, Map<String, String>> descriptorValues = new LinkedHashMap<Descriptor, Map<String, String>>();

				List<Descriptor> descriptors = evaluator.getDescriptors();
				for(Descriptor descriptor : descriptors){
					descriptorValues.put(descriptor, loadValues(descriptor));
				}

				Collection<Map.Entry<String, String>> entries = values.entrySet();
				for(Map.Entry<String, String> entry : entries){
					String id = entry.getKey();

					Map<Descriptor, String> parameters = new LinkedHashMap<Descriptor, String>();

					for(Descriptor descriptor : descriptors){
						String descriptorValue = (descriptorValues.get(descriptor)).get(id);

						parameters.put(descriptor, descriptorValue);
					}

					Result result;

					try {
						result = evaluator.evaluate(parameters);
					} catch(Exception e){
						error("Compound Id \'" + id + "\' is not evaluateable", e);

						continue;
					}

					boolean equals = ValueUtil.equals(result.getValue(), entry.getValue(), 8);
					if(!equals){
						error("Compound Id \'" + id + "\' has an irreproducible value (actual " + result.getValue() + ", expected " + entry.getValue() + ")");
					}
				}
			} finally {
				evaluator.destroy();
			}
		} catch(Exception e){
			error("Evaluation failure", e);

			return;
		}
	}

	static
	private Map<String, String> loadValues(Parameter<?, ?> parameter) throws IOException {
		ValuesCargo values = parameter.getCargo(ValuesCargo.class);

		return values.loadStringMap();
	}
}