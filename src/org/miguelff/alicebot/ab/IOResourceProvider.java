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
