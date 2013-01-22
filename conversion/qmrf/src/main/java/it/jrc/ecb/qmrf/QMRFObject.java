/*
 * Copyright (c) 2011 University of Tartu
 */
package it.jrc.ecb.qmrf;

import java.io.*;

import javax.xml.bind.annotation.*;

import org.jvnet.jaxb2_commons.lang.*;
import org.jvnet.jaxb2_commons.locator.*;

@XmlTransient
abstract
public class QMRFObject implements Equals, HashCode, Serializable {

	public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object that, EqualsStrategy equalsStrategy){
		return true;
	}

	public int hashCode(ObjectLocator thisLocator, HashCodeStrategy hashCodeStrategy){
		return 1;
	}
}