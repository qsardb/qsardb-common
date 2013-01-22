/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

public class PredictionValuesMapping <V> extends ParameterValuesMapping<PredictionRegistry, Prediction, V> {

	public PredictionValuesMapping(Prediction prediction, ValueFormat<V> format){
		super(prediction, format);
	}

	public PredictionValuesMapping(Prediction prediction, ValueFormat<V> format, Erratum erratum){
		super(prediction, format, erratum);
	}
}