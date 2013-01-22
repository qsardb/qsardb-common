/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class Attachment extends QMRFObject {

	abstract
	public Embedded getEmbedded();

	abstract
	public String getUrl();

	abstract
	public String getFiletype();

	abstract
	public String getDescription();
}