/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.evaluation;

import java.text.*;
import java.util.*;

import org.qsardb.cargo.pmml.*;
import org.qsardb.conversion.regression.*;
import org.qsardb.model.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class PMMLEvaluator extends Evaluator {

	private ModelManager<?> modelManager = null;


	public PMMLEvaluator(Qdb qdb, PMML pmml){
		super(qdb);

		PMMLManager pmmlManager = new PMMLManager(pmml);

		setModelManager(pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance()));
	}

	@Override
	protected String loadSummary(){
		ModelManager<?> modelManager = getModelManager();

		return modelManager.getSummary();
	}

	@Override
	protected Property loadProperty(){
		List<FieldName> fields = getModelManager().getPredictedFields();
		if(fields.size() != 1){
			throw new IllegalArgumentException();
		}

		FieldName field = fields.get(0);

		return getProperty(FieldNameUtil.decodePropertyId(field));
	}

	@Override
	protected List<Descriptor> loadDescriptors(){
		List<Descriptor> descriptors = new ArrayList<Descriptor>();

		List<FieldName> fields = getModelManager().getActiveFields();
		for(FieldName field : fields){
			descriptors.add(getDescriptor(FieldNameUtil.decodeDescriptorId(field)));
		}

		return descriptors;
	}

	@Override
	public Result evaluate(Map<Descriptor, ?> values) throws Exception {
		ModelManager<?> modelManager = getModelManager();

		Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

		Map<FieldName, DataField> dataFieldMap = new LinkedHashMap<FieldName, DataField>();

		List<Descriptor> descriptors = getDescriptors();
		for(Descriptor descriptor : descriptors){
			FieldName field = FieldNameUtil.encodeDescriptor(descriptor);

			DataField dataField = dataFieldMap.get(field);
			if(dataField == null){
				dataField = modelManager.getDataField(field);

				// For compatibility with generic PMML producer software
				if(dataField == null){
					field = new FieldName(descriptor.getId());

					dataField = modelManager.getDataField(field);
				} // End if

				if(dataField == null){
					throw new IllegalArgumentException();
				}

				dataFieldMap.put(field, dataField);
			}

			Object value = values.get(descriptor);
			if(value != null){
				value = ParameterUtil.parse(dataField, String.valueOf(value));
			}

			parameters.put(field, value);
		}

		Object value = ((org.jpmml.evaluator.Evaluator)modelManager).evaluate(parameters);

		return new Result(value, values);
	}

	@Override
	public Object evaluateAndFormat(Map<Descriptor, ?> values, DecimalFormat format) throws Exception {
		ModelManager<?> modelManager = getModelManager();

		if(modelManager instanceof RegressionModelManager){
			Equation equation = RegressionUtil.format(getQdb(), (RegressionModelManager)modelManager);

			Result result = evaluate(values);

			return super.formatRegressionResult(equation, result, format);
		}
		else if(modelManager instanceof NeuralNetworkManager){
			Result result = evaluate(values);

			// if the ANN model has only one output parameter, then extract its value from the map
			Map map = (Map)result.getValue();
			if(map.size() == 1){
				Object value = map.values().toArray()[0];
				result = new Result(value, result.getParameters());
			}

			return super.formatResult(result, format);
		}

		return super.evaluateAndFormat(values, format);
	}

	@Override
	public void destroy() throws Exception {

		try {
			super.destroy();
		} finally {
			setModelManager(null);
		}
	}

	public ModelManager<?> getModelManager(){
		return this.modelManager;
	}

	private void setModelManager(ModelManager<?> modelManager){
		this.modelManager = modelManager;
	}
}