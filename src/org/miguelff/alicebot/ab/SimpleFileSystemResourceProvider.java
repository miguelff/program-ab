package org.miguelff.alicebot.ab;

import java.io.InputStream;
import java.io.OutputStream;

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