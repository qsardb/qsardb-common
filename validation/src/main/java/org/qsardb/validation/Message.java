/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.validation;

import java.io.*;

public class Message implements Serializable {

	private Level level = null;

	private String path = null;

	private String content = null;


	public Message(Level level, String path, String content){
		setLevel(level);
		setPath(path);
		setContent(content);
	}

	public Level getLevel(){
		return this.level;
	}

	private void setLevel(Level level){
		this.level = level;
	}

	public String getPath(){
		return this.path;
	}

	private void setPath(String path){
		this.path = path;
	}

	public String getContent(){
		return this.content;
	}

	private void setContent(String content){
		this.content = content;
	}

	static
	public enum Level {
		ERROR("error"),
		WARNING("warning"),
		;

		private String value = null;


		Level(String value){
			setValue(value);
		}

		public String getValue(){
			return this.value;
		}

		private void setValue(String value){
			this.value = value;
		}
	}
}