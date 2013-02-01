package org.miguelff.alicebot.ab;

/**
 * Façade to different resource providers 
 * 
 * @author miguelff
 *
 */
public abstract class ResourceProvider {
	
	public static final IOResourceProvider IO;
	
	static {
		IO = initializeIO();
	}
	
	private static IOResourceProvider initializeIO(){
		return new SimpleFileSystemResourceProvider();		
	}

}
