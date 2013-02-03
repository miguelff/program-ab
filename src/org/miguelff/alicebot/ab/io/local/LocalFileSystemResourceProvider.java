package org.miguelff.alicebot.ab.io.local;

import org.miguelff.alicebot.ab.io.AbstractIOResourceProvider;
import org.miguelff.alicebot.ab.io.IOResource;

/**
 * Provides input and output streams for a resource, that exist 
 * in the file system
 * 
 * @author miguelff
 *
 */
public class LocalFileSystemResourceProvider extends AbstractIOResourceProvider {
	
	private static LocalFileSystemResourceProvider INSTANCE = new LocalFileSystemResourceProvider();
	
	public static LocalFileSystemResourceProvider getInstance() {
		return INSTANCE;
	}
	
	private LocalFileSystemResourceProvider() {};
	
	@Override
	public IOResource getResource(String resourceName) {
		return new LocalFileSystemResource(resourceName);
	}	
}