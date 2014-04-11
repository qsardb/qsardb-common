/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.regression;

import java.util.*;

public class EquationFormatter {

	public String formatEquation(Equation equation, DisplayFormat format){
		return formatLeftHandSide(equation.getIdentifier(), format) + " = " + formatRightHandSide(equation.getTerms(), format);
	}

	public String formatLeftHandSide(String identifier, DisplayFormat format){

		if(format != null){
			identifier = format.formatLeftHandSide(identifier);
		}

		return identifier;
	}

	public String formatRightHandSide(List<Equation.Term> terms, DisplayFormat format){
		StringBuilder sb = new StringBuilder();

		for(Equation.Term term : terms){
			String coefficientSign = "+";
			String coefficient = term.getCoefficient();

			if(coefficient.startsWith("-") || coefficient.startsWith("+")){
				coefficientSign = coefficient.substring(0, 1);
				coefficient = coefficient.substring(1);
			} // End if

			if(sb.length() > 0){
				sb.append(' ');

				sb.append(coefficientSign).append(' ').append(coefficient);
			} else

			{
				if("+".equals(coefficientSign)){
					sb.append(coefficient);
				} else

				{
					sb.append(coefficientSign).append(coefficient);
				}
			}

			String coefficientPrecision = term.getCoefficientPrecision();
			if(coefficientPrecision != null){
				sb.append('(').append('\u00b1').append(coefficientPrecision).append(')');
			}

			String identifier = term.getIdentifier();
			if(identifier != null){
				sb.append(' ');

				if(format != null){
					identifier = format.formatRightHandSide(identifier);
				}

				sb.append('*').append(' ').append(identifier);

				String exponent = term.getExponent();
				if (!exponent.equals("1")) {
					sb.append('^').append(exponent);
				}
			}
		}

		return sb.toString();
	}
}