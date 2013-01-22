/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class ParameterSetData extends QMRFObject {

	abstract
	public YesNoAnswer getChemname();

	abstract
	public YesNoAnswer getCas();

	abstract
	public YesNoAnswer getFormula();

	abstract
	public YesNoAnswer getInchi();

	abstract
	public YesNoAnswer getMol();

	abstract
	public YesNoAnswer getSmiles();
}