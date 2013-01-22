/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.util.*;

import org.qsardb.model.*;
import org.qsardb.validation.Message.*;

abstract
public class Validator<E extends Resource> {

	private E entity = null;

	private MessageCollector collector = null;


	abstract
	public Iterator<E> selectEntities(Qdb qdb);

	abstract
	public void validate() throws Exception;

	final
	public void run(Qdb qdb){
		Iterator<E> entities = selectEntities(qdb);

		while(entities.hasNext()){
			E entity = entities.next();

			setEntity(entity);

			try {
				validate();
			} catch(Exception e){
				error("Validation exception", e);
			} finally {
				setEntity(null);
			}
		}
	}

	protected Iterator<? extends ContainerRegistry<?, ?>> selectContainerRegistries(Qdb qdb){
		List<ContainerRegistry<?, ?>> result = new ArrayList<ContainerRegistry<?, ?>>();
		result.add(qdb.getCompoundRegistry());
		result.add(qdb.getPropertyRegistry());
		result.add(qdb.getDescriptorRegistry());
		result.add(qdb.getModelRegistry());
		result.add(qdb.getPredictionRegistry());

		return result.iterator();
	}

	protected boolean acceptContainer(Container<?, ?> container){
		return true;
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	protected Iterator<? extends Container<?, ?>> selectContainers(Qdb qdb){
		return new SequenceIterator(selectContainerRegistries(qdb)){

			@Override
			public Iterable<? extends Container<?, ?>> createIterable(Object object){
				ContainerRegistry<?, ?> registry = (ContainerRegistry<?, ?>)object;

				return registry;
			}

			@Override
			protected boolean accept(Object object){
				return acceptContainer((Container<?, ?>)object);
			}
		};
	}

	public void error(String content){
		error(content, null);
	}

	public void error(String content, Exception cause){
		message(Message.Level.ERROR, content);
	}

	public void warning(String content){
		warning(content, null);
	}

	public void warning(String content, Exception cause){
		message(Message.Level.WARNING, content);
	}

	private void message(Message.Level level, String content){
		MessageCollector collector = getCollector();

		if(collector != null){
			String path = getEntity().qdbPath();

			collector.add(new Message(level, path, content));
		}
	}

	public E getEntity(){
		return this.entity;
	}

	private void setEntity(E entity){
		this.entity = entity;
	}

	public MessageCollector getCollector(){
		return this.collector;
	}

	public void setCollector(MessageCollector collector){
		this.collector = collector;
	}

	static
	protected boolean isMissing(Object object){
		return object == null;
	}

	static
	protected boolean isMissing(String string){
		return (string == null) || (string.trim()).equals("");
	}
}