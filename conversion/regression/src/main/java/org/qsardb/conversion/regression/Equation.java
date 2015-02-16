/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.regression;

import java.util.*;

public class Equation {

	private String identifier = null;

	private List<Term> terms = null;


	/**
	 * The left-hand side of the equation
	 */
	public String getIdentifier(){
		return this.identifier;
	}

	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}

	/**
	 * The right-hand side of the equation
	 */
	public List<Term> getTerms(){
		return this.terms;
	}

	public void setTerms(List<Term> terms){
		this.terms = terms;
	}

	static
	public class Term {

		private String coefficient;

		private String coefficientPrecision;

		private String identifier;

		private String exponent;

		private String function;

		private List<Term> arguments;


		public Term(){
		}

		public String getCoefficient(){
			return this.coefficient;
		}

		public void setCoefficient(String coefficient){
			this.coefficient = coefficient;
		}

		public String getCoefficientPrecision(){
			return this.coefficientPrecision;
		}

		public void setCoefficientPrecision(String coefficientPrecision){
			this.coefficientPrecision = coefficientPrecision;
		}

		public boolean isIntercept(){
			return this.identifier == null;
		}

		public String getIdentifier(){
			return this.identifier;
		}

		public void setIdentifier(String identifier){
			this.identifier = identifier;
		}

		public String getExponent() {
			return this.exponent;
		}

		public void setExponent(String exponent) {
			this.exponent = exponent;
		}

		public String getFunction() {
			return this.function;
		}

		public void setFunction(String function) {
			this.function = function;
		}

		public List<Term> getArguments() {
			return this.arguments;
		}

		public void setArguments(List<Term> crossTerms){
			this.arguments = crossTerms;
		}
	}
}