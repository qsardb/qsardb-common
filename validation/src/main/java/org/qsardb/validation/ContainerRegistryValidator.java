/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.model.*;

abstract
public class ContainerRegistryValidator<E extends ContainerRegistry> extends Validator<E> {

	@Override
	public Iterator<E> selectEntities(Qdb qdb){
		return (Iterator<E>)selectContainerRegistries(qdb);
	}
}