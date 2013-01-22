/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.codessa;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.vfs.*;

public class VfsUtil {

	private VfsUtil(){
	}

	static
	public FileObject toFileObject(File file) throws IOException {
		FileObject object = (VFS.getManager()).toFileObject(file);

		if((FileType.FILE).equals(object.getType())){

			try {
				String extension = (object.getName()).getExtension();
				if(extension.equals("")){
					extension = "zip";
				}

				object = (VFS.getManager()).createFileSystem(extension, object);
			} catch(FileSystemException fse){
				// Ignored
			}
		}

		return object;
	}

	static
	public FileObject toBaseFolder(File file) throws IOException {
		return toBaseFolder(toFileObject(file));
	}

	static
	public FileObject toBaseFolder(FileObject object) throws IOException {

		while(true){
			FileObject[] children = object.getChildren();

			if(children.length == 1){
				FileObject child = children[0];

				if((FileType.FOLDER).equals(child.getType())){
					object = child;

					continue;
				}
			}

			return object;
		}
	}

	static
	public FileObject[] listFiles(FileObject dir, final Matcher matcher) throws FileSystemException {
		FileSelector selector = new FileSelector(){

			public boolean includeFile(FileSelectInfo fileInfo){
				matcher.reset(baseName(fileInfo.getFile()));

				return matcher.matches();
			}

			public boolean traverseDescendents(FileSelectInfo fileInfo){
				return true;
			}
		};

		FileObject[] files = dir.findFiles(selector);

		Comparator<FileObject> comparator = new Comparator<FileObject>(){

			public int compare(FileObject left, FileObject right){
				return getId(left) - getId(right);
			}

			private int getId(FileObject object){
				matcher.reset(baseName(object));
				matcher.matches();

				return Integer.parseInt(matcher.group(1));
			}
		};

		Arrays.sort(files, comparator);

		return files;
	}

	static
	public String baseName(FileObject object){
		return (object.getName()).getBaseName();
	}

	static
	public InputStream getInputStream(FileObject object) throws IOException {
		return (object.getContent()).getInputStream();
	}

	static
	public Reader getReader(FileObject object, String encoding) throws IOException {
		return new InputStreamReader(getInputStream(object), encoding);
	}

	static
	public OutputStream getOutputStream(FileObject object) throws IOException {
		return (object.getContent()).getOutputStream();
	}

	static
	public Writer getWriter(FileObject object, String encoding) throws IOException {
		return new OutputStreamWriter(getOutputStream(object), encoding);
	}
}