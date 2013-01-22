/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

abstract
public class SequenceIterator<I, E> implements Iterator<E> {

	private Iterator<I> iterables = null;

	private E next = null;

	private Iterator<E> iterator = null;


	public SequenceIterator(Iterator<I> iterables){
		this.iterables = iterables;

		this.next = prepareNext();
	}

	abstract
	public Iterable<E> createIterable(I iterable);

	@Override
	public boolean hasNext(){
		return this.next != null;
	}

	@Override
	public E next(){
		E result = this.next;

		if(result == null){
			throw new NoSuchElementException();
		}

		this.next = prepareNext();

		return result;
	}

	@Override
	public void remove(){
		throw new UnsupportedOperationException();
	}

	protected boolean accept(E element){
		return true;
	}

	private E prepareNext(){

		if(this.iterator == null || !this.iterator.hasNext()){

			if(this.iterables.hasNext()){
				Iterable<E> iterable = createIterable(this.iterables.next());

				this.iterator = iterable.iterator();
			} else

			{
				this.iterator = null;
			}
		}

		while(this.iterator != null && this.iterator.hasNext()){
			E element = this.iterator.next();

			if(accept(element)){
				return element;
			}
		}

		if(this.iterables.hasNext()){
			return prepareNext();
		}

		return null;
	}
}