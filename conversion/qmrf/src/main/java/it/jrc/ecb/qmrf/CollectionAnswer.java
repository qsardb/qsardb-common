/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import javax.xml.bind.annotation.*;

@XmlEnum
public enum CollectionAnswer {
	@XmlEnumValue("All")
	ALL,
	@XmlEnumValue("Some")
	SOME,
	@XmlEnumValue("No")
	NO,
	@XmlEnumValue("Unknown")
	UNKNOWN,
	;
}