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

		private String coefficient = null;

		private String coefficientPrecision = null;

		private String identifier = null;

		private String exponent = null;

		private boolean normalized = false;


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

		public boolean isNormalized() {
			return this.normalized;
		}

		void setNormalized(boolean isNormalized) {
			this.normalized = isNormalized;
		}
	}
}