/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.conversion.table;

import java.util.*;

import org.qsardb.model.*;

public class CompoundLabelsMapping extends CompoundAttributeMapping {

	@Override
	public void setAttribute(Compound compound, String string){
		Set<String> labels = compound.getLabels();

		if(string != null){
			labels.addAll(parse(string));
		}
	}

	static
	private Set<String> parse(String string){
		Set<String> result = new LinkedHashSet<String>();

		StringTokenizer st = new StringTokenizer(string, ",");

		while(st.hasMoreTokens()){
			String token = (st.nextToken()).trim();

			if(token.length() > 0){
				result.add(token);
			}
		}

		return result;
	}
}