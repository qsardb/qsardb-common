/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.model.*;

abstract
public class CargoValidator<E extends Cargo> extends Validator<E> {

	protected boolean acceptCargo(Cargo<?> cargo){
		return true;
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	protected Iterator<Cargo<?>> selectCargos(Qdb qdb){
		return new SequenceIterator(selectContainers(qdb)){

			@Override
			public Iterable<Cargo<?>> createIterable(Object object){
				Container<?, ?> container = (Container<?, ?>)object;

				List<Cargo<?>> result = new ArrayList<Cargo<?>>();

				Set<String> ids = container.getCargos();
				for(String id : ids){
					Cargo<?> cargo = container.getCargo(id);

					result.add(cargo);
				}

				return result;
			}

			@Override
			protected boolean accept(Object object){
				return acceptCargo((Cargo<?>)object);
			}
		};
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	protected <C extends Cargo> Iterator<C> selectCargos(Qdb qdb, final Class<? extends C> clazz){
		return new SequenceIterator(selectContainers(qdb)){

			@Override
			public Iterable<C> createIterable(Object object){
				Container<?, ?> container = (Container<?, ?>)object;

				if(container.hasCargo(clazz)){
					C cargo = container.getCargo(clazz);

					return new SingletonIterator<C>(cargo);
				}

				return new EmptyIterator<C>();
			}

			@Override
			protected boolean accept(Object object){
				return acceptCargo((C)object);
			}
		};
	}
}