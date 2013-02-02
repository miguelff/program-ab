package org.miguelff.alicebot.ab;

import org.miguelff.alicebot.ab.io.IOResourceProvider;
import org.miguelff.alicebot.ab.io.S3ResourceProvider;
import org.miguelff.alicebot.ab.io.SimpleFileSystemResourceProvider;
import org.miguelff.alicebot.ab.logging.ConsoleLog;
import org.miguelff.alicebot.ab.logging.ILog;
import org.miguelff.alicebot.ab.logging.NullLog;

/**
 * Facade to different resource providers 
 * 
 * @author miguelff
 *
 */
public abstract class ResourceProvider {
	
	public static final IOResourceProvider IO;
	
	public static final ILog Log;
	
	static {
		IO = initializeIO();
		Log = initializeLog();
	}
	
	private static IOResourceProvider initializeIO(){
		return (System.getenv("AWS_SECRET") != null) ? new S3ResourceProvider() : new SimpleFileSystemResourceProvider();
	}

	private static ILog initializeLog() {
		return (System.getenv("MUTE_LOG") != null) ? new NullLog() : new ConsoleLog();
	}

}
