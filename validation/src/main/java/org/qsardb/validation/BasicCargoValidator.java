/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;
import java.util.*;

import org.qsardb.model.*;

public class BasicCargoValidator extends CargoValidator {

	private int limit = 1024 * 1024;


	public BasicCargoValidator(){
	}

	public BasicCargoValidator(int limit){
		setLimit(limit);
	}

	@Override
	public Iterator<Cargo<?>> selectEntities(Qdb qdb){
		return selectCargos(qdb);
	}

	@Override
	public void validate() throws IOException {
		validateId();
		validateSize();
	}

	private void validateId(){
		Cargo<?> cargo = (Cargo<?>)getEntity();

		String id = cargo.getId();
		if(!IdUtil.validate(id)){
			error("Invalid Id \'" + id + "\'");
		}
	}

	private void validateSize() throws IOException {
		Cargo<?> cargo = (Cargo<?>)getEntity();

		InputStream is;
		try{
			is = cargo.getInputStream();
		} catch (FileNotFoundException ex) {
			error("Missing payload");
			return;
		}

		int size = 0;

		try {
			byte[] buffer = new byte[1024];

			while(true){
				int count = is.read(buffer);
				if(count < 0){
					break;
				}

				size += count;
			}
		} finally {
			is.close();
		}

		int limit = getLimit(cargo);

		if(size == 0){
			error("Empty payload");
		} else

		if(size > limit){
			warning("Over-size payload (max " + limit + " bytes)");
		}
	}

	public int getLimit(Cargo<?> cargo){
		return getLimit();
	}

	public int getLimit(){
		return this.limit;
	}

	public void setLimit(int limit){
		this.limit = limit;
	}
}
