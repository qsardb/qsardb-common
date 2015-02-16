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

	private static void addDataField(DataDictionary dict, Parameter param) {
		FieldName name = getFieldName(param);
		DataField df = new DataField(name, OpType.CONTINUOUS, DataType.DOUBLE);
		df.setDisplayName(param.getName());
		dict.getDataFields().add(df);
	}

	private static void addMiningField(MiningSchema schema, Parameter param) {
		MiningField mf = new MiningField(getFieldName(param));
		if (param instanceof Property) {
			mf.setUsageType(FieldUsageType.PREDICTED);
		} else {
			mf.setUsageType(FieldUsageType.ACTIVE);
		}
		schema.getMiningFields().add(mf);
	}

	private static FieldName getFieldName(Parameter param) {
		if (param instanceof Property) {
			return FieldNameUtil.encodeProperty((Property) param);
		} else if (param instanceof Descriptor) {
			return FieldNameUtil.encodeDescriptor((Descriptor) param);
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
			Equation.Term term = formatNumericPredictor(qdb, model, numericPredictor);
			terms.add(term);
		}

		for(PredictorTerm predictorTerm: regressionTable.getPredictorTerms()){
			Equation.Term term = formatPredictorTerm(qdb, model, predictorTerm);
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

	private static Equation.Term formatNumericPredictor(Qdb qdb, RegressionModel model, NumericPredictor numericPredictor) {
		FieldName descriptorName = numericPredictor.getName();

		Equation.Term term = RegressionUtil.createTerm(qdb, model, descriptorName);

		Double coefficient = numericPredictor.getCoefficient();
		term.setCoefficient(coefficient.toString());

		int exponent = numericPredictor.getExponent();
		term.setExponent(Integer.toString(exponent));

		return term;
	}

	private static Equation.Term formatPredictorTerm(Qdb qdb, RegressionModel model, PredictorTerm predictorTerm) {
		Equation.Term term = new Equation.Term();

		Double coefficient = predictorTerm.getCoefficient();
		term.setCoefficient(coefficient.toString());

		ArrayList<Equation.Term> crossTerms = new ArrayList<Equation.Term>();
		for (FieldRef fieldRef: predictorTerm.getFieldRefs()){
			FieldName descriptorName = fieldRef.getField();
			Equation.Term cterm = RegressionUtil.createTerm(qdb, model, descriptorName);
			crossTerms.add(cterm);
		}

		term.setFunction("*");
		term.setArguments(crossTerms);
		return term;
	}

	private static Equation.Term createTerm(Qdb qdb, RegressionModel model, FieldName descriptorName) {
		DerivedField df = findDerivedField(model, descriptorName);

		if (df != null && df.getExpression() instanceof Apply){
			Apply apply = (Apply) df.getExpression();
			return createTerm(qdb, model, apply);
		} else if (df != null && df.getExpression() instanceof NormContinuous){
			NormContinuous nc = (NormContinuous) df.getExpression();
			Equation.Term term = RegressionUtil.createTerm(qdb, model, nc.getField());
			term.setFunction("norm");
			return term;
		}

		Equation.Term term = new Equation.Term();
		Descriptor descriptor = resolveDescriptor(qdb, descriptorName);

		term.setIdentifier(descriptor.getId());
		return term;
	}

	private static Equation.Term createTerm(Qdb qdb, RegressionModel model, Apply apply){
		Equation.Term term = new Equation.Term();

		String function = formatFunction(apply);
		term.setFunction(function);
		
		ArrayList<Equation.Term> args = new ArrayList<Equation.Term>();
		for (Expression e: apply.getExpressions()) {
			if (e instanceof FieldRef) {
				args.add(RegressionUtil.createTerm(qdb, model, ((FieldRef)e).getField()));
			} else if (e instanceof Constant) {
				Equation.Term constTerm = new Equation.Term();
				constTerm.setCoefficient(((Constant)e).getValue());
				args.add(constTerm);
			} else if (e instanceof Apply) {
				args.add(createTerm(qdb, model, (Apply) e));
			} else {
				throw new IllegalArgumentException(e.toString());
			}
		}

		term.setArguments(args);
		return term;
	}

	private static Descriptor resolveDescriptor(Qdb qdb, FieldName descriptorName){
		Descriptor descriptor = FieldNameUtil.decodeDescriptor(qdb, descriptorName);
		if(descriptor == null){
			throw new IllegalArgumentException("Descriptor \'" + descriptorName.getValue() + "\' not found");
		}
		return descriptor;
	}


	private static DerivedField findDerivedField(RegressionModel model, FieldName descriptorName) {
		LocalTransformations tr = model.getLocalTransformations();
		if (tr != null) {
			for (DerivedField df: tr.getDerivedFields()) {
				if (descriptorName.equals(df.getName())) {
					return df;
				}
			}
		}
		return null;
	}

	private static String formatFunction(Apply apply) {
		String[] supportedFunctions = {
			"log10", "ln", "sqrt", "abs", "exp", "pow", "+", "-", "*", "/"
		};
		String query = apply.getFunction();
		for (String function: supportedFunctions) {
			if ("log10".equals(query)) {
				return "log";
			}
			if (function.equals(query)) {
				return function;
			}
		}

		throw new IllegalArgumentException("Apply function: "+query);
	}
}
