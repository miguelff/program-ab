package org.miguelff.alicebot.ab;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Common contract for IO resource providers,
 * offers some shortcuts for the most common operations
 * 
 * @author miguelff
 *
 */
public interface IOResourceProvider {

	InputStream inputFor(String resourceName);

	OutputStream outputFor(String resourceName);

	IOResource getResource(String resourceName);	
}

/**
 * Provides input and output streams for a resource, that exist 
 * in the file system
 * 
 * @author miguelff
 *
 */
class SimpleFileSystemResourceProvider implements IOResourceProvider {
	public InputStream inputFor(String resourceName) {
		return getResource(resourceName).input();
	}

	public OutputStream outputFor(String resourceName){
		return getResource(resourceName).output();
	}

	@Override
	public IOResource getResource(String resourceName) {
		return new SimpleFileSystemResource(resourceName);
	}
}
