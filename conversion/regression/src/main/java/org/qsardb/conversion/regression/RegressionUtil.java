/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.regression;

import java.util.*;
import org.dmg.pmml.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.model.*;
import org.qsardb.model.Parameter;

public class RegressionUtil {

	private RegressionUtil(){
	}

	static
	public PMML parse(Qdb qdb, String string) throws ParseException {
		EquationParser parser = new EquationParser();
		Equation equation = parser.parseEquation(string);

		return parse(qdb, equation);
	}

	static
	public PMML parse(Qdb qdb, Equation equation){
		DataDictionary dict = new DataDictionary();
		MiningSchema schema = new MiningSchema();

		RegressionModel model = new RegressionModel(schema, MiningFunctionType.REGRESSION);

		Property property = qdb.getProperty(equation.getIdentifier());
		if(property == null){
			throw new IllegalArgumentException("Property \'" + equation.getIdentifier() + "\' not found");
		}

		addDataField(dict, property);
		addMiningField(schema, property);
		model.setTargetFieldName(getFieldName(property));

		RegressionTable regressionTable = new RegressionTable(Double.NaN);
		model.getRegressionTables().add(regressionTable);

		List<Equation.Term> terms = equation.getTerms();
		for(Equation.Term term : terms){
			Double coefficient = Double.valueOf(term.getCoefficient());

			if(term.isIntercept()){
				regressionTable.setIntercept(coefficient);
			} else

			{
				Descriptor descriptor = qdb.getDescriptor(term.getIdentifier());
				if(descriptor == null){
					throw new IllegalArgumentException("Descriptor \'" + term.getIdentifier() + "\' not found");
				}

				addDataField(dict, descriptor);
				addMiningField(schema, descriptor);
				NumericPredictor predictor = new NumericPredictor(getFieldName(descriptor), coefficient);
				regressionTable.getNumericPredictors().add(predictor);
			}
		}

		return new PMML(null, dict, "4.1").withModels(model);
	}

	private static void addDataField(DataDictionary dict, Parameter param) { // XXX
		FieldName name = getFieldName(param);
		DataField df = new DataField(name, OpType.CONTINUOUS, DataType.DOUBLE);
		df.setDisplayName(param.getName());
		dict.getDataFields().add(df);
	}

	private static void addMiningField(MiningSchema schema, Parameter param) { // XXX
		MiningField mf = new MiningField(getFieldName(param));
		if (param instanceof Property) {
			mf.setUsageType(FieldUsageType.PREDICTED);
		} else {
			mf.setUsageType(FieldUsageType.ACTIVE);
		}
		schema.getMiningFields().add(mf);
	}

	private static FieldName getFieldName(Parameter param) throws IllegalArgumentException { // XXX
		if (param instanceof Property) {
			return FieldNameUtil.encodeProperty((Property) param); // XXX
		} else if (param instanceof Descriptor) {
			return FieldNameUtil.encodeDescriptor((Descriptor) param); // XXX
		} else {
			throw new IllegalArgumentException();
		}
	}

	static
	public Equation format(Qdb qdb, RegressionModel model){
		Equation equation = new Equation();

		FieldName propertyName = getPropertyName(model);

		Property property = FieldNameUtil.decodeProperty(qdb, propertyName);
		if(property == null){
			throw new IllegalArgumentException("Property \'" + propertyName.getValue() + "\' not found");
		}

		equation.setIdentifier(property.getId());

		List<Equation.Term> terms = new ArrayList<Equation.Term>();
		RegressionTable regressionTable = model.getRegressionTables().get(0);
		List<NumericPredictor> numericPredictors = regressionTable.getNumericPredictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			Equation.Term term = new Equation.Term();

			Double coefficient = numericPredictor.getCoefficient();
			term.setCoefficient(coefficient.toString());

			int exponent = numericPredictor.getExponent();
			term.setExponent(Integer.toString(exponent));

			FieldName descriptorName = numericPredictor.getName();
			FieldName normDescName = resolveDerivedField(model, descriptorName);

			Descriptor descriptor;

			if (normDescName == null) {
				descriptor = FieldNameUtil.decodeDescriptor(qdb, descriptorName);
			} else {
				descriptor = FieldNameUtil.decodeDescriptor(qdb, normDescName);
				term.setNormalized(true);
			}

			if(descriptor == null){
				throw new IllegalArgumentException("Descriptor \'" + descriptorName.getValue() + "\' not found");
			}

			term.setIdentifier(descriptor.getId());

			terms.add(term);
		}

		{
			Equation.Term term = new Equation.Term();

			Double intercept = regressionTable.getIntercept();
			term.setCoefficient(intercept.toString());

			terms.add(term);
		}

		equation.setTerms(terms);

		return equation;
	}

	private static FieldName getPropertyName(RegressionModel model) {
		for (MiningField f: model.getMiningSchema().getMiningFields()) {
			if (f.getUsageType().equals(FieldUsageType.PREDICTED)) {
				return f.getName();
			}
		}
		throw new IllegalArgumentException("MiningSchema without predicted field");
	}

	private static FieldName resolveDerivedField(RegressionModel model, FieldName descriptorName) {
		LocalTransformations tr = model.getLocalTransformations();
		if (tr != null) {
			for (DerivedField df: tr.getDerivedFields()) {
				if (descriptorName.equals(df.getName())
						&& df.getExpression() instanceof NormContinuous) {
					NormContinuous nc = (NormContinuous) df.getExpression();
					return nc.getField();
				}
			}
		}
		return null;
	}
}
