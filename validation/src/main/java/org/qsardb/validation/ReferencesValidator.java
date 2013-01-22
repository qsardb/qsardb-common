/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.bibtex.*;
import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

import org.jbibtex.*;

public class ReferencesValidator extends MapValidator<ReferencesCargo> {

	@Override
	public Iterator<ReferencesCargo> selectEntities(Qdb qdb){
		return selectCargos(qdb, ReferencesCargo.class);
	}

	@Override
	protected void validateValues(Collection<String> values){
		Parameter<?, ?> parameter = getEntity().getContainer();

		BibTeXDatabase database = loadDatabase(parameter);
		if(isMissing(database)){
			return;
		}

		Map<Key, BibTeXEntry> entries = database.getEntries();

		for(String value : values){
			StringTokenizer st = new StringTokenizer(value, ",\u0020");

			while(st.hasMoreTokens()){
				Key key = new Key(st.nextToken());

				BibTeXEntry entry = entries.get(key);
				if(isMissing(entry)){
					error("Unresolved reference \'" + key.getValue() + "\'");
				}
			}
		}
	}

	private BibTeXDatabase loadDatabase(Parameter<?, ?> parameter){

		if(!parameter.hasCargo(BibTeXCargo.class)){
			error("Missing BibTeX Cargo");

			return null;
		}

		BibTeXCargo cargo = parameter.getCargo(BibTeXCargo.class);

		try {
			String bibtex = cargo.loadString("US-ASCII");

			return parseBibTeX(bibtex);
		} catch(Exception e){
			return null;
		}
	}

	private BibTeXDatabase parseBibTeX(String bibtex) throws IOException, ParseException {
		Reader reader = new StringReader(bibtex);

		try {
			BibTeXParser parser = new BibTeXParser(){

				@Override
				public void checkStringResolution(Key key, BibTeXString string){
				}

				@Override
				public void checkCrossReferenceResolution(Key key, BibTeXEntry entry){
				}
			};

			return parser.parse(reader);
		} finally {
			reader.close();
		}
	}
}