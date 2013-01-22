/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class EntityRef extends QMRFObject {

	abstract
	public Object getIdref();

	abstract
	public String getCatalog();
}