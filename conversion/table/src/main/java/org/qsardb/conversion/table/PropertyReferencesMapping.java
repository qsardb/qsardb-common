/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

public class PropertyReferencesMapping extends ParameterReferencesMapping<PropertyRegistry, Property> {

	public PropertyReferencesMapping(Property property){
		super(property);
	}
}