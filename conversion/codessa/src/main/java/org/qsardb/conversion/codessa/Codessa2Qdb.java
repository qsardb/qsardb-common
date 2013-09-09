/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.codessa;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.model.*;

import org.jpmml.manager.*;

import org.apache.commons.vfs.*;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.RegressionTable;

public class Codessa2Qdb {

	private Codessa2Qdb(){
	}

	static
	public void convert(Qdb qdb, File file) throws Exception {
		convert(qdb, VfsUtil.toBaseFolder(file));
	}

	static
	public void convert(Qdb qdb, FileObject rootDir) throws Exception {
		convertCompounds(qdb, rootDir);
		convertDescriptors(qdb, rootDir);
		convertProperties(qdb, rootDir);
		convertModels(qdb, rootDir);
		convertPredictions(qdb, rootDir);
	}

	static
	private void convertCompounds(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();

		FileObject propertyFile = rootDir.resolveFile("property.tmp");

		logger.log(Level.INFO, "Loading compounds from " + propertyFile.getURL());

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(propertyFile, ENCODING));

		try {
			reader.readLine();

			Matcher matcher = (Pattern.compile("\\s*(\\d+)\\s+(.*)")).matcher("");

			while(true){
				String line = reader.readLine();
				if(line == null){
					break;
				}

				matchLine(matcher, line);

				Compound compound = new Compound(matcher.group(1));
				compound.setName("Compound " + compound.getId());

				compounds.add(compound);

				logger.log(Level.FINE, compound.toString());
			}
		} finally {
			reader.close();
		}
	}

	static
	private void convertDescriptors(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();
		DescriptorRegistry descriptors = qdb.getDescriptorRegistry();

		FileObject descriptorFile = rootDir.resolveFile("descript.tmp");

		logger.log(Level.INFO, "Loading descriptors from " + descriptorFile.getURL());

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(descriptorFile, ENCODING));

		try {
			Matcher matcher = (Pattern.compile("\\s*(\\d+)\\s+(.*)")).matcher("");

			int index = 1;

			while(true){
				String line = reader.readLine();
				if(line == null){
					break;
				}

				matchLine(matcher, line);

				Descriptor descriptor = new Descriptor(String.valueOf(index++));
				descriptor.setName(matcher.group(2));
				descriptor.setApplication(APPLICATION);

				descriptors.add(descriptor);

				int count = Integer.parseInt(matcher.group(1));

				Map<String, Double> values = readMap(reader, count, false, compounds);

				ValuesCargo valuesCargo = descriptor.addCargo(ValuesCargo.class);
				valuesCargo.storeDoubleMap(values);

				logger.log(Level.FINE, descriptor.toString());
			}
		} finally {
			reader.close();
		}
	}

	static
	private void convertProperties(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();
		PropertyRegistry properties = qdb.getPropertyRegistry();

		FileObject propertyFile = rootDir.resolveFile("property.tmp");

		logger.log(Level.INFO, "Loading properties from " + propertyFile.getURL());

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(propertyFile, ENCODING));

		try {
			Matcher matcher = (Pattern.compile("\\s*(\\d+)\\s+(.*)")).matcher("");

			int index = 1;

			// Most probably, rolls only once
			while(true){
				String line = reader.readLine();
				if(line == null){
					break;
				}

				matchLine(matcher, line);

				Property property = new Property(String.valueOf(index++));
				property.setName(matcher.group(2));

				properties.add(property);

				int count = Integer.parseInt(matcher.group(1));

				Map<String, Double> values = readMap(reader, count, true, compounds);

				ValuesCargo valuesCargo = property.addCargo(ValuesCargo.class);
				valuesCargo.storeDoubleMap(values);

				logger.log(Level.FINE, property.toString());
			}
		} finally {
			reader.close();
		}
	}

	static
	private void convertModels(Qdb qdb, FileObject rootDir) throws IOException, QdbException {
		ModelRegistry models = qdb.getModelRegistry();

		PropertyRegistry properties = qdb.getPropertyRegistry();
		Map<String, Property> propertiesByName = nameMap(properties);

		DescriptorRegistry descriptors = qdb.getDescriptorRegistry();
		Map<String, Descriptor> descriptorsByName = nameMap(descriptors);

		FileObject outputFile = rootDir.resolveFile("bestout.tmp");

		logger.log(Level.INFO, "Loading models from " + outputFile.getURL());

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(outputFile, ENCODING));

		try {
			Matcher matcher = (Pattern.compile("\\s*" + COLUMN + COLUMN + COLUMN)).matcher("");

			int index = 1;

			String propertyName = seekProperty(reader);
			if(propertyName == null){
				logger.log(Level.WARNING, "Skipping models");

				return;
			}

			Property property = propertiesByName.get(propertyName);

			seekLine(reader, "Multilinear Regression Results");

			while(true){
				String line = seekLine(reader, "NUMBER OF PARAMETERS");
				if(line == null){
					break;
				}

				RegressionModelManager pmmlManager = new RegressionModelManager();
				pmmlManager.createModel(MiningFunctionType.REGRESSION);

				org.dmg.pmml.FieldName propertyField = FieldNameUtil.addPropertyField(pmmlManager, property);
				pmmlManager.setTarget(propertyField);

				reader.readLine();
				reader.readLine();
				reader.readLine();

				// Column labels
				reader.readLine();

				reader.readLine();
				reader.readLine();
				reader.readLine();

				RegressionTable regressionTable = new RegressionTable(Double.NaN);
				pmmlManager.getRegressionTables().add(regressionTable);

				// Intercept
				{
					line = reader.readLine();
					String interceptName = line.trim();

					line = reader.readLine();
					matchLine(matcher, line);

					double intercept = Double.parseDouble(matcher.group(2));
					regressionTable.setIntercept(intercept);
				}

				int descriptorCount = (index + 1);
				for(int i = 0; i < descriptorCount; i++){
					line = reader.readLine();

					String descriptorName = line.trim();
					Descriptor descriptor = descriptorsByName.get(descriptorName);

					line = reader.readLine();
					matchLine(matcher, line);

					Double coefficient = Double.valueOf(matcher.group(2));

					org.dmg.pmml.FieldName descriptorField = FieldNameUtil.addDescriptorField(pmmlManager, descriptor);
					RegressionModelManager.addNumericPredictor(regressionTable, descriptorField, coefficient);
				}

				Model model = new Model(String.valueOf(index++), property);
				model.setName("Model " + model.getId());

				models.add(model);

				PMMLCargo pmmlCargo = model.addCargo(PMMLCargo.class);
				pmmlCargo.storePmml(pmmlManager.getPmml());

				logger.log(Level.FINE, model.toString());
			}
		} finally {
			reader.close();
		}
	}

	static
	private void convertPredictions(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();

		ModelRegistry models = qdb.getModelRegistry();
		if(models.isEmpty()){
			logger.log(Level.WARNING, "Skipping predictions");

			return;
		}

		PropertyRegistry properties = qdb.getPropertyRegistry();
		Map<String, Property> propertiesByName = nameMap(properties);

		PredictionRegistry predictions = qdb.getPredictionRegistry();

		// Assumes that the ordering of compounds has not been modified
		FileObject pointsFile = rootDir.resolveFile("pointsb.tmp");

		logger.log(Level.INFO, "Loading predictions from " + pointsFile.getURL());

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(pointsFile, ENCODING));

		try {
			Matcher matcher = (Pattern.compile("\\s*" + COLUMN + COLUMN + COLUMN + COLUMN + COLUMN)).matcher("");

			int index = 1;

			String propertyName = seekProperty(reader);
			Property property = propertiesByName.get(propertyName);

			seekLine(reader, "Multilinear Regression Results");

			while(true){
				String line = seekLine(reader, "POINTS");
				if(line == null){
					break;
				}

				// Column labels
				reader.readLine();

				Model model = models.get(String.valueOf(index++));

				Prediction prediction = new Prediction(model.getId() + "-training", model, Prediction.Type.TRAINING);
				prediction.setName(property.getName());
				prediction.setApplication(APPLICATION);

				predictions.add(prediction);

				Map<String, Double> values = new LinkedHashMap<String, Double>();

				for(Compound compound : compounds){
					line = reader.readLine();
					matchLine(matcher, line);

					String key = compound.getId();
					Double value = Double.valueOf(matcher.group(3));

					values.put(key, value);
				}

				ValuesCargo valuesCargo = prediction.addCargo(ValuesCargo.class);
				valuesCargo.storeDoubleMap(values);
			}
		} finally {
			reader.close();
		}
	}

	static
	private Map<String, Double> readMap(BufferedReader reader, int count, boolean extended, CompoundRegistry compounds) throws IOException {
		Map<String, Double> result = new LinkedHashMap<String, Double>();

		Matcher matcher = (Pattern.compile("\\s*(\\d+)\\s+(.*)" + (extended ? "\\s+(.*)" : ""))).matcher("");

		for(int i = 0; i < count; i++){
			String line = reader.readLine();

			try {
				matchLine(matcher, line);
			} catch(IllegalArgumentException iae){
				continue;
			}

			String key = matcher.group(1);
			Double value = Double.valueOf(matcher.group(2));

			Compound compound = compounds.get(key);
			if(compound != null){
				result.put(key, value);
			} else

			{
				logger.log(Level.WARNING, "Unknown compound: " +key);
			}
		}

		return result;
	}

	static
	private String seekProperty(BufferedReader reader) throws IOException {
		String propertyName = seekLine(reader, "Property is");

		if(propertyName != null){
			propertyName = propertyName.substring(propertyName.indexOf("Property is") + "Property is".length()).trim();
		}

		return propertyName;
	}

	static
	private String seekLine(BufferedReader reader, String string) throws IOException {

		while(true){
			String line = reader.readLine();
			if(line == null || line.contains(string)){
				return line;
			}
		}
	}

	static
	private void matchLine(Matcher matcher, String line){
		matcher.reset(line);

		if(!matcher.matches()){
			throw new IllegalArgumentException(line);
		}
	}

	static
	private <C extends Parameter<?, ?>> Map<String, C> nameMap(Collection<C> parameters){
		Map<String, C> map = new HashMap<String, C>();

		for(C parameter : parameters){
			map.put(parameter.getName(), parameter);
		}

		return map;
	}

	private static final String APPLICATION = "CODESSA 2.20";

	private static final String ENCODING = "ISO-8859-1";

	private static final String COLUMN = "([^\\s]*)\\s*";

	private static final Logger logger = Logger.getLogger(Codessa2Qdb.class.getName());
}