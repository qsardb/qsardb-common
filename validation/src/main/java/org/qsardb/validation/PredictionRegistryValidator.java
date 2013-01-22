/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.model.*;

public class PredictionRegistryValidator extends ContainerRegistryValidator<PredictionRegistry> {

	@Override
	protected Iterator<PredictionRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<PredictionRegistry>(qdb.getPredictionRegistry());
	}

	@Override
	public void validate(){
		PredictionRegistry predictions = getEntity();

		Set<Model> models = new LinkedHashSet<Model>();

		for(Prediction prediction : predictions){
			Model model = prediction.getModel();

			if(model != null){
				models.add(model);
			}
		}

		for(Model model : models){
			Collection<Prediction> trainingPredictions = predictions.getByModelAndType(model, Prediction.Type.TRAINING);
			Collection<Prediction> validationPredictions = predictions.getByModelAndType(model, Prediction.Type.VALIDATION);
			Collection<Prediction> testingPredictions = predictions.getByModelAndType(model, Prediction.Type.TESTING);

			if(trainingPredictions.size() < 1){
				error("Model Id \'" + model.getId() + "\' is missing a training Prediction");
			} else

			if(trainingPredictions.size() > 1){
				error("Model Id \'" + model.getId() + "\' has too many training Predictions");
			}
		}
	}
}