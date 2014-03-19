/*
 * Copyright (c) 2014 University of Tartu
 */

package org.qsardb.validation;

import java.util.Iterator;
import org.qsardb.model.Archive;
import org.qsardb.model.Qdb;


public class ArchiveValidator extends Validator<Archive> {

	@Override
	public Iterator<Archive> selectEntities(Qdb qdb) {
		return new SingletonIterator<Archive>(qdb.getArchive());
	}

	@Override
	public void validate() throws Exception {
		validateName();
		validateDescription();
	}

	private void validateName() {
		String name = getEntity().getName();
		if (isMissing(name)) {
			warning("Archive name is missing");
		}
	}

	private void validateDescription() {
		String description = getEntity().getDescription();
		if (isMissing(description)) {
			warning("Archive description is missing");
		}
	}
	
}
