/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class Chapter extends QMRFObject {

	abstract
	public String getChapter();

	abstract
	public String getHelp();

	abstract
	public String getName();
}