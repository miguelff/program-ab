package org.miguelff.alicebot.ab.io;

import java.io.InputStream;
import java.io.OutputStream;
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
