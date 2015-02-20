/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.statistics;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.dmg.pmml.DataField;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.cargo.pmml.PMMLCargo;
import org.qsardb.model.Model;
import org.qsardb.model.Parameter;
import org.qsardb.model.Prediction;

public class StatisticsUtil {

	public static Statistics evaluate(Model model, Prediction prediction) {
		if (isRegression(model)) {
			return new RegressionStatistics(model, prediction);
		} else {
			return new ClassificationStatistics(model, prediction);
		}
	}

	static boolean isRegression(Model model){
		PMML pmml = getPMML(model);
		if (pmml != null){
			for (DataField df: pmml.getDataDictionary().getDataFields()){
				String id = df.getName().getValue().replace("properties/", "");
				if (model.getProperty().getId().equals(id)){
					return df.getOptype() == OpType.CONTINUOUS;
				}
			}
			return false;
		} else {
			Map<String, String> map = loadValues(model.getProperty());
			int count = 0;
			for (String cid: map.keySet()) {
				try {
					Double.parseDouble(map.get(cid));
					count++;
				} catch (NumberFormatException e) {
					// ignored
				}
			}
			return count > 0;
		}
	}
	
	static PMML getPMML(Model model){
		if (model.hasCargo(PMMLCargo.class)) {
			PMMLCargo pmmlCargo = model.getCargo(PMMLCargo.class);
			try {
				return pmmlCargo.loadPmml();
			} catch (Exception ignored) {
			}
		}			
		return null;
	}

	static Map<String, String> loadValues(Parameter<?,?> param) {
		try {
			if (param.hasCargo(ValuesCargo.class)) {
				ValuesCargo cargo = param.getCargo(ValuesCargo.class);
				return cargo.loadStringMap();
			}
		} catch (IOException e) {
			throw new RuntimeException("Error in loadStringMap: "+param.getId(), e);
		}

		return Collections.emptyMap();
	}
}