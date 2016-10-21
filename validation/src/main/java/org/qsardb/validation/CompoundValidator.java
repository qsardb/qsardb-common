/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;
import java.util.regex.*;

import org.qsardb.model.*;

public class CompoundValidator extends ContainerValidator<Compound> {

	public CompoundValidator(Scope scope){
		super(scope);
	}

	@Override
	protected Iterator<CompoundRegistry> selectContainerRegistries(Qdb qdb){
		return new SingletonIterator<CompoundRegistry>(qdb.getCompoundRegistry());
	}

	@Override
	public void validate(){
		Scope scope = getScope();

		if((Scope.LOCAL).equals(scope)){
			validateCas();
			validateInChI();
		} else

		if((Scope.GLOBAL).equals(scope)){
			requireInChI();
		}
	}

	private void validateCas(){
		Compound compound = getEntity();

		String cas = compound.getCas();
		if(cas != null && !cas.isEmpty() && !validateCas(cas)){
			error("Invalid Cas \'" + cas + "\'");
		}
	}

	private void validateInChI(){
		Compound compound = getEntity();

		String inChI = compound.getInChI();
		if(inChI != null && !validateInChI(inChI)){
			error("Invalid InChI \'" + inChI + "\'");
		}
	}

	private void requireInChI(){
		Compound compound = getEntity();

		String inChI = compound.getInChI();
		if(isMissing(inChI)){
			error("Missing InChI");
		}
	}

	static
	private boolean validateCas(String string){
		Matcher matcher = cas_pattern.matcher(string);

		if(matcher.matches()){
			int sum = 0;

			String digits = (matcher.group(1) + matcher.group(2) + matcher.group(3));

			for(int i = 1, j = (digits.length() - 1) - 1; j > -1; i++, j--){
				sum += i * digitAt(digits, j);
			}

			return (sum % 10) == digitAt(digits, digits.length() - 1);
		}

		return false;
	}

	static
	private int digitAt(String digits, int index){
		char c = digits.charAt(index);

		return (c - '0');
	}

	static
	private boolean validateInChI(String string){
		Matcher matcher = inchi_pattern.matcher(string);

		return matcher.matches();
	}

	private static final Pattern cas_pattern = Pattern.compile("([0-9]{2,7})\\-([0-9]{2})\\-([0-9])");

	private static final Pattern inchi_pattern = Pattern.compile("InChI=1[S]?(?:\\/[^\\/]+)+");
}