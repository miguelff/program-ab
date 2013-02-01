package org.miguelff.alicebot.ab;

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
 * Modelles a resource, with input and output
 * as used by the application to handle robot's
 * brain memory.
 * 
 * @author miguelff
 *
 */
public interface IOResource {
	
	public static final IOResource NULL = NullResource.instance;
	
	public String getName();

	public InputStream input();

	public OutputStream output();
	
	public OutputStream appendedOutput();
	
	public List<IOResource> getNested();
	
	public void touch();
	
	public long getLastModified();

	public boolean hasNested();

	public boolean exists();
}

final class NullResource implements IOResource {

	static final IOResource instance = new NullResource();
	
	private NullResource(){}
	
	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream input() {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream output() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IOResource> getNested() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void touch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastModified() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNested() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public OutputStream appendedOutput() {
		throw new UnsupportedOperationException();
	}
	
}

abstract class AbstractResource implements IOResource {
	
	public boolean exists(){
		return this != IOResource.NULL;
	}
}


/**
 * Provides resources from the file system, proxying old behavior of the program.
 * No mutex. No anything, this class is unsafe.
 * 
 * @author miguelff
 * 
 */
class SimpleFileSystemResource extends AbstractResource  {

	private static final String DEFAULT_FS_ROOT = System.getProperty("user.home");

	private String resourceName, root;
	private File file;

	
	SimpleFileSystemResource(String resourceName) {
		this(resourceName, DEFAULT_FS_ROOT);
	}

	SimpleFileSystemResource(String resourceName, String root) {		
		this.resourceName = resourceName;
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
			throw new UnsupportedOperationException("Only directories have nested resources: "+ resourceName+" is not a directory");
		}
		List<IOResource> resources = new ArrayList<IOResource>();
		File[] files = file.listFiles();
		for (File f: files){
			resources.add(new SimpleFileSystemResource(f.getName().replace(this.root,"")));
		}
		return resources;
	}

	@Override
	public String getName() {
		return resourceName;
	}

	@Override
	public boolean hasNested() {
		return file.isDirectory();
	}

}
