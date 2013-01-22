/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import org.qsardb.model.*;

abstract
public class Mapping {

	private Erratum erratum = null;


	protected Mapping(){
	}

	protected Mapping(Erratum erratum){
		setErratum(erratum);
	}

	public void beginMapping() throws Exception {
	}

	abstract
	public void mapValue(Compound compound, String string) throws Exception;

	public String filter(String string){

		if(string != null){

			if(this.erratum != null){
				String replacementString = this.erratum.filter(string);

				if(replacementString != null){
					return replacementString;
				}
			}
		}

		return string;
	}

	public void endMapping() throws Exception {
	}

	public Erratum getErratum(){
		return this.erratum;
	}

	public void setErratum(Erratum erratum){
		this.erratum = erratum;
	}
}