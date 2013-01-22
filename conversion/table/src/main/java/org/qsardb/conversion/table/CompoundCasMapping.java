/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.util.regex.*;

import org.qsardb.model.*;

public class CompoundCasMapping extends CompoundAttributeMapping {

	public CompoundCasMapping(){
	}

	public CompoundCasMapping(Erratum erratum){
		super(erratum);
	}

	@Override
	public String filter(String string){
		string = super.filter(string);

		if(string != null){

			if(isValidCas(string)){
				return string;
			} // End if

			if(isValidNumber(string)){
				return (string.substring(0, string.length() - 3) + "-" + string.substring(string.length() - 3, string.length() - 1) + "-" + string.substring(string.length() - 1));
			} // End if

			// Contains digits, but is in unrecognized format
			for(int i = 0; i < string.length(); i++){
				char c = string.charAt(i);

				if(Character.isDigit(c)){
					throw new IllegalArgumentException(string);
				}
			}
		}

		return null;
	}

	@Override
	public void setAttribute(Compound compound, String string){
		compound.setCas(string);
	}

	static
	public boolean isValidCas(String string){
		Matcher matcher = CompoundCasMapping.casRegex.matcher(string);

		return matcher.matches();
	}

	static
	public boolean isValidNumber(String string){
		Matcher matcher = CompoundCasMapping.numberRegex.matcher(string);

		return matcher.matches();
	}

	private static final Pattern casRegex = Pattern.compile("[0-9]{2,7}\\-[0-9]{2}\\-[0-9]");
	private static final Pattern numberRegex = Pattern.compile("[0-9]{5,10}");
}