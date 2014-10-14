/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.bibtex.*;
import org.qsardb.model.*;

import org.jbibtex.*;

public class BibTeXValidator extends CargoValidator<BibTeXCargo> {

	@Override
	public Iterator<BibTeXCargo> selectEntities(Qdb qdb){
		return selectCargos(qdb, BibTeXCargo.class);
	}

	@Override
	public void validate() throws IOException {
		BibTeXCargo cargo = getEntity();

		String bibtex = cargo.loadString();

		BibTeXDatabase database;

		try {
			database = parseBibTeX(bibtex);
		} catch(Exception e){
			error("Failed to parse BibTeX", e);

			return;
		}

		validateDatabase(database);
	}

	private void validateDatabase(BibTeXDatabase database){
		Collection<Map.Entry<Key, BibTeXEntry>> entries = (database.getEntries()).entrySet();

		for(Map.Entry<Key, BibTeXEntry> entry : entries){
			validateEntry(entry.getValue());
		}
	}

	private void validateEntry(BibTeXEntry entry){
		Collection<Map.Entry<Key, Value>> fields = (entry.getFields()).entrySet();

		for(Map.Entry<Key, Value> field : fields){
			String latex = (field.getValue()).toUserString();

			if(latex.indexOf('\\') > -1 || latex.indexOf('{') > -1) {
				try {
					parseLaTeX(latex);
				} catch(Exception e){
					warning("Failed to parse LaTeX input: " + latex, e);
				}
			}
		}

		Value doi = entry.getField(BibTeXEntry.KEY_DOI);
		if(isMissing(doi)){
			warning("Entry \'" + (entry.getKey()).getValue() + "\' is missing DOI");
		}
	}

	private BibTeXDatabase parseBibTeX(String bibtex) throws IOException, ParseException {
		Reader reader = new StringReader(bibtex);

		try {
			BibTeXParser parser = new BibTeXParser(){

				@Override
				public void checkStringResolution(Key key, BibTeXString string){

					if(isMissing(string)){
						warning("Unresolved string \'" + key.getValue() + "\'");
					}
				}

				@Override
				public void checkCrossReferenceResolution(Key key, BibTeXEntry entry){

					if(isMissing(entry)){
						warning("Unresolved cross-reference \'" + key.getValue() + "\'");
					}
				}
			};

			return parser.parse(reader);
		} finally {
			reader.close();
		}
	}

	private List<LaTeXObject> parseLaTeX(String latex) throws IOException, ParseException {
		Reader reader = new StringReader(latex);

		try {
			LaTeXParser parser = new LaTeXParser();

			return parser.parse(reader);
		} finally {
			reader.close();
		}
	}
}