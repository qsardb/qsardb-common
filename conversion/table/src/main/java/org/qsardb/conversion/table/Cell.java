/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

public class Cell {

	private String text = null;


	public Cell(String text){
		setText(text);
	}

	public String getText(){
		return this.text;
	}

	private void setText(String text){
		this.text = text;
	}

	@Override
	public String toString(){
		return getText();
	}
}