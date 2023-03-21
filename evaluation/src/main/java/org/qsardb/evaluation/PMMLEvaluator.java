/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.evaluation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Segmentation;
import org.jpmml.evaluator.DefaultClassificationMap;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.MiningModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.NeuralNetworkEvaluator;
import org.jpmml.evaluator.RegressionModelEvaluator;
import org.jpmml.manager.ModelManager;
import org.jpmml.manager.PMMLManager;
import org.qsardb.cargo.pmml.FieldNameUtil;
import org.qsardb.conversion.regression.DisplayFormat;
import org.qsardb.conversion.regression.Equation;
import org.qsardb.conversion.regression.EquationFormatter;
import org.qsardb.conversion.regression.QdbDisplayFormat;
import org.qsardb.conversion.regression.RegressionUtil;
import org.qsardb.model.Descriptor;
import org.qsardb.model.Property;
import org.qsardb.model.Qdb;

public class PMMLEvaluator extends Evaluator {

	private ModelManager<?> modelManager = null;

	public PMMLEvaluator(Qdb qdb, PMML pmml) {
		super(qdb);

		PMMLManager pmmlManager = new PMMLManager(pmml);

		setModelManager(pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance()));
	}

	@Override
	protected String loadSummary() {
		ModelManager<?> modelManager = getModelManager();

		return modelManager.getSummary();
	}

	@Override
	protected Property loadProperty() {
		List<FieldName> fields = getModelManager().getPredictedFields();
		if (fields.size() != 1) {
			throw new IllegalArgumentException();
		}

		FieldName field = fields.get(0);

		return getProperty(FieldNameUtil.decodePropertyId(field));
	}

	@Override
	protected List<Descriptor> loadDescriptors() {
		List<Descriptor> descriptors = new ArrayList<Descriptor>();

		List<FieldName> fields = getModelManager().getActiveFields();
		for (FieldName field : fields) {
			descriptors.add(getDescriptor(FieldNameUtil.decodeDescriptorId(field)));
		}

		return descriptors;
	}

	@Override
	public Result evaluate(Map<Descriptor, ?> values) throws Exception {
		org.jpmml.evaluator.Evaluator evaluator
				= (org.jpmml.evaluator.Evaluator) getModelManager();

		Map<FieldName, org.jpmml.evaluator.FieldValue> arguments = new LinkedHashMap<FieldName, org.jpmml.evaluator.FieldValue>();

		Map<FieldName, DataField> dataFieldMap = new LinkedHashMap<FieldName, DataField>();

		List<Descriptor> descriptors = getDescriptors();
		for (Descriptor descriptor : descriptors) {
			FieldName field = FieldNameUtil.encodeDescriptor(descriptor);

			DataField dataField = dataFieldMap.get(field);
			if (dataField == null) {
				dataField = evaluator.getDataField(field);

				// For compatibility with generic PMML producer software
				if (dataField == null) {
					field = new FieldName(descriptor.getId());

					dataField = evaluator.getDataField(field);
				} // End if

				if (dataField == null) {
					throw new IllegalArgumentException();
				}

				dataFieldMap.put(field, dataField);
			}

			Object value = values.get(descriptor);
			org.jpmml.evaluator.FieldValue fieldValue = EvaluatorUtil.prepare(evaluator, field, value);
			arguments.put(field, fieldValue);
		}

		Map<FieldName, ?> jpmmlResult = evaluator.evaluate(arguments);

		Object targetValue = jpmmlResult.get(evaluator.getTargetField());
		Result result = new Result(EvaluatorUtil.decode(targetValue), values);

		// probability for classification with ensemble models (e.g. RF)
		if (evaluator instanceof MiningModelEvaluator) {
			MiningModelEvaluator mme = (MiningModelEvaluator) evaluator;
			Segmentation segmentation = mme.getModel().getSegmentation();
			if (segmentation.getMultipleModelMethod() == MultipleModelMethodType.MAJORITY_VOTE
					&& targetValue instanceof DefaultClassificationMap) {
				String c = (String) result.getValue();
				Double p = ((DefaultClassificationMap)targetValue).getProbability(c);
				result.setProbability(p);
			}
		}
		return result;
	}

	@Override
	public Object evaluateAndFormat(Map<Descriptor, ?> values, DecimalFormat format) throws Exception {
		Result result = evaluate(values);

		ModelManager<?> modelManager = getModelManager();
		if (modelManager instanceof RegressionModelEvaluator) {
			try {
				RegressionModelEvaluator evaluator = (RegressionModelEvaluator) modelManager;
				Equation equation = RegressionUtil.format(getQdb(), evaluator.getPMML());
				return formatRegressionResult(equation, result, format);
			} catch (IllegalArgumentException ex) {
				return super.formatResult(result, format);
			}
		} else if (modelManager instanceof NeuralNetworkEvaluator) {
			// if the ANN model has only one output parameter, then extract its value from the map
			if (result.getValue() instanceof Map) {
				Map map = (Map) result.getValue();
				if (map.size() == 1) {
					Object value = map.values().iterator().next();
					result = new Result(value, result.getParameters());
				}
			}

			return super.formatResult(result, format);
		}

		return super.formatResult(result, format);
	}

	private String formatRegressionResult(Equation equation, final Result result, DecimalFormat format) {
		StringBuilder sb = new StringBuilder();

		EquationFormatter formatter = new EquationFormatter();

		Property property = getProperty();

		DisplayFormat nameFormat = new QdbDisplayFormat(getQdb());
		sb.append(formatter.formatEquation(equation, nameFormat));

		DisplayFormat valueFormat = new DisplayFormat() {

			@Override
			public String formatLeftHandSide(String identifier) {
				return null;
			}

			@Override
			public String formatRightHandSide(String identifier) {
				Descriptor descriptor = getQdb().getDescriptor(identifier);

				Object value = (result.getParameters()).get(descriptor);

				return format(value, getFormat(descriptor));
			}
		};
		sb.append(' ').append('=').append(' ');
		sb.append(formatter.formatRightHandSide(equation.getTerms(), valueFormat));

		if (format == null) {
			format = getFormat(property);
		}

		sb.append(' ').append('=').append(' ');
		sb.append(format(result.getValue(), format));

		return sb.toString();
	}

	@Override
	public void destroy() throws Exception {

		try {
			super.destroy();
		} finally {
			setModelManager(null);
		}
	}

	public ModelManager<?> getModelManager() {
		return this.modelManager;
	}

	private void setModelManager(ModelManager<?> modelManager) {
		this.modelManager = modelManager;
	}
}
