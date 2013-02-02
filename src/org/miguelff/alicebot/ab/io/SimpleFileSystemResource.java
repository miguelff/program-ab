package org.miguelff.alicebot.ab.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Provides resources from the file system, proxying old behavior of the program.
 * No mutex. No anything, this class is unsafe.
 * 
 * @author miguelff
 * 
 */
public class SimpleFileSystemResource extends AbstractIOResource  {

	private static final String DEFAULT_FS_ROOT = System.getenv("DEFAULT_FS_DIRECTORY");

	private String root;
	private File file;

	
	public SimpleFileSystemResource(String resourceName) {
		this(resourceName, DEFAULT_FS_ROOT);
	}

	public SimpleFileSystemResource(String resourceName, String root) {		
		super(resourceName);
		this.root = root;
		this.file = new File(root,resourceName);			
	}

	@Override
	public InputStream input() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public OutputStream output() {
		try {
			return new FileOutputStream(file);
		} catch (Exception e) {
			throw new RuntimeException(e);		
		}
	}
	
	
	@Override
	public OutputStream appendedOutput() {
		try {
			return new FileOutputStream(file, true);
		} catch (Exception e) {
			throw new RuntimeException(e);		
		}
	}

	@Override
	public void touch() {
		file.setLastModified(new Date().getTime());
	}

	@Override
	public long getLastModified() {
		return file.lastModified();
	}

	@Override
	public List<IOResource> getNested() {
		if (! hasNested()) {
			throw new UnsupportedOperationException("Only directories have nested resources: "+ getName() +" is not a directory");
		}
		List<IOResource> resources = new ArrayList<IOResource>();
		File[] files = file.listFiles();
		for (File f: files){
			resources.add(new SimpleFileSystemResource(f.getName().replace(this.root,"")));
		}
		return resources;
	}	

	@Override
	public boolean hasNested() {
		return file.isDirectory();
	}

}