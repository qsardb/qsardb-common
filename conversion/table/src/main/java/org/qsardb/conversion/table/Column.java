/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

public class Column {

	private String id = null;


	public Column(String id){
		setId(id);
	}

	public String getId(){
		return this.id;
	}

	private void setId(String id){
		this.id = id;
	}

	@Override
	public int hashCode(){
		return this.getId().hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof Column){
			Column that = (Column)object;

			return (this.getId()).equals(that.getId());
		}

		return false;
	}

	@Override
	public String toString(){
		return getId();
	}
}