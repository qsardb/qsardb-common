/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.sdfile;

import org.qsardb.conversion.table.*;

public class MolfileCompoundNameMapping extends CompoundNameMapping {

	@Override
	public String filter(String string){

		if(string != null){
			int newline = string.indexOf("\n");
			if(newline < 0){
				throw new IllegalArgumentException();
			}

			string = (string.substring(0, newline)).trim();
		}

		return string;
	}
}