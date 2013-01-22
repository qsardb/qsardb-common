/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.io.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

abstract
public class ParameterValuesMapping<R extends ContainerRegistry<R, C>, C extends Container<R, C>, V> extends Mapping {

	private C parameter = null;

	private ValueFormat<V> format = null;

	transient
	private Map<String, V> values = null;


	public ParameterValuesMapping(C parameter, ValueFormat<V> format){
		setParameter(parameter);
		setFormat(format);
	}

	public ParameterValuesMapping(C parameter, ValueFormat<V> format, Erratum erratum){
		super(erratum);

		setParameter(parameter);
		setFormat(format);
	}

	@Override
	public void beginMapping() throws IOException {
		C parameter = getParameter();
		ValueFormat<V> format = getFormat();

		this.values = new LinkedHashMap<String, V>();

		if(parameter.hasCargo(ValuesCargo.class)){
			ValuesCargo cargo = parameter.getCargo(ValuesCargo.class);

			this.values.putAll(cargo.loadMap(format));
		}
	}

	@Override
	public void mapValue(Compound compound, String string){
		string = filter(string);

		if(string == null){
			return;
		}

		C parameter = getParameter();
		ValueFormat<V> format = getFormat();

		try {
			this.values.put(compound.getId(), format.parse(string));
		} catch(RuntimeException re){
			System.err.println("Cannot parse " + parameter.getId() + " value \"" + escape(string) + "\" for " + compound.getId());

			throw re;
		}
	}

	@Override
	public void endMapping() throws IOException {
		C parameter = getParameter();
		ValueFormat<V> format = getFormat();

		ValuesCargo cargo = parameter.getOrAddCargo(ValuesCargo.class);

		cargo.storeMap(this.values, format);

		this.values.clear();
		this.values = null;
	}

	public C getParameter(){
		return this.parameter;
	}

	private void setParameter(C parameter){
		this.parameter = parameter;
	}

	public ValueFormat<V> getFormat(){
		return this.format;
	}

	private void setFormat(ValueFormat<V> format){
		this.format = format;
	}

	static
	private String escape(String string){
		StringBuffer sb = new StringBuffer();

		for(int i = 0; i < string.length(); i++){
			char c = string.charAt(i);

			if((c > 0 && c < 128) && Character.isLetterOrDigit(c)){
				sb.append(c);
			} else

			{
				sb.append("\\u");

				String hex = Integer.toHexString(c);
				for(int j = 0; j < (4 - hex.length()); j++){
					sb.append("0");
				}

				sb.append(hex);
			}
		}

		return sb.toString();
	}
}