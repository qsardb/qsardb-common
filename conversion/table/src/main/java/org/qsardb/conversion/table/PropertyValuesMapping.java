/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

public class PropertyValuesMapping <V> extends ParameterValuesMapping<PropertyRegistry, Property, V> {

	public PropertyValuesMapping(Property property, ValueFormat<V> format){
		super(property, format);
	}

	public PropertyValuesMapping(Property property, ValueFormat<V> format, Erratum erratum){
		super(property, format, erratum);
	}
}