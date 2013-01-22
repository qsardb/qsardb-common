/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class ParameterSetDescriptors extends QMRFObject {

	abstract
	public CollectionAnswer getAnswer();
}