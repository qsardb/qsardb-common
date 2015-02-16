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

			if (coefficient == null) {
				coefficientSign = "";
				coefficient = "";
			} else 
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
				addIdentifier(term, format, sb);
			}

			String function = term.getFunction();
			if(function != null && term.getArguments()!= null){
				addFunction(term, format, sb);
			}
		}

		return sb.toString();
	}

	private void addIdentifier(Equation.Term term, DisplayFormat format, StringBuilder sb){
		String identifier = term.getIdentifier();
		if(format != null){
			identifier = format.formatRightHandSide(identifier);
		}

		if (sb.length() > 0) {
			sb.append(" * ");
		}
		sb.append(identifier);

		String exponent = getExponent(term);
		if (exponent != null) {
			sb.append('^').append(exponent);
		}
	}

	private void addFunction(Equation.Term term, DisplayFormat format, StringBuilder sb){
		if (sb.length() > 0) {
			sb.append(" * ");
		}

		String function = term.getFunction();
		boolean isFunction = function.length() > 1;
		String delimiter = isFunction ? ", " : " "+function+" ";

		StringBuilder argsBuilder = new StringBuilder();
		for (Equation.Term t: term.getArguments()){
			argsBuilder.append(delimiter);
			argsBuilder.append(formatRightHandSide(Arrays.asList(t), format));
		}

		String args = argsBuilder.substring(delimiter.length());
		String exponent = getExponent(term);
		if (isFunction) {
			sb.append(function).append("(").append(args).append(")");
		} else if ("*/".contains(function) && exponent == null) {
			sb.append(args);
		} else {
			sb.append("(").append(args).append(")");
		}

		if (exponent != null) {
			sb.append('^').append(exponent);
		}
	}

	private String getExponent(Equation.Term term) {
		String exp = term.getExponent();
		return (exp != null && !exp.equals("1")) ? exp : null;
	}
}
