/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.pmml.*;
import org.qsardb.model.*;

import org.dmg.pmml.*;
import org.dmg.pmml.Model;

public class PMMLValidator extends CargoValidator<PMMLCargo> {

	@Override
	protected Iterator<ModelRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<ModelRegistry>(qdb.getModelRegistry());
	}

	@Override
	public Iterator<PMMLCargo> selectEntities(Qdb qdb){
		return selectCargos(qdb, PMMLCargo.class);
	}

	@Override
	public void validate() throws IOException {
		PMMLCargo cargo = getEntity();

		PMML pmml;

		try {
			pmml = cargo.loadPmml();
		} catch(IllegalArgumentException ex) {
			error("Failed to parse PMML", ex);
			return;
		} catch(QdbException qe){
			error("Failed to parse PMML", qe);
			return;
		}

		validatePmml(pmml);
	}

	private void validatePmml(PMML pmml){
		List<Model> models = pmml.getModels();

		if(models.size() < 1){
			error("Missing Model element");
			return;
		} else if(models.size() > 1){
			error("Too many Model elements");
			return;
		}

		validateModel(models.get(0));
	}

	private void validateModel(Model model){
		Qdb qdb = getEntity().getQdb();

		MiningSchema miningSchema = model.getMiningSchema();

		List<MiningField> miningFields = miningSchema.getMiningFields();
		for(MiningField miningField : miningFields){
			FieldName name = miningField.getName();

			switch(miningField.getUsageType()){
				case ACTIVE:
					Descriptor descriptor = FieldNameUtil.decodeDescriptor(qdb, name);
					if(isMissing(descriptor)){
						error("Unknown Descriptor Id \'" + FieldNameUtil.decodeDescriptorId(name) + "\'");
					}
					break;
				case PREDICTED:
					Property property = FieldNameUtil.decodeProperty(qdb, name);
					String propertyId = FieldNameUtil.decodePropertyId(name);
					if(isMissing(property)){
						error("Unknown Property Id \'" + propertyId + "\'");
						return;
					}
					if(!getEntity().getContainer().getProperty().equals(property)) {
						error("Model's Property Id links to different property: "+propertyId);
					}
					break;
				default:
					break;
			}
		}
	}
}
