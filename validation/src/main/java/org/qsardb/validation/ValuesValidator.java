/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.model.*;

public class ValuesValidator extends MapValidator<ValuesCargo> {

	@Override
	public Iterator<ValuesCargo> selectEntities(Qdb qdb){
		return selectCargos(qdb, ValuesCargo.class);
	}
}