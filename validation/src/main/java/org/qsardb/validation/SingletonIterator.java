/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

public class SingletonIterator<E> implements Iterable<E>, Iterator<E> {

	private E element = null;


	public SingletonIterator(E element){
		this.element = element;
	}

	@Override
	public Iterator<E> iterator(){
		return this;
	}

	@Override
	public boolean hasNext(){
		return this.element != null;
	}

	@Override
	public E next(){
		E result = this.element;

		if(result == null){
			throw new NoSuchElementException();
		}

		this.element = null;

		return result;
	}

	@Override
	public void remove(){
		throw new UnsupportedOperationException();
	}
}