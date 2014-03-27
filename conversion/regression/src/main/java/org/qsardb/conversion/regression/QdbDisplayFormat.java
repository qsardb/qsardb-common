/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.regression;

import org.qsardb.model.*;

public class QdbDisplayFormat implements DisplayFormat {

	private Qdb qdb = null;


	public QdbDisplayFormat(Qdb qdb){
		setQdb(qdb);
	}

	public String formatLeftHandSide(String identifier){
		Property property = getQdb().getProperty(identifier);
		if(property != null){
			return property.getId();
		}

		return null;
	}

	public String formatRightHandSide(String identifier){
		Descriptor descriptor = getQdb().getDescriptor(identifier);
		if(descriptor != null){
			return descriptor.getId();
		}

		return null;
	}

	public Qdb getQdb(){
		return this.qdb;
	}

	private void setQdb(Qdb qdb){
		this.qdb = qdb;
	}
}