/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import javax.xml.bind.annotation.*;

@XmlEnum
public enum YesNoAnswer {
	@XmlEnumValue("Yes")
	YES,
	@XmlEnumValue("No")
	NO,
	;
}