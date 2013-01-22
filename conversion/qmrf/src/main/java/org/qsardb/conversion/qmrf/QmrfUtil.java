/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.qmrf;

import it.jrc.ecb.qmrf.*;
import it.jrc.ecb.qmrf.ObjectFactory;

import java.io.*;

import javax.xml.bind.*;
import javax.xml.transform.sax.*;

import org.qsardb.model.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class QmrfUtil {

	private QmrfUtil(){
	}

	static
	public QMRF loadQmrf(InputStream is) throws JAXBException, SAXException {
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setEntityResolver(new QmrfEntityResolver());

		InputSource source = new InputSource(is);
		SAXSource filteredSource = new SAXSource(reader, source);

		return (QMRF)JAXBUtil.createUnmarshaller(getJAXBContext()).unmarshal(filteredSource);
	}

	static
	public void storeQmrf(QMRF qmrf, OutputStream os) throws JAXBException {
		JAXBUtil.createMarshaller(getJAXBContext()).marshal(qmrf, os);
	}

	static
	private JAXBContext getJAXBContext() throws JAXBException {

		if(QmrfUtil.jaxbCtx == null){
			QmrfUtil.jaxbCtx = JAXBContext.newInstance(ObjectFactory.class);
		}

		return QmrfUtil.jaxbCtx;
	}

	private static JAXBContext jaxbCtx = null;
}