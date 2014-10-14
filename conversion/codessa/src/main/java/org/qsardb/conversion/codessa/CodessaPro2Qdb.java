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
import org.qsardb.cargo.structure.*;
import org.qsardb.model.*;

import org.apache.commons.io.*;
import org.apache.commons.vfs.*;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.RegressionTable;
import org.qsardb.conversion.regression.Equation;
import org.qsardb.conversion.regression.RegressionUtil;

public class CodessaPro2Qdb {

	private CodessaPro2Qdb(){
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
		convertCorrelations(qdb, rootDir);
	}

	static
	private void convertCompounds(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();

		FileObject scfDir = rootDir.resolveFile("mopscf");

		logger.log(Level.INFO, "Loading compounds from " + scfDir.getURL());

		Matcher matcher = (Pattern.compile("S0*(\\d+)\\.MNO")).matcher("");

		FileObject[] scfFiles = VfsUtil.listFiles(scfDir, matcher);
		for(FileObject scfFile : scfFiles){
			Compound compound = new Compound(parseId(matcher, scfFile));
			compound.setName("Compound " +compound.getId());

			compounds.add(compound);

			Cargo<Compound> scfCargo = compound.addCargo(ChemicalMimeData.MOPAC_OUT.getId());

			InputStream is = VfsUtil.getInputStream(scfFile);

			try {
				scfCargo.storeByteArray(IOUtils.toByteArray(is));
			} finally {
				is.close();
			}

			logger.log(Level.FINE, compound.toString());
		}
	}

	static
	private void convertDescriptors(Qdb qdb, FileObject rootDir) throws IOException {
		convertInternalDescriptors(qdb, rootDir);
		convertExternalDescriptors(qdb, rootDir);
	}

	static
	private void convertInternalDescriptors(Qdb qdb, FileObject rootDir) throws IOException {
		DescriptorRegistry descriptors = qdb.getDescriptorRegistry();

		FileObject descsFile = rootDir.resolveFile("work").resolveFile("descs.txt");

		logger.log(Level.INFO, "Loading descriptors from " + descsFile.getURL());

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(descsFile, ENCODING));

		try {
			Pattern tokensPattern = Pattern.compile("\\t");

			while(true){
				String line = reader.readLine();
				if(line == null){
					break;
				}

				String[] tokens = tokensPattern.split(line);

				Descriptor descriptor = new Descriptor(tokens[0].trim());
				descriptor.setName(tokens[2]);
				descriptor.setApplication(APPLICATION);

				descriptor.getLabels().addAll(convertDescriptorLabels(tokens[6]));

				descriptors.add(descriptor);

				logger.log(Level.FINE, descriptor.toString());
			}
		} finally {
			reader.close();
		}

		readCompoundDescriptorValues(qdb, rootDir);
	}

	static
	private Collection<String> convertDescriptorLabels(String path){

		if(path.startsWith("/theory/descriptors/")){
			path = path.substring("/theory/descriptors/".length());
		} // End if

		if(path.endsWith("/index.html")){
			path = path.substring(0, path.length() - "/index.html".length());
		} else

		if(path.endsWith(".htm")){
			path = path.substring(0, path.length() - ".htm".length());
		} // End if

		return Arrays.asList(path.split("/"));
	}

	static
	private void readCompoundDescriptorValues(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();
		DescriptorRegistry descriptors = qdb.getDescriptorRegistry();

		Map<Descriptor, Map<String, Double>> valuesMap = new HashMap<Descriptor, Map<String, Double>>();

		for(Compound compound : compounds){
			readCompoundValues(compound, descriptors, valuesMap, rootDir);
		}

		for(Descriptor descriptor : descriptors){
			Map<String, Double> values = valuesMap.get(descriptor);
			if(values == null){
				logger.log(Level.WARNING, "Descriptor " + descriptor.getId() + " has no values");

				continue;
			}

			ValuesCargo valuesCargo = descriptor.addCargo(ValuesCargo.class);
			valuesCargo.storeDoubleMap(values);
		}
	}

	static
	private void readCompoundValues(Compound compound, DescriptorRegistry descriptors, Map<Descriptor, Map<String, Double>> valuesMap, FileObject rootDir) throws IOException {
		FileObject desFile = rootDir.resolveFile("descs").resolveFile("S" + formatId(compound.getId()) + ".DES");

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(desFile, ENCODING));

		try {
			Matcher matcher = (Pattern.compile("(\\d+)\\s+(.*)\\s+;\\s+(.*)")).matcher("");

			while(true){
				String line = reader.readLine();
				if(line == null){
					break;
				}

				matcher.reset(line);

				// Process molecular descriptors, skip fragmental and atomical descriptors
				if(matcher.matches() && matcher.group(1).length() == 10){
					String descId = matcher.group(1);
					descId = prepareDescriptorId(descId);

					Descriptor descriptor = descriptors.get(descId);
					if(descriptor == null){
						descriptor = new Descriptor(descId);
						descriptor.setName(matcher.group(3));
						descriptor.setApplication(APPLICATION);

						descriptors.add(descriptor);
					}

					Map<String, Double> values = valuesMap.get(descriptor);
					if(values == null){
						values = new LinkedHashMap<String, Double>();

						valuesMap.put(descriptor, values);
					}

					String key = compound.getId();
					Double value = Double.valueOf(matcher.group(2));

					values.put(key, value);
				}
			}
		} finally {
			reader.close();
		}
	}

	static
	private void convertExternalDescriptors(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();
		DescriptorRegistry descriptors = qdb.getDescriptorRegistry();

		FileObject descsDir = rootDir.resolveFile("descs");

		logger.log(Level.INFO, "Loading descriptors from " + descsDir.getURL());

		Matcher matcher = (Pattern.compile("D0*(\\d+)\\.DES")).matcher("");

		FileObject[] desFiles = VfsUtil.listFiles(descsDir, matcher);
		for(FileObject desFile : desFiles){
			Descriptor descriptor = new Descriptor("_" +parseId(matcher, desFile));
			descriptor.setApplication(APPLICATION);

			descriptors.add(descriptor);

			readParameter(descriptor, compounds, desFile);

			logger.log(Level.FINE, descriptor.toString());
		}
	}

	static
	private void convertProperties(Qdb qdb, FileObject rootDir) throws IOException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();
		PropertyRegistry properties = qdb.getPropertyRegistry();

		FileObject propsDir = rootDir.resolveFile("props");

		logger.log(Level.INFO, "Loading properties from " + propsDir.getURL());

		Matcher matcher = (Pattern.compile("P0*(\\d+)\\.prp")).matcher("");

		FileObject[] prpFiles = VfsUtil.listFiles(propsDir, matcher);
		for(FileObject prpFile : prpFiles){
			Property property = new Property(parseId(matcher, prpFile));

			properties.add(property);

			readParameter(property, compounds, prpFile);

			logger.log(Level.FINE, property.toString());
		}
	}

	@SuppressWarnings (
		value = {"unused"}
	)
	static
	private <C extends Parameter<?, ?>> void readParameter(C parameter, CompoundRegistry compounds, FileObject object) throws IOException {
		BufferedReader reader = new BufferedReader(VfsUtil.getReader(object, ENCODING));

		try {
			String name = reader.readLine();
			String comment = reader.readLine();

			parameter.setName(name);

			Map<String, Double> values = readMap(reader, compounds);

			ValuesCargo valuesCargo = parameter.addCargo(ValuesCargo.class);
			valuesCargo.storeDoubleMap(values);
		} finally {
			reader.close();
		}
	}

	static
	private void convertCorrelations(Qdb qdb, FileObject rootDir) throws IOException, QdbException {
		FileObject corrsDir = rootDir.resolveFile("corrs");

		logger.log(Level.INFO, "Loading correlations from " + corrsDir.getURL());

		Matcher matcher = (Pattern.compile("C0*(\\d+).cor")).matcher("");

		FileObject[] corFiles = VfsUtil.listFiles(corrsDir, matcher);
		for(FileObject corFile : corFiles){
			convertCorrelation(qdb, parseId(matcher, corFile), corFile);
		}
	}

	static
	private void convertCorrelation(Qdb qdb, String id, FileObject corFile) throws IOException, QdbException {
		CompoundRegistry compounds = qdb.getCompoundRegistry();
		DescriptorRegistry descriptors = qdb.getDescriptorRegistry();
		PropertyRegistry properties = qdb.getPropertyRegistry();
		ModelRegistry models = qdb.getModelRegistry();
		PredictionRegistry predictions = qdb.getPredictionRegistry();

		logger.log(Level.INFO, "Loading model and prediction from " + corFile.getURL());

		BufferedReader reader = new BufferedReader(VfsUtil.getReader(corFile, ENCODING));

		try {
			Matcher interceptMatcher = (Pattern.compile(COLUMN + COLUMN + COLUMN)).matcher("");
			Matcher descriptorMatcher = (Pattern.compile(COLUMN + COLUMN + COLUMN + COLUMN + COLUMN)).matcher("");

			String modelSummary = reader.readLine();
			reader.readLine();
			String type = reader.readLine();

			Property property = properties.get(token(reader.readLine()));

			String statistics = reader.readLine();
			String fCrit = reader.readLine();
			String stdDev = reader.readLine();

			Equation eq = new Equation();
			eq.setIdentifier(property.getId());
			eq.setTerms(new ArrayList<Equation.Term>());

			// Intercept
			{
				String line = reader.readLine();
				matchLine(interceptMatcher, token(line));

				Equation.Term intercept = new Equation.Term();
				intercept.setCoefficient(interceptMatcher.group(1));
				eq.getTerms().add(intercept);
			}

			int descriptorCount = Integer.parseInt(token(reader.readLine()));
			for(int i = 0; i < descriptorCount; i++){
				String line = reader.readLine();
				matchLine(descriptorMatcher, token(line));

				String descId = descriptorMatcher.group(1);
				descId = prepareDescriptorId(descId);
				String coefficient = descriptorMatcher.group(2);

				Equation.Term term = new Equation.Term();
				term.setIdentifier(descId);
				term.setCoefficient(coefficient);
				eq.getTerms().add(term);
			}

			Model model = new Model(id, property);
			model.setName("Model " + model.getId());

			models.add(model);

			logger.log(Level.FINE, model.toString());

			String predictionSummary = reader.readLine();

			Map<String, Double> values = readMap(reader, compounds);

			PMMLCargo pmmlCargo = model.addCargo(PMMLCargo.class);
			pmmlCargo.storePmml(RegressionUtil.parse(qdb, eq));

			Prediction prediction = new Prediction(model.getId() + "-training", model, Prediction.Type.TRAINING);
			prediction.setName(property.getName());
			prediction.setApplication(APPLICATION);

			predictions.add(prediction);

			ValuesCargo valuesCargo = prediction.addCargo(ValuesCargo.class);
			valuesCargo.storeDoubleMap(values);

			logger.log(Level.FINE, prediction.toString());
		} finally {
			reader.close();
		}
	}

	static
	private Map<String, Double> readMap(BufferedReader reader, CompoundRegistry compounds) throws IOException {
		Map<String, Double> result = new LinkedHashMap<String, Double>();

		Matcher matcher = (Pattern.compile("0*(\\d+)\\s+(.*)")).matcher("");

		while(true){
			String line = reader.readLine();
			if(line == null){
				break;
			}

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
				logger.log(Level.WARNING, "Unknown compound: " + key);
			}
		}

		return result;
	}

	static
	private String parseId(Matcher matcher, FileObject object){
		matchLine(matcher, VfsUtil.baseName(object));

		return matcher.group(1);
	}

	static
	private String formatId(String id){

		while(id.length() < 7){
			id = ("0" + id);
		}

		return id;
	}

	static
	private void matchLine(Matcher matcher, String line){
		matcher.reset(line);

		if(!matcher.matches()){
			throw new IllegalArgumentException(line);
		}
	}

	static
	private String prepareDescriptorId(String id){

		if(id.length() == 10){
			String prefix = id.substring(0, 4);
			while(prefix.startsWith("0")){
				prefix = prefix.substring(1);
			}

			String suffix = id.substring(4);
			while(suffix.startsWith("0")){
				suffix = suffix.substring(1);
			}

			// Internal descriptor
			if(prefix.length() > 0){

				if(suffix.length() > 0){
					return prefix + "_" + suffix;
				}

				return prefix;
			}

			// External descriptor
			if(suffix.length() > 0){
				return "_" + suffix;
			}

			throw new IllegalArgumentException(id);
		}

		return id;
	}

	static
	private String token(String line){
		int hash = line.indexOf('#');

		return (line.substring(0, hash)).trim();
	}

	private static final String APPLICATION = "CODESSA PRO 2005";

	private static final String ENCODING = "ISO-8859-1";

	private static final String COLUMN = "([^\\s]*)\\s*";

	private static final Logger logger = Logger.getLogger(CodessaPro2Qdb.class.getName());
}