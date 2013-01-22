/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

public class EmptyIterator<E> implements Iterable<E>, Iterator<E> {

	@Override
	public Iterator<E> iterator(){
		return this;
	}

	@Override
	public boolean hasNext(){
		return false;
	}

	@Override
	public E next(){
		throw new NoSuchElementException();
	}

	@Override
	public void remove(){
		throw new UnsupportedOperationException();
	}
}