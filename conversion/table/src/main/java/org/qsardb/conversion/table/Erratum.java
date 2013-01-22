/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.util.*;

public class Erratum {

	private Map<String, String> errors = new LinkedHashMap<String, String>();

	private Map<String, String> warnings = new LinkedHashMap<String, String>();


	public String filter(String from){
		String to = this.errors.get(from);
		if(to == null){
			to = this.warnings.get(from);
		}

		return to;
	}

	public Map<String, String> errors(){
		return this.errors;
	}

	public Erratum error(String from, String to){

		if(to != null){
			this.errors.put(from, to);
		}

		return this;
	}

	public Map<String, String> warnings(){
		return this.warnings;
	}

	public Erratum warning(String from, String to){

		if(to != null){
			this.warnings.put(from, to);
		}

		return this;
	}
}