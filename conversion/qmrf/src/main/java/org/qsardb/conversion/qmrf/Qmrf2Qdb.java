/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.qmrf;

import it.jrc.ecb.qmrf.*;
import it.jrc.ecb.qmrf.Descriptor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Level;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.structure.*;
import org.qsardb.conversion.regression.*;
import org.qsardb.conversion.sdfile.*;
import org.qsardb.conversion.table.*;
import org.qsardb.model.*;

import org.apache.commons.io.*;

import org.dmg.pmml.*;

import org.jsoup.*;

public class Qmrf2Qdb {

	private Qmrf2Qdb(){
	}

	static
	public void convert(Qdb qdb, QMRF qmrf) throws Exception {
		parseArchiveDescriptor(qdb, qmrf);
		parsePropertyAndDescriptors(qdb, qmrf);
		parseModelAndPredictions(qdb, qmrf);
	}

	static
	private void parseArchiveDescriptor(Qdb qdb, QMRF qmrf) throws Exception {
		QMRFChapters chapters = qmrf.getQMRFChapters();

		Archive archive = qdb.getArchive();

		QSARIdentifier qsarIdentifier = chapters.getQSARIdentifier();

		QSARTitle title = qsarIdentifier.getQSARTitle();
		archive.setName(htmlToText(title.getContent()));
	}

	static
	private void parsePropertyAndDescriptors(Qdb qdb, QMRF qmrf) throws Exception {
		QMRFChapters chapters = qmrf.getQMRFChapters();

		PropertyRegistry propertyRegistry = qdb.getPropertyRegistry();

		QSAREndpoint qsarEndpoint = chapters.getQSAREndpoint();

		org.qsardb.model.Property qdbProperty = new org.qsardb.model.Property(asPropertyId("1"));
		qdbProperty.setName(null);
		qdbProperty.setDescription(null);

		ModelSpecies modelSpecies = qsarEndpoint.getModelSpecies();
		qdbProperty.setSpecies(htmlToText(modelSpecies.getContent()));

		ModelEndpoint modelEndpoint = qsarEndpoint.getModelEndpoint();

		List<EndpointRef> endpointRefs = modelEndpoint.getEndpointRef();
		for(EndpointRef endpointRef : endpointRefs){
			Endpoint endpoint = endpointRef.getIdref();
			qdbProperty.setEndpoint(toString(endpoint));

			break;
		}

		propertyRegistry.add(qdbProperty);

		DescriptorRegistry descriptorRegistry = qdb.getDescriptorRegistry();

		QSARAlgorithm qsarAlgorithm = chapters.getQSARAlgorithm();

		AlgorithmsDescriptors algorithmDescriptors = qsarAlgorithm.getAlgorithmsDescriptors();

		List<DescriptorRef> descriptorRefs = algorithmDescriptors.getDescriptorRef();
		for(DescriptorRef descriptorRef : descriptorRefs){
			Descriptor descriptor = descriptorRef.getIdref();

			org.qsardb.model.Descriptor qdbDescriptor = new org.qsardb.model.Descriptor(asDescriptorId(descriptor.getId()));
			qdbDescriptor.setName(trimText(descriptor.getName()));
			qdbDescriptor.setDescription(trimText(descriptor.getDescription()));

			descriptorRegistry.add(qdbDescriptor);
		}
	}

	static
	private void parseModelAndPredictions(Qdb qdb, QMRF qmrf) throws Exception {
		QMRFChapters chapters = qmrf.getQMRFChapters();

		ModelRegistry modelRegistry = qdb.getModelRegistry();

		org.qsardb.model.Property qdbProperty = qdb.getProperty(asPropertyId("1"));

		QSARAlgorithm qsarAlgorithm = chapters.getQSARAlgorithm();

		org.qsardb.model.Model qdbModel = new org.qsardb.model.Model(asModelId("1"), qdbProperty);
		qdbModel.setName(null);
		qdbModel.setDescription(null);

		modelRegistry.add(qdbModel);

		AlgorithmExplicit algorithmExplicit = qsarAlgorithm.getAlgorithmExplicit();

		List<AlgorithmRef> algorithmRefs = algorithmExplicit.getAlgorithmRef();
		AlgorithmRef algorithmRef = algorithmRefs.get(0);

		Algorithm algorithm = algorithmRef.getIdref();
		if(isRegression(algorithm)){
			String equation = htmlToText(algorithmExplicit.getEquation());

			String leftHandSide = qdbProperty.getId();
			String rightHandSide = equation.substring(equation.indexOf('=') + 1);

			DescriptorRegistry descriptorRegistry = qdb.getDescriptorRegistry();
			for(org.qsardb.model.Descriptor qdbDescriptor : descriptorRegistry){
				rightHandSide = replaceSubstring(rightHandSide, qdbDescriptor.getName(), qdbDescriptor.getId());
			}

			// This fix is specific to MolCode Ltd. descriptor names
			rightHandSide = rightHandSide.replaceAll("\\(AM1\\)", "");

			String qdbEquation = (leftHandSide + "=" + rightHandSide);
			logger.log(Level.FINE, qdbEquation);

			try {
				PMML pmml = RegressionUtil.parse(qdb, qdbEquation);

				PMMLCargo pmmlCargo = qdbModel.addCargo(PMMLCargo.class);
				pmmlCargo.storePmml(pmml);
			} catch(Exception e){
				logger.log(Level.WARNING, "Cannot parse equation", e);
			}
		}

		PredictionRegistry predictionRegistry = qdb.getPredictionRegistry();

		QSARMiscellaneous qsarMiscellaneous = chapters.getQSARMiscelaneous();

		Attachments attachments = qsarMiscellaneous.getAttachments();

		QSARRobustness qsarRobustness = chapters.getQSARRobustness();
		AttachmentTrainingData trainingData = attachments.getAttachmentTrainingData();

		if((qsarRobustness.getTrainingSetAvailability()).getAnswer() == YesNoAnswer.YES){
			logger.log(Level.INFO, "Converting training set");

			org.qsardb.model.Prediction qdbPrediction = new org.qsardb.model.Prediction(asTrainingId("1"), qdbModel, Prediction.Type.TRAINING);
			qdbPrediction.setName(null);
			qdbPrediction.setDescription(null);

			predictionRegistry.add(qdbPrediction);

			List<Molecules> molecules = trainingData.getMolecules();
			parseAttachment(qdb, qdbProperty, qdbPrediction, molecules.get(0), qsarRobustness.getTrainingSetData());
		}

		List<QSARPredictivity> qsarPredictivities = chapters.getQSARPredictivity();
		AttachmentValidationData validationData = attachments.getAttachmentValidationData();

		for(QSARPredictivity qsarPredictivity : qsarPredictivities){
			int index = qsarPredictivities.indexOf(qsarPredictivity);

			if((qsarPredictivity.getValidationSetAvailability()).getAnswer() == YesNoAnswer.YES){
				logger.log(Level.INFO, "Converting validation set");

				org.qsardb.model.Prediction qdbPrediction = new org.qsardb.model.Prediction(asValidationId(String.valueOf(index + 1)), qdbModel, Prediction.Type.VALIDATION);
				qdbPrediction.setName(null);
				qdbPrediction.setDescription(null);

				predictionRegistry.add(qdbPrediction);

				List<Molecules> molecules = validationData.getMolecules();
				parseAttachment(qdb, qdbProperty, qdbPrediction, molecules.get(index), qsarPredictivity.getValidationSetData());
			}
		}
	}

	static
	private void parseAttachment(Qdb qdb, org.qsardb.model.Property qdbProperty, org.qsardb.model.Prediction qdbPrediction, Attachment attachment, ParameterSetData parameterSetData) throws Exception {
		File file = File.createTempFile("attachment", null);

		try {
			URL url = new URL((attachment.getUrl()).replace(' ', '+'));
			logger.log(Level.INFO, "Downloading attachment " + url);

			FileUtils.copyURLToFile(url, file);

			Table table = new SDFile(file);

			List<Column> columns = new ArrayList<Column>();

			Iterator<Column> it = table.columns();
			while(it.hasNext()){
				Column column = it.next();
				columns.add(column);
			}

			TableSetup setup = new TableSetup();

			compoundCas:
			if(parameterSetData.getCas() == YesNoAnswer.YES){
				Column column = getColumn(columns, "CAS", true);
				if(column == null){
					break compoundCas;
				}
				columns.remove(column);

				setup.addMapping(column.getId(), new CompoundCasMapping());
			} // End if

			compoundName:
			if(parameterSetData.getChemname() == YesNoAnswer.YES){
				Column column = getColumn(columns, "Name", true);
				if(column == null){
					logger.log(Level.WARNING, "Extracting compound names from molfile header block");

					setup.addMapping(SDFile.COLUMN_MOLFILE, new MolfileCompoundNameMapping());

					break compoundName;
				}

				columns.remove(column);

				setup.addMapping(column.getId(), new CompoundNameMapping());
			} // End if

			compoundInChI:
			if(parameterSetData.getInchi() == YesNoAnswer.YES){
				Column column = getColumn(columns, "InChI", true);
				if(column == null){
					break compoundInChI;
				}
				columns.remove(column);

				setup.addMapping(column.getId(), new CompoundInChIMapping());
			} // End if

			if(parameterSetData.getMol() == YesNoAnswer.YES){
				setup.addMapping(SDFile.COLUMN_MOLFILE, new CompoundCargoMapping(ChemicalMimeData.MDL_MOLFILE.getId()));
			} // End if

			compoundSmilesCargo:
			if(parameterSetData.getSmiles() == YesNoAnswer.YES){
				Column column = getColumn(columns, "SMILES", true);
				if(column == null){
					break compoundSmilesCargo;
				}
				columns.remove(column);

				setup.addMapping(column.getId(), new CompoundCargoMapping(ChemicalMimeData.DAYLIGHT_SMILES.getId()));
			} // End if

			DescriptorRegistry descriptorRegistry = qdb.getDescriptorRegistry();

			qdbDescriptor:
			for(org.qsardb.model.Descriptor qdbDescriptor : descriptorRegistry){
				Column column = getColumn(columns, qdbDescriptor.getName(), true);
				if(column == null){
					logger.log(Level.WARNING, "Cannot find descriptor " + qdbDescriptor.getName());

					continue qdbDescriptor;
				}

				columns.remove(column);

				setup.addMapping(column.getId(), new DescriptorValuesMapping<Double>(qdbDescriptor, new DoubleFormat()));
			}

			qdbProperty:
			if(qdbProperty != null){
				Column column = getColumn(columns, "exp.", true); // XXX
				if(column == null){
					logger.log(Level.WARNING, "Cannot find property " + qdbProperty.getName());

					break qdbProperty;
				}

				columns.remove(column);

				setup.addMapping(column.getId(), new PropertyValuesMapping<Double>(qdbProperty, new DoubleFormat()));
			} // End if

			qdbPrediction:
			if(qdbPrediction != null){
				Column column = getColumn(columns, "pred.", true); // XXX
				if(column == null){
					logger.log(Level.WARNING, "Cannot find prediction " + qdbPrediction.getName());

					break qdbPrediction;
				}

				columns.remove(column);

				setup.addMapping(column.getId(), new PredictionValuesMapping<Double>(qdbPrediction, new DoubleFormat()));
			}

			Table2Qdb.convert(qdb, table, setup);
		} finally {
			file.delete();
		}
	}

	static
	private Column getColumn(Collection<Column> columns, String id){
		return getColumn(columns, id, false);
	}

	static
	private Column getColumn(Collection<Column> columns, String id, boolean nullable){
		List<Column> columnCandidates = new ArrayList<Column>();

		for(Column column : columns){

			if((column.getId()).equalsIgnoreCase(id)){
				return column;
			} else

			if((column.getId().toLowerCase()).contains(id.toLowerCase())){
				columnCandidates.add(column);
			}
		}

		if(columnCandidates.size() == 1){
			return columnCandidates.get(0);
		} // End if

		if(nullable){
			return null;
		}

		throw new IllegalArgumentException("Cannot find column '" + id + "'");
	}

	static
	private String replaceSubstring(String string, String left, String right){
		String originalString = string;

		for(int i = left.length(); i >= Math.min(5, left.length()); i--){
			string = string.replace(left.substring(0, i), right);

			if(!(string).equals(originalString)){
				return string;
			}
		}

		throw new IllegalArgumentException("Cannot find '" + left + "' (or substring of it) in '" + originalString + "'");
	}

	static
	private boolean isRegression(Algorithm algorithm){

		if(algorithm != null){
			return isRegression(algorithm.getDefinition()) || isRegression(algorithm.getDescription());
		}

		return false;
	}

	static
	private boolean isRegression(String string){
		return string != null && (string.toLowerCase()).contains("regression");
	}

	static
	private String toString(Endpoint endpoint){
		StringBuffer sb = new StringBuffer();

		String group = endpoint.getGroup();
		if(group.matches("\\d\\..*")){
			sb.append(trimText(group));
		}

		String subgroup = endpoint.getSubgroup();
		if(subgroup.matches("\\d\\.\\d.*")){

			if(sb.length() > 0){
				sb.append(" ");
			}

			sb.append(trimText(subgroup));
		}

		return sb.toString();
	}

	static
	private String asPropertyId(String id){
		return "property" + id;
	}

	static
	private String asDescriptorId(String id){

		if(id.startsWith("descriptors_catalog_")){
			id = id.replace("descriptors_catalog_", "descriptor");
		}

		return id;
	}

	static
	private String asModelId(String id){
		return "model" + id;
	}

	static
	private String asTrainingId(String id){
		return "training" + id;
	}

	static
	private String asValidationId(String id){
		return "validation" + id;
	}

	static
	private String trimText(String string){

		if(string != null){
			string = string.trim();
		}

		return string;
	}

	static
	private String htmlToText(String string){
		org.jsoup.nodes.Document document = Jsoup.parse(string);

		return trimText(document.text());
	}

	private static final Logger logger = Logger.getLogger(Qmrf2Qdb.class.getName());
}