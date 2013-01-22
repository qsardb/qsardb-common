/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

public class DescriptorReferencesMapping extends ParameterReferencesMapping<DescriptorRegistry, Descriptor> {

	public DescriptorReferencesMapping(Descriptor descriptor){
		super(descriptor);
	}
}