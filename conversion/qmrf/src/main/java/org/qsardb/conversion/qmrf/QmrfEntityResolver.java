/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.qmrf;

import java.io.*;

import org.xml.sax.*;

class QmrfEntityResolver implements EntityResolver {

	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		String systemName = systemId.substring(systemId.lastIndexOf('/') + 1);

		if(systemName.equalsIgnoreCase("qmrf.dtd")){
			InputStream is = (getClass().getClassLoader()).getResourceAsStream("qmrf.dtd");
			if(is == null){
				throw new IOException();
			}

			return new InputSource(is);
		}

		return null;
	}
}