/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

public class DescriptorValuesMapping <V> extends ParameterValuesMapping<DescriptorRegistry, Descriptor, V> {

	public DescriptorValuesMapping(Descriptor descriptor, ValueFormat<V> format){
		super(descriptor, format);
	}

	public DescriptorValuesMapping(Descriptor descriptor, ValueFormat<V> format, Erratum erratum){
		super(descriptor, format, erratum);
	}
}