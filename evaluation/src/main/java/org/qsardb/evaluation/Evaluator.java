/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.evaluation;

import java.math.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

abstract
public class Evaluator {

	private Qdb qdb = null;

	private String summary = null;

	private Property property = null;

	private List<Descriptor> descriptors = null;


	public Evaluator(Qdb qdb){
		setQdb(qdb);
	}

	abstract
	protected String loadSummary() throws Exception;

	abstract
	protected Property loadProperty() throws Exception;

	abstract
	protected List<Descriptor> loadDescriptors() throws Exception;

	abstract
	public Result evaluate(Map<Descriptor, ?> values) throws Exception;

	public void init() throws Exception {
		setSummary(loadSummary());

		setProperty(loadProperty());
		setDescriptors(loadDescriptors());
	}

	public void destroy() throws Exception {
	}

	public Object evaluateAndFormat(Map<Descriptor, ?> values, DecimalFormat format) throws Exception {
		Result result = evaluate(values);

		return formatResult(result, format);
	}

	protected String formatResult(final Result result, DecimalFormat format){
		StringBuilder sb = new StringBuilder();

		Property property = getProperty();
		sb.append(property.getName());

		sb.append(' ' ).append('=').append(' ');

		if(format == null){
			format = getFormat(property);
		}

		sb.append(format(result.getValue(), format));

		if (result.getProbability() != null) {
			sb.append(String.format(" (p=%.2f)", result.getProbability()));
		}

		return sb.toString();
	}

	protected Property getProperty(String id){
		Property property = getQdb().getProperty(id);
		if(property == null){
			throw new IllegalArgumentException("Property \'" + id + "\' not found");
		}

		return property;
	}

	protected Descriptor getDescriptor(String id){
		Descriptor descriptor = getQdb().getDescriptor(id);
		if(descriptor == null){
			throw new IllegalArgumentException("Descriptor \'" + id + "\' not found");
		}

		return descriptor;
	}

	public Qdb getQdb(){
		return this.qdb;
	}

	private void setQdb(Qdb qdb){
		this.qdb = qdb;
	}

	public String getSummary(){
		return this.summary;
	}

	private void setSummary(String summary){
		this.summary = summary;
	}

	public Property getProperty(){
		return this.property;
	}

	private void setProperty(Property property){
		this.property = property;
	}

	public List<Descriptor> getDescriptors(){
		return this.descriptors;
	}

	private void setDescriptors(List<Descriptor> descriptors){
		this.descriptors = descriptors;
	}

	static
	protected String format(Object value, DecimalFormat format){

		if(format != null){

			if(value instanceof Number){
				Number number = (Number)value;

				return format.format(number.doubleValue());
			}
		}

		return String.valueOf(value);
	}

	static
	public DecimalFormat getFormat(Parameter<?, ?> parameter){

		if(parameter.hasCargo(ValuesCargo.class)){
			ValuesCargo valuesCargo = parameter.getCargo(ValuesCargo.class);

			try {
				Map<String, BigDecimal> values = parseValues(valuesCargo.loadStringMap());

				ScaleFrequencyMap map = ScaleFrequencyMap.sample(values.values());

				int minCount = Math.max(3, values.size() / 10);

				return new DecimalFormat(map.getPattern(minCount), new DecimalFormatSymbols(Locale.US));
			} catch(Exception e){
				logger.log(Level.WARNING, "Parameter \'" + parameter.getId() + "\' does not specify decimal format", e);
			}
		}

		return null;
	}

	static
	private Map<String, BigDecimal> parseValues(Map<String, String> values){
		Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();

		Collection<Map.Entry<String, String>> entries = values.entrySet();
		for(Map.Entry<String, String> entry : entries){
			if (entry.getValue() == null) {
				continue;
			}

			try {
				result.put(entry.getKey(), new BigDecimal(entry.getValue()));
			} catch(NumberFormatException nfe){
				// Ignored
			}
		}

		return result;
	}

	static
	public class Result {

		private Object value = null;
		private Double probability = null;

		private Map<Descriptor, ?> parameters = null;


		public Result(Object value, Map<Descriptor, ?> parameters){
			setValue(value);
			setParameters(parameters);
		}

		public Object getValue(){
			return this.value;
		}

		private void setValue(Object value){
			this.value = value;
		}

		public Map<Descriptor, ?> getParameters(){
			return this.parameters;
		}

		private void setParameters(Map<Descriptor, ?> parameters){
			this.parameters = parameters;
		}

		public Double getProbability() {
			return this.probability;
		}

		void setProbability(Double p) {
			this.probability = p;
		}
	}

	private static final Logger logger = Logger.getLogger(Evaluator.class.getName());
}